package sep.server.model.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.json.common.CurrentPlayerModel;
import sep.server.json.common.ErrorMsgModel;
import sep.server.json.game.ActivePhaseModel;
import sep.server.json.game.GameStartedModel;
import sep.server.json.game.effects.*;
import sep.server.model.game.tiles.*;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.json.game.MockGameStartedModel;
import sep.server.model.game.cards.Card;
import sep.server.model.game.builder.DeckBuilder;
import sep.server.model.game.cards.damage.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import org.json.JSONObject;
import sep.server.viewmodel.Session;

/**
 * The rules of the game are implemented here. It is a high-level manager object for one game and controls the
 * overall flow of it. It is spawned at the start of a game and not destroyed until the game has ended.
 */
public class GameMode
{
    private static final Logger l = LogManager.getLogger(Session.class);
    private Course course;
    ArrayList<Player> players;
    int energyBank;
    static GameState gameState;
    int availableCheckPoints;

    int gamePhase = 0; //0 => Aufbauphase, 1 => Upgradephase, 2 => Programmierphase, 3 => Aktivierungsphase

    ArrayList<SpamDamage> spamCardDeck;
    ArrayList<TrojanHorseDamage> trojanCardDeck;
    ArrayList<VirusDamage> virusCardDeck;
    ArrayList<WormDamage> wormDamageDeck;

    Player currentPlayer; //aktuell nur in setup-phase benutzt






    public GameMode(String course, PlayerController[] playerControllers)
    {
        super();

        this.course = new Course(course);
        this.gamePhase = 0;

        DeckBuilder deckBuilder = new DeckBuilder();
        spamCardDeck = deckBuilder.buildSpamDeck();
        trojanCardDeck = deckBuilder.buildTrojanDeck();
        virusCardDeck = deckBuilder.buildVirusDeck();
        wormDamageDeck = deckBuilder.buildWormDeck();

        this.players = new ArrayList<>();
        for(PlayerController pc : playerControllers)
        {
            this.players.add(new Player(pc, this.course));
            continue;
        }

        this.currentPlayer = players.get(0);

        //Real methods
        //send the built Course to all Clients
        for (PlayerController pc : playerControllers) {
            new GameStartedModel(pc.getClientInstance(), this.course.getCourse()).send();
            continue;
        }

        // Announcing Phase Zero.
        playerControllers[0].getSession().handleActivePhase(0);

        //Selecting starting player. (first one in PlayerControllers ArrayList
        new CurrentPlayerModel(currentPlayer.getPlayerController().getClientInstance(),
                currentPlayer.getPlayerController().getPlayerID()).send();


        /* Just temporary. This is for helping to develop the front-end.
        for (PlayerController pc : playerControllers) {
            new MockGameStartedModel(pc.getClientInstance()).send();
            continue;
        }

        // Announcing Phase Zero.
        for (PlayerController pc : playerControllers) {
            pc.getClientInstance().sendMockJSON(new JSONObject("{\"messageType\":\"ActivePhase\",\"messageBody\":{\"phase\":0}}"));
            continue;
        }

        // Selecting starting player.
        for (PlayerController pc : playerControllers) {
            pc.getClientInstance().sendMockJSON(new JSONObject(String.format("{\"messageType\":\"CurrentPlayer\",\"messageBody\":{\"clientID\":%d}}", playerControllers[0].getPlayerID())));
            continue;
        }
        */

        return;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    //TODO Refactoring in zwei oder mehr Methoden?
    public void setStartingPoint(PlayerController pc, int x, int y){
        if(gamePhase != 0){
            l.warn("Unable to set StartPoint due to wrong GamePhase");
            new ErrorMsgModel(pc.getClientInstance(), "Wrong Gamephase");

        } else if(pc.getPlayerID() != currentPlayer.getPlayerController().getPlayerID()){
            l.warn("Unable to set StartPoint due to wrong Player. Choosing Player is not currentPlayer");
            new ErrorMsgModel(pc.getClientInstance(), "Your are not CurrentPlayer");
            //ErrorMessage an Client & Logger; falscher Spieler

        } else{

            int validation = currentPlayer.getPlayerRobot().validStartingPoint(x,y);
            if(validation == 1){

                currentPlayer.getPlayerRobot().setStartingPoint(x,y);
                l.info("StartingPointSelected from PlayerID: " + pc.getPlayerID() + " with Coordinates: " + x + " , " + y);
                pc.getSession().handleSelectedStartingPoint(pc.getPlayerID(),x,y);

                if(startingPointSelectionFinished()){
                    //Wenn alle Spieler ihre StartPosition gesetzt haben, beginnt die ProgrammingPhase
                    gamePhase = 1;
                    pc.getSession().handleActivePhase(gamePhase);
                    l.info("StartPhase has concluded. ProgrammingPhase has started");
                    programmingPhase();


                } else{
                    for(Player player : players){
                        if (player.getPlayerRobot().getCurrentTile() == null){
                            currentPlayer = player;
                            l.info("Now Player with ID: " + player.getPlayerController().getPlayerID() + "has to set StartingPoint");
                            pc.getSession().handleCurrentPlayer(player.getPlayerController().getPlayerID());
                        }
                    }
                }
            }
            else{
                //Antwort, dass es nicht geklappt hat. (Soll hier konkrete Message an Client?
                l.warn("StartingPointSelection failed. Error Code from method validStartingPoint(): " + validation);
                new ErrorMsgModel(pc.getClientInstance(), "StartingPointSelection failed");
            }

        }

    }

    public boolean startingPointSelectionFinished(){
        for(Player player : players){
            if(player.getPlayerRobot().getCurrentTile() == null){
                return false;
            }
        }
        return true;
    }
    public void setAvailableCheckPoints(int availableCheckPoints) {
        this.availableCheckPoints = availableCheckPoints;
    }

    public void programmingPhase() {
        distributeCards(players);
    }

    public void startTimer() {
        gameState.sendStartTimer();

        try {
            Thread.sleep(30000); //30 sekunden
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int[] playerIdWhoNotFinished = new int[players.size()];
        int index = 0;

        for (Player player : players) {
            if (!player.checkRegisterStatus()) {
                playerIdWhoNotFinished[index++] = player.getPlayerController().getPlayerID();
            }
        }
        if (index < players.size()) {
            playerIdWhoNotFinished = Arrays.copyOf(playerIdWhoNotFinished, index);
        }

       gameState.sendStopTimer(playerIdWhoNotFinished);
    }



    /**
     * The following method handles the activation phase: it iterates over the different registers and
     * plays the current card for each player (players are sorted by priority). Once each player's
     * card has been played the board elements activate and the robot lasers are shot.
     */
    public void activationPhase() {
        for(int currentRegister = 0; currentRegister < 5; currentRegister++) {
            determinePriorities();
            sortPlayersByPriority(currentRegister);
            /*determineCurrentCards(currentRegister);*/
            for(int j = 0; j < players.size(); currentRegister++) {
                players.get(j).registers[currentRegister].playCard();
            }
            activateConveyorBelts(2);
            activateConveyorBelts(1);
            activatePushPanels(currentRegister);
            activateGears();
            shootBoardLasers();
            shootRobotLasers();
            checkEnergySpaces(currentRegister);
            checkCheckpoints();
        }
        endRound();
    }

    /**
     * The following method calculates the priorities for all players: First the distance from each robot to the
     * antenna is calculated. Next the priorities are assigned. The closest player gets the highest priority.
     */
    public void determinePriorities() {
        Coordinate antennaCoordinate = new Coordinate(0,4); //StartA board
        int maxPriority = players.size();
        int currentPriority = maxPriority;
        int[] distances = new int[players.size()];

        for(int i = 0; i < players.size(); i++) {
            Robot playerRobot = players.get(i).getPlayerRobot();
            Coordinate robotCoordinate = playerRobot.getCurrentTile().getCoordinate();
            distances[i] = Math.abs(antennaCoordinate.getXCoordinate() - robotCoordinate.getXCoordinate())
                    + Math.abs(antennaCoordinate.getYCoordinate() - robotCoordinate.getYCoordinate());
        }

        for(int j = 0; j < maxPriority; j++) {
            int minDistance = Integer.MAX_VALUE;
            int minIndex = -1;

            for(int i = 0; i < distances.length; i++) {
                if(distances[i] < minDistance) {
                    minDistance = distances[i];
                    minIndex = i;
                }
            }

            if(minIndex != -1) {
                players.get(minIndex).setPriority(currentPriority);
                currentPriority--;
                distances[minIndex] = Integer.MAX_VALUE;
            }
        }

    }

    /**
     * The following method sorts all players from highest to lowest priority.
     */
    public void sortPlayersByPriority(int currentRegisterIndex) {
        players.sort(Comparator.comparingInt(Player::getPriority).reversed());
    }

    /*public HashMap<Integer, String> determineCurrentCards(int currentRegisterIndex) {
        //TODO adjust to JSON wrapper class once created
        HashMap<Integer, String> currentCards = new HashMap<>();

        for(Player player : players) {
            String cardInRegister = ((Card) player.getCardInRegister(currentRegisterIndex)).getCardType();
            currentCards.put(player.getPlayerController().getPlayerID(), cardInRegister);
        }

        return currentCards;
    }*/

    /**
     * The following method handles the activation of conveyor belts and sends the corresponding JSON messages.
     * The robot is moved in the outcoming flow direction of the conveyor belt.
     * @param speed determines the amount of fields the robot is moved
     */
    private void activateConveyorBelts(int speed) {
        for (Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if (fieldType instanceof ConveyorBelt) {
                    ConveyorBelt conveyorBelt = (ConveyorBelt) fieldType;
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
                            return;
                        }

                        if (!player.getPlayerRobot().isMovable(course.getTileByCoordinate(newCoordinate))) {
                            return;
                        }

                        course.updateRobotPosition(player.getPlayerRobot(), newCoordinate);

                        for(Player player1 : players) {
                            new MovementModel(player1.getPlayerController().getClientInstance(),
                                    player.getPlayerController().getPlayerID(),
                                    newCoordinate.getXCoordinate(), newCoordinate.getYCoordinate()).send();
                        }
                    }
                }
            }
        }
    }

    /**
     * The following method is required during the conveyor belt activation period.
     * It checks if the robot moved onto another conveyor belt tile. If yes, the method checks
     * if the new conveyor belt tile has a curved arrow by comparing the incoming flow directions
     * with the outcoming flow direction. If yes, the direction of the robot is changed accordingly
     * and the corresponding JSON message is sent.
     * @param player Owner of the current robot
     * @param coordinate Coordinate of the new tile the robot moved onto
     */
    public void curvedArrowCheck(Player player, Coordinate coordinate) {
        Tile newTile = course.getTileByCoordinate(coordinate);
        for(FieldType newFieldType : newTile.getFieldTypes()) {
            if(newFieldType instanceof ConveyorBelt) {
                ConveyorBelt newConveyorBelt = (ConveyorBelt) newFieldType;
                String newOutDirection = newConveyorBelt.getOutcomingFlowDirection();
                String[] newInDirection = newConveyorBelt.getIncomingFlowDirection();
                String robotOldDirection = player.getPlayerRobot().getDirection();
                if(newInDirection != null && newOutDirection != null) {
                    for(String direction : newInDirection) {
                        switch(newOutDirection) {
                            case("top") -> player.getPlayerRobot().setDirection("NORTH");
                            case("right") -> player.getPlayerRobot().setDirection("EAST");
                            case("bottom") -> player.getPlayerRobot().setDirection("SOUTH");
                            case("left") -> player.getPlayerRobot().setDirection("WEST");
                        }
                    }
                    if((robotOldDirection == "NORTH" && player.getPlayerRobot().getDirection() == "EAST") ||
                            (robotOldDirection == "EAST" && player.getPlayerRobot().getDirection() == "SOUTH") ||
                            (robotOldDirection == "SOUTH" && player.getPlayerRobot().getDirection() == "WEST") ||
                            (robotOldDirection == "WEST" && player.getPlayerRobot().getDirection() == "NORTH")) {
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
     * @param currentRegister the register that is currently active
     */
    public void activatePushPanels(int currentRegister) {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof PushPanel) {
                    PushPanel pushPanel = (PushPanel) fieldType;
                    int[] activateAtRegister = pushPanel.getActivateAtRegister();

                    for(int register : activateAtRegister) {
                        if(register == currentRegister) {
                            String pushOrientation = pushPanel.getOrientation();
                            Coordinate oldCoordinate = currentTile.getCoordinate();

                            Coordinate newCoordinate = calculateNewCoordinate(pushOrientation, oldCoordinate);

                            if (!course.isCoordinateWithinBounds(newCoordinate)) {
                                player.getPlayerRobot().reboot();
                                return;
                            }

                            if (!player.getPlayerRobot().isMovable(course.getTileByCoordinate(newCoordinate))) {
                                return;
                            }

                            course.updateRobotPosition(player.getPlayerRobot(), newCoordinate);

                            for(Player player1 : players) {
                                new MovementModel(player1.getPlayerController().getClientInstance(),
                                        player.getPlayerController().getPlayerID(),
                                        newCoordinate.getXCoordinate(), newCoordinate.getYCoordinate()).send();
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
    public void activateGears() {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof Gear) {
                    Gear gear = (Gear) fieldType;
                    String rotationalDirection = gear.getRotationalDirection();
                    String robotDirection = player.getPlayerRobot().getDirection();
                    String newDirection = robotDirection;
                    if(rotationalDirection == "counterclockwise") {
                        switch (robotDirection) {
                            case "NORTH" -> newDirection = "WEST";
                            case "EAST" -> newDirection = "NORTH";
                            case "SOUTH" -> newDirection = "EAST";
                            case "WEST" -> newDirection = "SOUTH";
                        }
                    } else if(rotationalDirection == "clockwise") {
                        switch (robotDirection) {
                            case "NORTH" -> newDirection = "EAST";
                            case "EAST" -> newDirection = "SOUTH";
                            case "SOUTH" -> newDirection = "WEST";
                            case "WEST" -> newDirection = "NORTH";
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
    public void shootBoardLasers() {}
    public void shootRobotLasers() {}

    /**
     * The following method checks if any robot ended their register on an energy space, if
     * they receive an energy cube, and sends the corresponding JSON messages.
     * @param currentRegister the register that is currently active
     */
    public void checkEnergySpaces(int currentRegister) {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof EnergySpace) {
                    EnergySpace energySpace = (EnergySpace) fieldType;
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
    public void checkCheckpoints() {
        for(Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if(fieldType instanceof CheckPoint) {
                    CheckPoint checkPoint = (CheckPoint) fieldType;
                    int checkpointNumber = checkPoint.getCheckpointNumber();
                    if(player.getCheckpointsCollected() == checkpointNumber-1) {
                        player.setCheckpointsCollected(player.getCheckpointsCollected()+1);
                    }

                    for(Player player1 : players) {
                        new CheckPointModel(player1.getPlayerController().getClientInstance(),
                                player.getPlayerController().getPlayerID(),
                                player.getCheckpointsCollected()).send();
                    }

                    if(player.getCheckpointsCollected() == availableCheckPoints) {
                        endGame();
                        for(Player player1 : players) {
                            new GameFinishedModel(player1.getPlayerController().getClientInstance(),
                                    player.getPlayerController().getPlayerID()).send();
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
     * @return
     */
    public Coordinate calculateNewCoordinate(String orientation, Coordinate oldCoordinate) {
        Coordinate newCoordinate = null;
        switch (orientation) {
            case "top" -> newCoordinate = new Coordinate(oldCoordinate.getXCoordinate(),
                    oldCoordinate.getYCoordinate() - 1);
            case "right" -> newCoordinate = new Coordinate(oldCoordinate.getXCoordinate() + 1,
                    oldCoordinate.getYCoordinate());
            case "bottom" -> newCoordinate = new Coordinate(oldCoordinate.getXCoordinate(),
                    oldCoordinate.getYCoordinate() + 1);
            case "left" -> newCoordinate = new Coordinate(oldCoordinate.getXCoordinate() - 1,
                    oldCoordinate.getYCoordinate());
        }
        return newCoordinate;
    }

    /*public Object[] replaceCardInRegister(int currentRegisterIndex, int currentPlayerIndex) {
        //TODO adjust to JSON wrapper class once created
        Player player = players.get(currentPlayerIndex);
        IPlayableCard topCardFromDiscardPile = player.getPlayerDeck().get(0);
        String newCard = ((Card) topCardFromDiscardPile).getCardType();
        int clientID = player.getPlayerController().getPlayerID();

        player.setCardInRegister(currentRegisterIndex, topCardFromDiscardPile);

        return new Object[] {currentRegisterIndex, newCard, clientID};
    }*/

    public void distributeCards(ArrayList<Player> players) {
        for (Player player : players) {
            for (int i = 0; i < 9; i++) {
                if (player.getPlayerDeck().isEmpty()) {
                    player.shuffleAndRefillDeck();
                    gameState.sendShuffle(player);
                }
                IPlayableCard card = player.getPlayerDeck().remove(0);
                player.getPlayerHand().add(card);
            }

            gameState.sendHandCards(player);
        }

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

    public void endGame() {}

    public PlayerController[] getPlayerControllers()
    {
        PlayerController[] playerControllers = new PlayerController[this.players.size()];
        for (int i = 0; i < this.players.size(); i++)
        {
            playerControllers[i] = this.players.get(i).getPlayerController();
            continue;
        }
        return playerControllers;
    }

}
