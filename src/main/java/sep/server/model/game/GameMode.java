package sep.server.model.game;

import sep.server.json.common.ErrorMsgModel;
import sep.server.json.game.activatingphase.ReplaceCardModel;
import sep.server.json.game.effects.*;
import sep.server.model.game.cards.Card;
import sep.server.model.game.tiles.*;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.builder.DeckBuilder;
import sep.server.model.game.cards.damage.*;
import sep.server.viewmodel.Session;
import sep.server.model.IOwnershipable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.stream.Collectors;

/**
 * The rules of the game are implemented here. It is a high-level manager object for one game and controls the
 * overall flow of it. It is spawned at the start of a game and not destroyed until the game has ended.
 */
public class GameMode {

    private static final Logger l = LogManager.getLogger(Session.class);

    public static final int STARTING_ENERGY = 5;
    private static final int NEW_PROGRAMMING_CARDS = 9;
    private static final int REGISTER_PHASE_COUNT = 5;

    private final Course course;
    private final int availableCheckPoints;
    private final Session session;
    private EGamePhase gamePhase;

    private final ArrayList<SpamDamage> spamCardDeck;
    private final ArrayList<TrojanHorseDamage> trojanCardDeck;
    private final ArrayList<VirusDamage> virusCardDeck;
    private final ArrayList<WormDamage> wormDamageDeck;

    private final ArrayList<Player> players;
    private Player curPlayerInRegistration;

    /** TODO This may not be safe. Because clients may change behaviour during the activation phase. */
    private int currentRegister;

    /** @deprecated This is currently not initialized. Fix or remove. */
    private int energyBank;

    private Thread activationPhaseThread;

    public GameMode(final String courseName, final IOwnershipable[] ctrls, final Session session) {
        super();

        l.debug("Starting game with the following course: {}", courseName);

        // TODO WARNING
        //      This of course does not work and will throw if agents were to join the game.
        //      Just temporary until the interfaces are up and implemented.
        final PlayerController[] playerControllers = new PlayerController[ctrls.length];
        for (int i = 0; i < ctrls.length; i++)
        {
            playerControllers[i] = (PlayerController) ctrls[i];
        }

        this.course = new Course(courseName);
        this.availableCheckPoints = this.getAvailableCheckpoints(courseName);
        this.session = session;
        this.gamePhase = EGamePhase.INVALID;

        DeckBuilder deckBuilder = new DeckBuilder();
        this.spamCardDeck = deckBuilder.buildSpamDeck();
        this.trojanCardDeck = deckBuilder.buildTrojanDeck();
        this.virusCardDeck = deckBuilder.buildVirusDeck();
        this.wormDamageDeck = deckBuilder.buildWormDeck();

        this.players = Arrays.stream(playerControllers).map(pc -> new Player(pc, this.course, this.session)).collect(Collectors.toCollection(ArrayList::new));
        Arrays.stream(playerControllers).forEach(pc -> this.players.stream().filter(p -> p.getPlayerController() == pc).findFirst().ifPresent(pc::setPlayer));
        this.curPlayerInRegistration = this.players.get(0);
        this.currentRegister = 0;

        this.handleNewPhase(EGamePhase.REGISTRATION);
    }

    // region Game Phases

    // region Registration Phase Helpers

    /**
     * The following method checks if the starting point selection has been finished.
     * @return true if finished, false if not finished
     */
    private boolean startingPointSelectionFinished() {
        for(Player player : players) {
            if(player.getPlayerRobot().getCurrentTile() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * The following method is used to set a starting point (as long it is a valid one).
     * Afterward, next player for choosing a starting point is chosen or, if not possible, the phase is ended.
     * @param pc player that wants to set a starting point
     * @param x x coordinate of the starting point
     * @param y y coordinate of the starting point
     */
    public void setStartingPoint(PlayerController pc, int x, int y) {
        if(ableToSetStartPoint(pc)) {

            int validation = curPlayerInRegistration.getPlayerRobot().validStartingPoint(x,y);
            if(validation == 1) {

                curPlayerInRegistration.getPlayerRobot().setStartingPoint(x,y);
                l.info("StartingPointSelected from PlayerID: " + pc.getPlayerID() + " with Coordinates: " + x + " , " + y);
                pc.getSession().handleSelectedStartingPoint(pc.getPlayerID(),x,y);
                pc.getSession().handlePlayerTurning(pc.getPlayerID(), course.getStartingTurningDirection());

                if(startingPointSelectionFinished()) {
                    //Once all players have set their starting points, the programming phase starts
                    l.debug("Registration Phase has concluded. Upgrade Phase must be started.");
                    this.handleNewPhase(EGamePhase.UPGRADE);

                } else {
                    //Otherwise, the next player that hasn't set their robot yet is chosen
                    for(Player player : players) {
                        if (player.getPlayerRobot().getCurrentTile() == null) {
                            curPlayerInRegistration = player;
                            l.info("Now Player with ID: " + player.getPlayerController().getPlayerID() + "has to set StartingPoint");
                            pc.getSession().broadcastCurrentPlayer(player.getPlayerController().getPlayerID());
                        }
                    }
                }
            }
            else {
                l.warn("StartingPointSelection failed. Error Code from method validStartingPoint(): " + validation);
                new ErrorMsgModel(pc.getClientInstance(), "StartingPointSelection failed");
            }
        }
    }

    /**
     * Checks if setting a starting point is possible.
     * Prints corresponding error messages
     * @param pc Player that wants to set a starting point
     * @return true if possible, false if not possible
     */
    public boolean ableToSetStartPoint(PlayerController pc) {
        if (gamePhase != EGamePhase.REGISTRATION) {
            l.debug("Unable to set StartPoint due to wrong GamePhase");
            new ErrorMsgModel(pc.getClientInstance(), "Wrong GamePhase");
            return false;

        } else if (pc.getPlayerID() != curPlayerInRegistration.getPlayerController().getPlayerID()) {
            l.debug("Unable to set StartPoint due to wrong Player. Choosing Player is not currentPlayer");
            new ErrorMsgModel(pc.getClientInstance(), "Your are not CurrentPlayer");
            return false;

        } else {
            return true;
        }
    }

    // endregion Registration Phase Helpers

    /**
     * The following method triggers the registration phase.
     */
    private void triggerRegistrationPhase() {
        this.session.broadcastGameStart(this.course.getCourse());

        // TODO WARNING: This must stand here because the {@link Session#broadcastGameStart(ArrayList<ArrayList<Tile>>)}
        //               method will instantiate the game screen of the client. Therefore, the client will not be able
        //               to understand the following request if it is sent before the game screen is loaded. We may
        //               want to fix this on the client side!
        this.session.broadcastNewGamePhase(this.gamePhase);

        /* The current player is the first player that joined the game. */
        this.session.broadcastCurrentPlayer(this.curPlayerInRegistration.getPlayerController().getPlayerID());

        l.debug("Registration Phase started. Waiting for players to set their starting positions . . .");
    }

    /**
     * The following method triggers the upgrade phase.
     */
    private void triggerUpgradePhase(){
        l.debug("Upgrade Phase not implemented yet. Skipping to Programming Phase.");
        this.handleNewPhase(EGamePhase.PROGRAMMING);
    }

    /**
     * The following method triggers the programming phase and prepares the player decks.
     */
    private void triggerProgrammingPhase(){
        for (Player p : players){
            //TODO @possibleBUG muss hier nicht noch irgendwo die alte Hand bereinigt werden?
            for (int i = 0; i < GameMode.NEW_PROGRAMMING_CARDS; i++){
                if (p.getPlayerDeck().isEmpty()){
                    p.shuffleAndRefillDeck();
                    this.session.getGameState().sendShuffle(p);
                }
                p.getPlayerHand().add(p.getPlayerDeck().remove(0));
            }

            this.session.sendHandCardsToPlayer(p.getPlayerController(), p.getPlayerHandAsStringArray());
        }

        l.debug("Programming Phase started. All players have received their cards. Waiting for players to set their cards . . .");
    }

    // region Activation Phase Helpers

    /**
     * The following method calculates the priorities for all players: First the distance from each robot to the
     * antenna is calculated. Next the priorities are assigned. The closest player gets the highest priority.
     */
    private void determinePriorities() {
        final Coordinate antennaCoordinate = this.course.getPriorityAntennaCoordinate();
        l.debug("Determining priorities for all players. Found Priority Antenna at {}.", antennaCoordinate.toString());

        final int[] distances = new int[this.players.size()];
        for (int i = 0; i < this.players.size(); i++) {
            Coordinate robotCoordinate = this.players.get(i).getPlayerRobot().getCurrentTile().getCoordinate();
            distances[i] = Math.abs(antennaCoordinate.getX() - robotCoordinate.getX()) + Math.abs(antennaCoordinate.getY() - robotCoordinate.getY());
        }

        int currentPriority = this.players.size();
        for (int j = 0; j < this.players.size(); j++){
            int minDistance = Integer.MAX_VALUE;
            int minIndex = -1;

            for (int i = 0; i < distances.length; i++) {
                if (distances[i] < minDistance) {
                    minDistance = distances[i];
                    minIndex = i;
                }
            }

            if (minIndex != -1) {
                this.players.get(minIndex).setPriority(currentPriority);
                currentPriority--;
                distances[minIndex] = Integer.MAX_VALUE;
            }
        }
    }

    /**
     * The following method sorts all players in the players list according to their priorities.
     */
    private void sortPlayersByPriorityInDesc() {
        this.players.sort(Comparator.comparingInt(Player::getPriority).reversed());
    }

    /**
     * The following method handles the activation of conveyor belts and sends the corresponding JSON messages.
     * The robot is moved in the out-coming flow direction of the conveyor belt.
     * @param speed determines the amount of fields the robot is moved
     */
    private void activateConveyorBelts(int speed) {
        for (Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if (fieldType instanceof ConveyorBelt conveyorBelt) {
                    int beltSpeed = conveyorBelt.getSpeed();

                    if (beltSpeed == speed) {
                        Coordinate oldCoordinate = currentTile.getCoordinate();
                        String outDirection = conveyorBelt.getOutcomingFlowDirection();
                        Coordinate newCoordinate = null;

                        for(int i = 0; i<speed; i++) {
                            newCoordinate = calculateNewCoordinate(outDirection, oldCoordinate);
                            curvedArrowCheck(player, newCoordinate);
                        }

                        if (!course.isCoordinateWithinBounds(newCoordinate)) {
                            player.getPlayerRobot().reboot();
                        }

                        /*if (player.getPlayerRobot().isTraversable(currentTile, course.getTileByCoordinate(newCoordinate))) {
                            return;
                        }*/

                        course.updateRobotPosition(player.getPlayerRobot(), newCoordinate);

                        for(Player player1 : players) {
                            new MovementModel(player1.getPlayerController().getClientInstance(),
                                    player.getPlayerController().getPlayerID(),
                                    newCoordinate.getX(), newCoordinate.getY()).send();
                        }
                    }
                }
            }
        }
    }

    /**
     * The following method calculates the new coordinates for activating conveyor belts and push panels.
     * @param orientation direction the robot is moved to
     * @param oldCoordinate coordinates of the current push panel pushing a robot
     * @return new coordinate
     */
    public Coordinate calculateNewCoordinate(String orientation, Coordinate oldCoordinate) {
        Coordinate newCoordinate = null;
        switch (orientation) {
            case "top" -> newCoordinate = new Coordinate(oldCoordinate.getX(),
                    oldCoordinate.getY() - 1);
            case "right" -> newCoordinate = new Coordinate(oldCoordinate.getX() + 1,
                    oldCoordinate.getY());
            case "bottom" -> newCoordinate = new Coordinate(oldCoordinate.getX(),
                    oldCoordinate.getY() + 1);
            case "left" -> newCoordinate = new Coordinate(oldCoordinate.getX() - 1,
                    oldCoordinate.getY());
        }
        return newCoordinate;
    }

    /**
     * The following method is required during the conveyor belt activation period.
     * It checks if the robot moved onto another conveyor belt tile. If yes, the method checks
     * if the new conveyor belt tile has a curved arrow by comparing the incoming flow directions
     * with the out-coming flow direction. If yes, the direction of the robot is changed accordingly
     * and the corresponding JSON message is sent.
     * @param player Owner of the current robot
     * @param coordinate Coordinate of the new tile the robot moved onto
     */
    public void curvedArrowCheck(Player player, Coordinate coordinate) {
        Tile newTile = course.getTileByCoordinate(coordinate);
        for(FieldType newFieldType : newTile.getFieldTypes()) {
            if(newFieldType instanceof ConveyorBelt conveyorBelt) {
                String outDirection = conveyorBelt.getOutcomingFlowDirection();
                String[] inDirection = conveyorBelt.getIncomingFlowDirection();
                String robotOldDirection = player.getPlayerRobot().getDirection();
                if(inDirection != null && outDirection != null) {
                    for(String direction : inDirection) {
                        if(!Objects.equals(direction, outDirection)) {
                            switch(outDirection) {
                                case("top") -> player.getPlayerRobot().setDirection("top");
                                case("right") -> player.getPlayerRobot().setDirection("right");
                                case("bottom") -> player.getPlayerRobot().setDirection("bottom");
                                case("left") -> player.getPlayerRobot().setDirection("left");
                            }
                        }
                    }
                    if((Objects.equals(robotOldDirection, "top") && Objects.equals(player.getPlayerRobot().getDirection(), "right")) ||
                            (Objects.equals(robotOldDirection, "right") && Objects.equals(player.getPlayerRobot().getDirection(), "bottom")) ||
                            (Objects.equals(robotOldDirection, "bottom") && Objects.equals(player.getPlayerRobot().getDirection(), "left")) ||
                            (Objects.equals(robotOldDirection, "left") && Objects.equals(player.getPlayerRobot().getDirection(), "top"))) {
                        for(Player player1 : players) {
                            new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                                    player.getPlayerController().getPlayerID(),
                                    "clockwise").send();
                        }
                    } else {
                        for(Player player1 : players) {
                            new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                                    player.getPlayerController().getPlayerID(),
                                    "counterclockwise").send();
                        }
                    }
                }
            }
        }
    }

    /**
     * The following method handles the activation of push panels and sends the corresponding JSON messages.
     * The robot is moved to the next field in the direction of the panel's pushOrientation.
     */
    private void activatePushPanels() {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof PushPanel pushPanel) {
                    int[] activateAtRegister = pushPanel.getActivateAtRegister();

                    for(int register : activateAtRegister) {
                        if(register == currentRegister) {
                            String pushOrientation = pushPanel.getOrientation();
                            Coordinate oldCoordinate = currentTile.getCoordinate();

                            Coordinate newCoordinate = calculateNewCoordinate(pushOrientation, oldCoordinate);

                            if (!course.isCoordinateWithinBounds(newCoordinate)) {
                                player.getPlayerRobot().reboot();
                            }

                            /*if (player.getPlayerRobot().isTraversable(currentTile, course.getTileByCoordinate(newCoordinate))) {
                                return;
                            }*/

                            course.updateRobotPosition(player.getPlayerRobot(), newCoordinate);

                            for(Player player1 : players) {
                                new MovementModel(player1.getPlayerController().getClientInstance(),
                                        player.getPlayerController().getPlayerID(),
                                        newCoordinate.getX(), newCoordinate.getY()).send();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * The following method handles the activation of gears and sends the corresponding JSON messages.
     * The robot is rotated 90 degrees into the gear's rotational direction.
     */
    private void activateGears() {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof Gear gear) {
                    String rotationalDirection = gear.getRotationalDirection();
                    String robotDirection = player.getPlayerRobot().getDirection();
                    String newDirection = robotDirection;

                    if(Objects.equals(rotationalDirection, "counterclockwise")) {
                        switch (robotDirection) {
                            case "top" -> newDirection = "left";
                            case "right" -> newDirection = "top";
                            case "bottom" -> newDirection = "right";
                            case "left" -> newDirection = "bottom";
                        }
                    } else if(Objects.equals(rotationalDirection, "clockwise")) {
                        switch (robotDirection) {
                            case "top" -> newDirection = "right";
                            case "right" -> newDirection = "bottom";
                            case "bottom" -> newDirection = "left";
                            case "left" -> newDirection = "top";
                        }
                    }

                    player.getPlayerRobot().setDirection(newDirection);

                    for(Player player1 : players) {
                        new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                                player.getPlayerController().getPlayerID(), rotationalDirection).send();
                    }
                }
            }
        }
    }

    /**
     * The following method checks the course for lasers and passes the respective tile including its field types to
     * the handleLaserByDirection method.
     */
    private void findLasers() {
        for (ArrayList<Tile> row : course.getCourse()) {
            for (Tile tile : row) {
                for (FieldType fieldType : tile.getFieldTypes()) {
                    if (fieldType instanceof Laser) {
                        handleLaserByDirection((Laser) fieldType, tile);
                    }
                }
            }
        }
    }

    /**
     * The following method determines all parameters needed to shoot the robot lasers during the activation phase
     * and calls the handleLaserShooting method depending on the direction the robot is facing to.
     */
    private void shootRobotLasers() {
        for(Player player : players) {
            Robot playerRobot = player.getPlayerRobot();
            Tile robotTile = playerRobot.getCurrentTile();

            int robotTileXCoordinate = robotTile.getCoordinate().getX();
            int robotTileYCoordinate = robotTile.getCoordinate().getY();
            String robotDirection = playerRobot.getDirection();

            switch(robotDirection) {
                case "top" -> handleLaserShooting("top", 1, robotTileXCoordinate, robotTileYCoordinate, 0, -1);
                case "right" -> handleLaserShooting("right", 1, robotTileXCoordinate, robotTileYCoordinate, 1, 0);
                case "bottom" -> handleLaserShooting("bottom", 1, robotTileXCoordinate, robotTileYCoordinate, 0, 1);
                case "left" -> handleLaserShooting("left", 1, robotTileXCoordinate, robotTileYCoordinate, -1, 0);
            }
        }
    }

    /**
     * The following method determines the laser orientation. Depending on the orientation it passes different values to
     * the handleLaserShooting method.
     * @param laser laser object holding laser orientation and laser strength
     * @param tile tile of current laser being handled
     */
    private void handleLaserByDirection(Laser laser, Tile tile) {
        int laserXCoordinate = tile.getCoordinate().getX();
        int laserYCoordinate = tile.getCoordinate().getY();
        String laserOrientation = laser.getOrientation();
        int laserCount = Laser.getLaserCount();


        switch (laserOrientation) {
            case "top" -> handleLaserShooting("top", laserCount, laserXCoordinate, laserYCoordinate, 0, -1);
            case "right" -> handleLaserShooting("right", laserCount, laserXCoordinate, laserYCoordinate, 1, 0);
            case "bottom" -> handleLaserShooting("bottom", laserCount, laserXCoordinate, laserYCoordinate, 0, 1);
            case "left" -> handleLaserShooting("left", laserCount, laserXCoordinate, laserYCoordinate, -1, 0);
        }
    }

    /**
     * The following method shoots the laser and checks for obstacles in its way which stop the laser.
     * If the obstacle is a robot, they draw 1-3 spam cards, depending on the laser's strength.
     * @param laserOrientation direction the laser is shooting into
     * @param laserCount intensity of the laser, can vary between 1-3
     * @param x x-coordinate of laser tile
     * @param y y-coordinate of laser tile
     * @param xIncrement differs depending on laser orientation
     * @param yIncrement differs depending on laser orientation
     */
    private void handleLaserShooting(String laserOrientation, int laserCount, int x, int y, int xIncrement, int yIncrement) {
        boolean laserGoing = true;

        while (course.areCoordinatesWithinBounds(x, y) && laserGoing) {
            Tile tile = course.getTileByNumbers(x, y);

            if (tile.isOccupied()) {
                Robot occupyingRobot = tile.getRobot();

                for (Player player : players) {
                    if (player.getPlayerRobot() == occupyingRobot) {
                        for(int i = 0; i<laserCount; i++) {
                            if(!this.spamCardDeck.isEmpty()) {
                                player.getDiscardPile().add(this.spamCardDeck.remove(0));
                            }
                        }
                        laserGoing = false;
                        break;
                    }
                }
            }

            for (FieldType fieldType : tile.getFieldTypes()) {
                if (fieldType instanceof Wall wall) {
                    String[] orientations = wall.getOrientations();

                    for (String wallOrientation : orientations) {
                        if(((laserOrientation.equals("top") || laserOrientation.equals("bottom")) &&
                                (wallOrientation.equals("bottom") || wallOrientation.equals("top")) &&
                                (y != tile.getCoordinate().getY())) ||
                                ((laserOrientation.equals("left") || laserOrientation.equals("right")) &&
                                        (wallOrientation.equals("left") || wallOrientation.equals("right")) &&
                                        (x != tile.getCoordinate().getX()))) {
                            laserGoing = false;
                            break;
                        }
                    }
                }

                if (fieldType instanceof Antenna) {
                    laserGoing = false;
                    break;
                }
            }

            x += xIncrement;
            y += yIncrement;
        }
    }


    /**
     * The following method checks if any robot ended their register on an energy space, if
     * they receive an energy cube, and sends the corresponding JSON messages.
     */
    private void checkEnergySpaces() {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof EnergySpace energySpace) {
                    int availableEnergy = energySpace.getAvailableEnergy();
                    int currentEnergy = player.getEnergyCollected();
                    if(currentRegister == 5) {
                        if(energyBank > 0) {
                            player.setEnergyCollected(currentEnergy + 1);
                            energyBank -= 1;
                        }
                    } else if(availableEnergy > 0) {
                        player.setEnergyCollected(currentEnergy + 1);
                    }

                    for(Player player1 : players) {
                        new EnergyModel(player1.getPlayerController().getClientInstance(),
                                player.getPlayerController().getPlayerID(),
                                player.getEnergyCollected(),
                                "EnergySpace").send();
                    }
                }
            }
        }
    }

    /**
     * The following method checks if any robot has reached a checkpoint. If yes, the method
     * checks if it is the correct checkpoint according to numerical order. If it is the last
     * checkpoint it ends the game. The method also sends the corresponding JSON message.
     */
    private void checkCheckpoints() {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof CheckPoint checkPoint) {
                    int checkpointNumber = checkPoint.getCheckpointNumber();
                    if(player.getCheckpointsCollected() == checkpointNumber-1) {
                        player.setCheckpointsCollected(player.getCheckpointsCollected()+1);
                    }

                    for(Player player1 : players) {
                        new CheckPointReachedModel(player1.getPlayerController().getClientInstance(),
                                player.getPlayerController().getPlayerID(),
                                player.getCheckpointsCollected()).send();
                    }

                    if(player.getCheckpointsCollected() == availableCheckPoints) {
                        endGame(player);
                        for(Player player1 : players) {
                            new GameFinishedModel(player1.getPlayerController().getClientInstance(),
                                    player.getPlayerController().getPlayerID()).send();
                        }
                    }
                }
            }
        }
    }

    public void endGame(Player winner) {
        session.handleGameFinished(winner.getPlayerController().getPlayerID());
    }

    /**
     * The following method is called whenever the activation phase is ended. It empties the registers
     * and calls a method that refills the player deck.
     */
    public void endRound() {
        for(int i = 0; i<5; i++) {
            for(Player player : players) {
                player.getDiscardPile().add(player.getRegisters()[i]);
                player.getRegisters()[i] = null;
            }
        }

        for(Player player : players) {
            player.shuffleAndRefillDeck();
        }
    }

    // endregion Activation Phase Helpers

    /**
     * One run of a register in the activation phase. This method must
     * be called five times in a row to complete the activation phase.
     * @return True if the activation phase should continue. False otherwise.
     */
    private boolean runActivationPhase() {
        l.debug("Starting register phase {}.", this.currentRegister);

        this.determinePriorities();
        this.sortPlayersByPriorityInDesc();
        this.session.broadcastCurrentCards(this.currentRegister);

        for (Player p : this.players) {
            if (p.getRegisters()[this.currentRegister] != null) {
                l.info("Player {} is playing card {}.", p.getPlayerController().getPlayerID(), p.getRegisters()[this.currentRegister].getCardType());
                p.getRegisters()[this.currentRegister].playCard(p, this.currentRegister);
                continue;
            }

            l.warn("Player {} does not have a card in register {}.", p.getPlayerController().getPlayerID(), this.currentRegister);
        }

        // TODO Add delay between each activation phase step.
        this.activateConveyorBelts(2);
        this.activateConveyorBelts(1);
        this.activatePushPanels();
        this.activateGears();
        this.findLasers();
        this.shootRobotLasers();
        this.checkEnergySpaces();
        this.checkCheckpoints();

        this.currentRegister++;

        return this.currentRegister < GameMode.REGISTER_PHASE_COUNT;
    }

    private void triggerActivationPhase()
    {
        if (this.activationPhaseThread != null && this.activationPhaseThread.isAlive())
        {
            l.warn("Activation Phase is already running. Skipping . . .");
            return;
        }

        this.activationPhaseThread = new Thread(
        () ->
        {
            l.debug("Activation Phase started.");

            this.currentRegister = 0;
            while (this.runActivationPhase())
            {
                l.debug("Register {} in Activation Phase ended. Waiting 5s for the next register iteration . . .", this.currentRegister);
                try
                {
                    Thread.sleep(5_000); /* Just for debugging right now. */
                }
                catch (InterruptedException e)
                {
                    l.error("Activation Phase was interrupted. This should not happen!", e);
                    throw new RuntimeException(e);
                }
            }

            this.endRound();

            l.debug("Activation Phase ended successfully. Waiting 5s for the next phase . . .");

            try
            {
                Thread.sleep(5_000); /* Just for debugging right now. */
            }
            catch (InterruptedException e)
            {
                l.error("Activation Phase was interrupted. This should not happen!", e);
                throw new RuntimeException(e);
            }

            this.handleNewPhase(EGamePhase.UPGRADE);
        });

        this.activationPhaseThread.start();
    }

    /**
     * Interface for a new phase in the game. All phase updates should be called through this method.
     *
     * @param phase New phase to be set
     */
    public void handleNewPhase(EGamePhase phase) {
        l.info("Session [{}] is entering a new phase. From {} to {}.", this.session.getSessionID(), this.gamePhase, phase);

        // Because the game screen in the client is not yet loaded at this point.
        // Therefore, they will not be able to understand this request.
        if (phase != EGamePhase.REGISTRATION) {
            this.session.broadcastNewGamePhase(phase);
        }

        switch (phase) {
            case REGISTRATION -> {
                this.gamePhase = EGamePhase.REGISTRATION;
                this.triggerRegistrationPhase();
            }

            case UPGRADE -> {
                this.gamePhase = EGamePhase.UPGRADE;
                this.triggerUpgradePhase();
            }

            case PROGRAMMING -> {
                this.gamePhase = EGamePhase.PROGRAMMING;
                this.triggerProgrammingPhase();
            }

            case ACTIVATION -> {
                this.gamePhase = EGamePhase.ACTIVATION;
                this.triggerActivationPhase();
            }

            default -> this.gamePhase = EGamePhase.INVALID;
        }
    }

    // endregion Game Phases

    /**
     * Called in the method addCardToRegister in the Class Player when the players register is full
     */
    public void startTimer() {
        this.session.getGameState().sendStartTimer();

        // Sleep for 30 seconds
        Thread programmingCardThread = new Thread(() -> {
            try {
                Thread.sleep(30000); // Sleep for 30 seconds
            } catch (InterruptedException e) {
                l.info("All Players have set their Cards");
            }

            int[] playerIdWhoNotFinished = new int[players.size()];
            ArrayList playerWhoNotFinished = new ArrayList();
            int index = 0;
            for (Player player : players) {
                if (!player.hasPlayerFinishedProgramming()) {
                    playerIdWhoNotFinished[index++] = player.getPlayerController().getPlayerID();
                    playerWhoNotFinished.add(player);
                }
            }
            if (index < players.size()) {
                playerIdWhoNotFinished = Arrays.copyOf(playerIdWhoNotFinished, index);
            }
            this.session.getGameState().sendStopTimer(playerIdWhoNotFinished);

            discardAndDrawBlind(playerWhoNotFinished);

            triggerActivationPhase();
        });

        programmingCardThread.start();

        boolean haveAllPlayersSetTheirCards = true;
        for (Player player : players) {
            if (!player.hasPlayerFinishedProgramming()) {
                haveAllPlayersSetTheirCards= false;
            }
        }

        if (haveAllPlayersSetTheirCards) {
            programmingCardThread.interrupt();
            triggerActivationPhase();
        }
    }

    public void discardAndDrawBlind(ArrayList<Player> players) {
        for (Player player : players) {
            player.handleIncompleteProgramming();
        }
    }

    /**
     * The following method is called whenever a card in a register needs to be replaced by
     * another card (by default, top card of player deck).
     * @param player player who needs to replace their cards
     * @param card card that will be added instead
     */
    public void replaceCardInRegister(Player player, IPlayableCard card) {
        player.getDiscardPile().add(player.getCardByRegisterIndex(currentRegister));
        player.getRegisters()[currentRegister] = null;

        IPlayableCard topCardFromDiscardPile = player.getPlayerDeck().get(0);
        String newCard = ((Card) topCardFromDiscardPile).getCardType();
        player.setCardInRegister(currentRegister, topCardFromDiscardPile);

        new ReplaceCardModel(player.getPlayerController().getClientInstance(),
                currentRegister, player.getPlayerController().getPlayerID(),
                newCard).send();
    }

    /* TODO Remove player after connection loss */
    public void removePlayer(final int playerID)
    {
        l.error("Removing player with ID {} from game. Not implemented yet.", playerID);
        return;
    }

    // region Getters and Setters

    public ArrayList<Player> getPlayers()
    {
        return this.players;
    }

    public ArrayList<SpamDamage> getSpamDeck()
    {
        return spamCardDeck;
    }

    public ArrayList<TrojanHorseDamage> getTrojanDeck()
    {
        return trojanCardDeck;
    }

    public ArrayList<VirusDamage> getVirusDeck()
    {
        return virusCardDeck;
    }

    public ArrayList<WormDamage> getWormDeck()
    {
        return wormDamageDeck;
    }

    public int getAvailableCheckpoints(final String courseName) {
        if (courseName.equals("DizzyHighway")) {
            return 1;
        }

        return 0;
    }

    public PlayerController[] getPlayerControllers() {
        PlayerController[] playerControllers = new PlayerController[this.players.size()];
        for (int i = 0; i < this.players.size(); i++)
        {
            playerControllers[i] = this.players.get(i).getPlayerController();
        }
        return playerControllers;
    }

    // endregion Getters and Setters

}
