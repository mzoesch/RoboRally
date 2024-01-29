package sep.server.model.game;

import sep.Types;
import sep.server.json.common.ErrorMsgModel;
import sep.server.json.game.damage.DrawDamageModel;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.tiles.*;
import sep.server.viewmodel.PlayerController;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.builder.DeckBuilder;
import sep.server.model.game.cards.damage.*;
import sep.server.viewmodel.Session;
import sep.server.model.IOwnershipable;
import sep.server.model.Agent;
import sep.server.viewmodel.EServerInstance;
import java.util.*;

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
    private final GameState gameState;
    private EGamePhase gamePhase;

    private final ArrayList<SpamDamage> spamCardDeck;
    private final ArrayList<TrojanHorseDamage> trojanCardDeck;
    private final ArrayList<VirusDamage> virusCardDeck;
    private final ArrayList<WormDamage> wormDamageDeck;
    private final ArrayList<AUpgradeCard> upgradeDeck;

    private final ArrayList<Player> players;
    private Player curPlayerInRegistration;

    /** TODO This may not be safe. Because clients may change behaviour during the activation phase. */
    private int currentRegisterIndex;

    private int energyBank;
    private ArrayList<AUpgradeCard> upgradeShop;

    private Thread activationPhaseThread;

    private boolean bFirstRound;

    private final ArrayList<Player> upgradePhasePlayersSortedByPriority;

    private Thread programmingPhaseTimerService;

    public GameMode(final String courseName, final GameState gameState) {
        super();

        l.debug("Starting game with the following course: {}", courseName);

        this.course = new Course(courseName);
        this.availableCheckPoints = this.getAvailableCheckpoints(courseName);
        this.gameState = gameState;
        this.gamePhase = EGamePhase.INVALID;

        DeckBuilder deckBuilder = new DeckBuilder();
        this.spamCardDeck = deckBuilder.buildSpamDeck();
        this.trojanCardDeck = deckBuilder.buildTrojanDeck();
        this.virusCardDeck = deckBuilder.buildVirusDeck();
        this.wormDamageDeck = deckBuilder.buildWormDeck();

        this.upgradeDeck = deckBuilder.buildUpgradeDeck();
        Collections.shuffle(this.upgradeDeck);
        this.upgradeShop = new ArrayList<AUpgradeCard>();

        this.energyBank = 48;

        this.players = Arrays.stream(this.getControllers()).map(ctrl -> new Player(ctrl, this.course)).collect(Collectors.toCollection(ArrayList::new));
        Arrays.stream(this.getControllers()).forEach(ctrl -> this.players.stream().filter(p -> p.getController() == ctrl).findFirst().ifPresent(ctrl::setPlayer));
        this.curPlayerInRegistration = this.players.get(0);
        this.currentRegisterIndex = 0;

        this.bFirstRound = true;

        this.upgradePhasePlayersSortedByPriority = new ArrayList<Player>();

        this.handleNewPhase(EGamePhase.REGISTRATION);

        this.programmingPhaseTimerService = null;

        return;
    }

    // region Game Phases

    // region Registration Phase Helpers

    /**
     * The following method checks if the starting point selection has been finished.
     *
     * @return true if finished, false if not finished
     */
    private boolean startingPointSelectionFinished() {
        for (Player player : players) {
            if (player.getPlayerRobot().getCurrentTile() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * The following method is used to set a starting point (as long it is a valid one).
     * Afterward, next player for choosing a starting point is chosen or, if not possible, the phase is ended.
     *
     * @param ctrl player that wants to set a starting point
     * @param x    x coordinate of the starting point
     * @param y    y coordinate of the starting point
     */
    public synchronized void setStartingPoint(IOwnershipable ctrl, int x, int y) {
        if (ableToSetStartPoint(ctrl)) {

            int validation = curPlayerInRegistration.getPlayerRobot().validStartingPoint(x, y);
            if (validation == 1) {

                curPlayerInRegistration.getPlayerRobot().setStartingPoint(x, y);
                l.info("StartingPointSelected from PlayerID: " + ctrl.getPlayerID() + " with Coordinates: " + x + " , " + y);
                ctrl.getAuthGameMode().getSession().broadcastSelectedStartingPoint(ctrl.getPlayerID(), x, y);

                if (startingPointSelectionFinished()) {
                    l.debug("Registration Phase has concluded. Upgrade Phase must be started.");
                    this.handleNewPhase(EGamePhase.UPGRADE);

                } else {
                    for (Player player : players) {
                        if (player.getPlayerRobot().getCurrentTile() == null) {
                            curPlayerInRegistration = player;
                            l.info("Now Player with ID: " + player.getController().getPlayerID() + " has to set StartingPoint");
                            ctrl.getAuthGameMode().getSession().broadcastCurrentPlayer(player.getController().getPlayerID());
                            if (player.getController() instanceof final Agent a) {
                                a.evaluateStartingPoint();
                            }
                            return;
                        }
                    }
                }
            } else {
                l.warn("StartingPointSelection failed. Error Code from method validStartingPoint(): " + validation);
                if (ctrl instanceof PlayerController pc) {
                    new ErrorMsgModel(pc.getClientInstance(), "StartingPointSelection failed");
                } else {
                    l.error("The agent {} tried to do something illegal. Starting point selection failed.", ctrl.getPlayerID());
                }
            }
        }
    }

    /**
     * Checks if setting a starting point is possibl and prints corresponding error messages.
     *
     * @param ctrl Player that wants to set a starting point
     * @return true if possible, false if not possible
     */
    public boolean ableToSetStartPoint(IOwnershipable ctrl) {
        if (gamePhase != EGamePhase.REGISTRATION) {
            l.debug("Unable to set StartPoint due to wrong GamePhase");
            if (ctrl instanceof PlayerController pc) {
                new ErrorMsgModel(pc.getClientInstance(), "Wrong GamePhase");
            } else {
                l.error("The agent {} tried to do something illegal.", ctrl.getPlayerID());
            }
            return false;

        } else if (ctrl.getPlayerID() != curPlayerInRegistration.getController().getPlayerID()) {
            l.error("Unable to set StartPoint due to wrong Player. Choosing Player is not currentPlayer. [CurrentPlayer: {}, ChoosingPlayer: {}]", curPlayerInRegistration.getController().getPlayerID(), ctrl.getPlayerID());
            if (ctrl instanceof PlayerController pc) {
                new ErrorMsgModel(pc.getClientInstance(), "Your are not CurrentPlayer");
            } else {
                l.error("The agent {} tried to do something illegal.", ctrl.getPlayerID());
            }
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
        this.getSession().broadcastGameStart(this.course.getCourse());

        /* The current player is the first player that joined the game. */
        this.getSession().broadcastCurrentPlayer(this.curPlayerInRegistration.getController().getPlayerID());

        if (this.curPlayerInRegistration.getController() instanceof final Agent a) {
            l.fatal("An agent must never be the first current player in a game. Fault agent ID: {}.", a.getPlayerID());
            EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
            return;
        }

        l.debug("Registration Phase started. Waiting for players to set their starting positions . . .");
    }

    //region Upgrade Phase helpers

    private static final class RefilledCards
    {
        public final ArrayList<String> out;
        public RefilledCards() { super(); this.out = new ArrayList<String>(); return; }
    }

    /**
     * Sets up the upgrade shop by either refilling it or exchanging upgrade slots.
     *
     * @return True if the upgrade shop was exchanged, false otherwise.
     */
    private boolean setupUpgradeShop(final RefilledCards outRefilledCards)
    {
        if (!this.isUpgradeShopRightSize())
        {
            l.warn("The upgrade shop is not the right size. Expected {}, got {}. Adjusting upgrade shop.", this.players.size(), this.upgradeShop.size());

            for (final AUpgradeCard upgradeCard : this.upgradeShop)
            {
                if (upgradeCard == null)
                {
                    continue;
                }

                this.upgradeDeck.add(upgradeCard);

                continue;
            }

            this.upgradeShop.clear();

            for (int i = 0; i < this.players.size(); ++i)
            {
                this.upgradeShop.add(null);
                continue;
            }

            l.debug("Adjust upgrade shop size to {}.", this.upgradeShop.size());
        }

        final boolean bExchange = !this.isACardMissingInUpgradeShop();

        if (bExchange)
        {
            l.debug("No upgrade cards were bought last phase. Exchanging upgrade slots. Old shop: {}.", this.upgradeShop.toString());
        }
        else
        {
            l.debug("Upgrade cards were bought last phase. Refilling upgrade shop. Old shop: {}.", this.upgradeShop.toString());
        }

        if (this.isACardMissingInUpgradeShop())
        {
            this.refillUpgradeShop(outRefilledCards);
        }
        else
        {
            this.exchangeUpgradeSlots();
        }

        return bExchange;
    }

    private boolean isACardMissingInUpgradeShop()
    {
        for (final AUpgradeCard upgradeCard : this.upgradeShop)
        {
            if (upgradeCard == null)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Refills the upgrade shop with new upgrade cards.
     */
    private void refillUpgradeShop(final RefilledCards outRefilledCards)
    {
        for (int i = 0; i < this.upgradeShop.size(); ++i)
        {
            if (this.upgradeShop.get(i) == null)
            {
                if (this.upgradeDeck.get(0) == null)
                {
                    l.error("Something went wrong while refilling the upgrade shop. The upgrade deck is: {}.", this.upgradeDeck.toString());
                    EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
                    return;
                }

                this.upgradeShop.set(i, this.upgradeDeck.get(0));
                this.upgradeDeck.remove(0);

                outRefilledCards.out.add(this.upgradeShop.get(i).getCardType());

                l.debug("Refilled upgrade shop at index {} with {}.", i, this.upgradeShop.get(i).toString());
            }

            continue;
        }

        l.info("Upgrade shop refilled. The new cards are: {}.", this.upgradeShop.toString());

        return;
    }

    /**
     * Exchanges upgrade slots in the upgrade shop.
     */
    private void exchangeUpgradeSlots()
    {
        for (int i = 0; i < this.upgradeShop.size(); ++i)
        {
            if (this.upgradeShop.get(i) != null)
            {
                this.upgradeDeck.add(this.upgradeShop.get(i));
                this.upgradeShop.set(i, null);
            }

            this.upgradeShop.set(i, this.upgradeDeck.get(0));
            this.upgradeDeck.remove(0);

            l.debug("Refilled upgrade shop at index {} with {}.", i, upgradeShop.get(i).toString());
        }

        l.info("Cards in upgrade shop slots exchanged. The new cards are: {}.", upgradeShop.toString());

        return;
    }

    private void removeCardFromShop(final String card)
    {
        for (int i = 0; i < this.upgradeShop.size(); ++i)
        {
            if (this.upgradeShop.get(i) == null)
            {
                continue;
            }

            if (this.upgradeShop.get(i).getCardType().equals(card))
            {
                this.upgradeShop.set(i, null);
                break;
            }

            continue;
        }

        return;
    }

    //endregion Upgrade Phase helpers

    /**
     * The following method triggers the upgrade phase.
     */
    private void triggerUpgradePhase()
    {
        final RefilledCards     out         = new RefilledCards();
        final boolean           bExchanged  = this.setupUpgradeShop(out);

        /* We always send the refill shop request to the client in the first round. */
        if (this.bFirstRound)
        {
            this.getSession().broadcastShopRefill(this.upgradeShop.stream().filter(Objects::nonNull).map(AUpgradeCard::getCardType).collect(Collectors.toCollection(ArrayList::new)));
            this.bFirstRound = false;
        }
        else
        {
            if (bExchanged)
            {
                this.getSession().broadcastShopExchange(this.upgradeShop.stream().filter(Objects::nonNull).map(AUpgradeCard::getCardType).collect(Collectors.toCollection(ArrayList::new)));
            }
            else
            {
                this.getSession().broadcastShopRefill(out.out);
            }
        }

        this.evaluateUpgradePhasePriorities();
        this.getSession().broadcastCurrentPlayer(this.upgradePhasePlayersSortedByPriority.get(0).getController().getPlayerID());
        this.upgradePhasePlayersSortedByPriority.remove(0);

        return;
    }

    private void executePostBuyBehavior()
    {
        if (!this.upgradePhasePlayersSortedByPriority.isEmpty())
        {
            l.debug("Waiting for client {} to finish their upgrade phase.", this.upgradePhasePlayersSortedByPriority.get(0).getController().getPlayerID());
            this.getSession().broadcastCurrentPlayer(this.upgradePhasePlayersSortedByPriority.get(0).getController().getPlayerID());
            this.upgradePhasePlayersSortedByPriority.remove(0);

            return;
        }

        this.handleNewPhase(EGamePhase.PROGRAMMING);

        return;
    }

    public void onUpgradeCardBought(final PlayerController pc, final String card)
    {
        if (card == null)
        {
            l.debug("Player {} bought nothing. Continuing.", pc.getPlayerID());
            this.executePostBuyBehavior();
            return;
        }

        if (!this.doesUpgradeShopContains(card))
        {
            l.error("Player {} tried to buy a card that is not in the upgrade shop. They requested {} but the upgrade shop is: {}. Ignoring.", pc.getPlayerID(), card, this.upgradeShop.toString());
            new ErrorMsgModel(pc.getClientInstance(), String.format("The card %s you tried to buy is not in the upgrade shop.", card));
            this.executePostBuyBehavior();
            return;
        }

        if (this.getUpgradeCardCost(card) == -1)
        {
            l.error("Player {} tried to buy a card that is not in the upgrade shop. They requested {} but the upgrade shop is: {}. Ignoring", pc.getPlayerID(), card, this.upgradeShop.toString());
            new ErrorMsgModel(pc.getClientInstance(), String.format("The card %s you tried to buy is not in the upgrade shop.", card));
            this.executePostBuyBehavior();
            return;
        }

        if (pc.getPlayer().getEnergyCollected() < this.getUpgradeCardCost(card))
        {
            l.error("Player {} tried to buy a card but does not have enough energy. They had {} but needed {}.", pc.getPlayerID(), pc.getPlayer().getEnergyCollected(), this.getUpgradeCardCost(card));
            new ErrorMsgModel(pc.getClientInstance(), String.format("You need %d energy to buy %s.", this.getUpgradeCardCost(card), card));
            this.executePostBuyBehavior();
            return;
        }

        final int cost = this.getUpgradeCardCost(card);
        this.removeCardFromShop(card);
        pc.getPlayer().getBoughtUpgradeCards().add(card);
        pc.getPlayer().setEnergyCollected(pc.getPlayer().getEnergyCollected() - cost);
        this.getSession().broadcastBoughtUpgradeCard(pc.getPlayerID(), card);
        l.debug("Player {} bought {}. The new upgrade shop is: {}.", pc.getPlayerID(), card, this.upgradeShop.toString());
        this.getSession().broadcastEnergyUpdate(pc.getPlayerID(), pc.getPlayer().getEnergyCollected(), "EnergySpent");
        this.executePostBuyBehavior();

        return;
    }

    /**
     * The following method triggers the programming phase and prepares the player decks.
     */
    private void triggerProgrammingPhase() {
        for (Player p : players) {
            p.clearOldHand();
            p.clearOldRegister();

            int maxCards = Math.min(GameMode.NEW_PROGRAMMING_CARDS, p.getPlayerDeck().size());

            for (int i = 0; i < maxCards; i++) {
                p.getPlayerHand().add(p.getPlayerDeck().remove(0));
            }

            p.shuffleAndRefillDeck();
            l.debug("P {} - Shuffling and refilling deck.", p.getController().getName());
            this.getSession().sendShuffleCodingNotification(p.getController().getPlayerID());

            int remainingCards = 9 - maxCards;
            for (int i = 0; i < remainingCards; i++) {
                p.getPlayerHand().add(p.getPlayerDeck().remove(0));
            }

            l.debug("P {} - Has following Cards in his Hand: {}", p.getController().getName(), Arrays.toString(p.getPlayerHandAsStringArray()));

            if (p.getController() instanceof PlayerController pc) {
                pc.getSession().sendHandCardsToPlayer(pc, p.getPlayerHandAsStringArray());
                continue;
            }

            // TODO: Call a method on the agent and let them decide how they want to play this programming phase.

            if (p.getController() instanceof final Agent a) {
                a.evaluateProgrammingPhase();
                continue;
            }

            l.error("No matching instance found for handling the programming phase for player {}.", p.getController().getPlayerID());
            continue;
        }

        l.debug("Programming Phase started. All remote controllers have received their cards. Waiting for them to set their cards . . .");
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
        for (int j = 0; j < this.players.size(); j++) {
            int minDistance = Integer.MAX_VALUE;
            int minIndex = -1;

            for (int i = 0; i < distances.length; i++) {
                if (distances[i] < minDistance) {
                    minDistance = distances[i];
                    minIndex = i;
                }
            }

            //Leave until new code was tested
            /*if (minIndex != -1) {
                this.players.get(minIndex).setPriority(currentPriority);
                currentPriority--;
                distances[minIndex] = Integer.MAX_VALUE;
            }*/

            //Now, includes AdminPrivilege logic. Still needs to be tested once shop is available.
            if (minIndex != -1) {
                Player currentPlayer = this.players.get(minIndex);

                if (currentPlayer.getHasAdminPrivilegeUpgrade() && currentPlayer.getChosenRegisterAdminPrivilegeUpgrade() == currentRegisterIndex) {
                    currentPlayer.setPriority(currentPriority);
                    currentPriority--;
                    distances[minIndex] = Integer.MAX_VALUE;
                } else {
                    currentPlayer.setPriority(currentPriority);
                    currentPriority--;
                    distances[minIndex] = Integer.MAX_VALUE;
                }
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
     * activate the ConveyorBelts and broadcast player positions
     */
    public void activateConveyorBelts() {

        activateBlueConveyorBelts();
        activateBlueConveyorBelts();
        for (Player player : players) {
            this.getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition().getX(), player.getPosition().getY());
        }
        activateGreenConveyorBelts();
        for (Player player : players) {
            this.getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition().getX(), player.getPosition().getY());
        }
    }

    /**
     * activates blue conveyor belts (but only one time)
     */
    private void activateBlueConveyorBelts() {

        for (Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if (fieldType instanceof ConveyorBelt conveyorBelt) {
                    int beltSpeed = conveyorBelt.getSpeed();

                    if (beltSpeed == 2) {
                        Coordinate oldCoordinate = currentTile.getCoordinate();
                        String outDirection = conveyorBelt.getOutcomingFlowDirection();
                        Coordinate targetCoordinate = calculateNewCoordinate(outDirection, oldCoordinate);

                        if(!course.getTileByCoordinate(targetCoordinate).isOccupied()){
                            curvedArrowCheck(player, targetCoordinate, outDirection);
                            player.getPlayerRobot().moveRobotOneTile(true, outDirection, 0);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(null);
                            course.updateRobotPosition(player.getPlayerRobot(), targetCoordinate);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(player.getPlayerRobot());
                        }
                    }
                }
            }
        }
    }


    /**
     * activates green conveyor belts
     */
    public void activateGreenConveyorBelts(){
        for (Player player : players) {
            Tile currentTile = player.getPlayerRobot().getCurrentTile();

            for (FieldType fieldType : currentTile.getFieldTypes()) {
                if (fieldType instanceof ConveyorBelt conveyorBelt) {
                    int beltSpeed = conveyorBelt.getSpeed();

                    if (beltSpeed == 1) {
                        Coordinate oldCoordinate = currentTile.getCoordinate();
                        String outDirection = conveyorBelt.getOutcomingFlowDirection();
                        Coordinate targetCoordinate = calculateNewCoordinate(outDirection, oldCoordinate);
                        curvedArrowCheck(player, targetCoordinate, outDirection);

                        if(!course.getTileByCoordinate(targetCoordinate).isOccupied()){
                            player.getPlayerRobot().moveRobotOneTile(true, outDirection, 0);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(null);
                            course.updateRobotPosition(player.getPlayerRobot(), targetCoordinate);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(player.getPlayerRobot());
                        }
                    }
                }
            }
        }
    }

    /**
     * method for moving checkpoints, if they are placed on conveyorBelts (like in course twister)
     */
    public void moveCheckpoints(){
        if(course.getMovingCheckpoints()){
            ArrayList<Coordinate> oldCheckpointCoordinates = course.getCheckpointCoordinates();

            for(Coordinate oldCoordinate : oldCheckpointCoordinates){
                //removing the checkpoint from his old tile
                Tile oldTile = course.getTileByCoordinate(oldCoordinate);
                CheckPoint checkpoint = oldTile.removeCheckpoint();

                //get the new Coordinate for the CheckPoint
                ConveyorBelt conveyorBelt = oldTile.getConveyorBelt();
                String outDirection = conveyorBelt.getOutcomingFlowDirection();
                Coordinate targetCoordinate = calculateNewCoordinate(outDirection, oldCoordinate);

                if(conveyorBelt.getSpeed()>1) {
                    ConveyorBelt nextConveyorBelt = course.getTileByCoordinate(targetCoordinate).getConveyorBelt();
                    targetCoordinate = calculateNewCoordinate(nextConveyorBelt.getOutcomingFlowDirection(), targetCoordinate);
                }

                //add the Checkpoint to the new Tile
                Tile newTile = course.getTileByCoordinate(targetCoordinate);
                newTile.addCheckPoint(checkpoint);

                this.getSession().broadcastCheckPointMoved(checkpoint.getCheckpointNumber(), targetCoordinate.getX(), targetCoordinate.getY());
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
     * if the new conveyor belt tile has a curved arrow by comparing the incoming flow direction
     * with the out-coming flow direction. If yes, the direction of the robot is changed accordingly
     * and the corresponding JSON message is sent.
     * @param player Owner of the current robot
     * @param coordinate Coordinate of the new tile the robot moved onto
     * @param direction Direction from which the robot was moved into the conveyor belt
     */
    public void curvedArrowCheck(Player player, Coordinate coordinate, String direction) {
        Tile newTile = course.getTileByCoordinate(coordinate);
        for(FieldType newFieldType : newTile.getFieldTypes()) {
            if(newFieldType instanceof ConveyorBelt conveyorBelt) {
                String outDirection = conveyorBelt.getOutcomingFlowDirection();

                if(direction != null && outDirection != null) {
                        if((Objects.equals(direction, "bottom") && outDirection.equals("right")) ||
                                (Objects.equals(direction, "left") && outDirection.equals("bottom")) ||
                                (Objects.equals(direction, "top") && outDirection.equals("left")) ||
                                (Objects.equals(direction, "right") && outDirection.equals("top"))) {

                            switch(player.getPlayerRobot().getDirection()) {
                                case("top") -> player.getPlayerRobot().setDirection("right");
                                case("right") -> player.getPlayerRobot().setDirection("bottom");
                                case("bottom") -> player.getPlayerRobot().setDirection("left");
                                case("left") -> player.getPlayerRobot().setDirection("top");
                            }

                            this.getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");

                        } else if((Objects.equals(direction, "bottom") && outDirection.equals("left")) ||
                                (Objects.equals(direction, "left") && outDirection.equals("top")) ||
                                (Objects.equals(direction, "top") && outDirection.equals("right")) ||
                                (Objects.equals(direction, "right") && outDirection.equals("bottom"))) {

                            switch(player.getPlayerRobot().getDirection()) {
                                case ("top") -> player.getPlayerRobot().setDirection("left");
                                case ("right") -> player.getPlayerRobot().setDirection("top");
                                case ("bottom") -> player.getPlayerRobot().setDirection("right");
                                case ("left") -> player.getPlayerRobot().setDirection("bottom");
                            }

                            this.getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");

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
                        if(register == (currentRegisterIndex +1)) {
                            l.debug("Push Panel is activated.");
                            String pushOrientation = pushPanel.getOrientation();
                            Coordinate oldCoordinate = currentTile.getCoordinate();

                            Coordinate targetCoordinate = calculateNewCoordinate(pushOrientation, oldCoordinate);

                            //TODO refactor to use moveForward method from Robot class:

                            if (!player.getPlayerRobot().getCourse().isCoordinateWithinBounds(targetCoordinate) ||
                                    player.getPlayerRobot().getCourse().getTileByCoordinate(targetCoordinate).isPit()) {
                                l.debug("Player {}'s robot moved to {} and fell off the board. Rebooting . . .",
                                        player.getPlayerRobot().determineRobotOwner().getController().getPlayerID(),
                                        targetCoordinate.toString());
                                player.getPlayerRobot().reboot();
                                return;
                            }

                            if(player.getPlayerRobot().getCourse().getTileByCoordinate(targetCoordinate).isPit()) {
                                l.debug("Player {}'s robot moved to {} and fell down a pit. Rebooting . . .",
                                        player.getPlayerRobot().determineRobotOwner().getController().getPlayerID(),
                                        targetCoordinate.toString());
                                player.getPlayerRobot().reboot();
                                return;
                            }

                            if (!player.getPlayerRobot().isTraversable(player.getPlayerRobot().getCourse().
                                            getTileByCoordinate(oldCoordinate),
                                    player.getPlayerRobot().getCourse().getTileByCoordinate(targetCoordinate), 0)) {
                                l.debug("Player {}'s robot wanted to traverse an impassable tile [from {} to {}]. " +
                                        "Ignoring.", player.getPlayerRobot().determineRobotOwner().getController().
                                        getPlayerID(), oldCoordinate.toString(), targetCoordinate.toString());
                                return;
                            }

                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(null);
                            course.updateRobotPosition(player.getPlayerRobot(), targetCoordinate);
                            player.getPlayerRobot().getCurrentTile().setOccupiedBy(player.getPlayerRobot());

                            this.getSession().broadcastPositionUpdate(player.getController().getPlayerID(), targetCoordinate.getX(), targetCoordinate.getY());
                        } else {
                            l.debug("Push Panel is not activated.");
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
        this.getSession().broadcastAnimation(EAnimation.GEAR);

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

                    this.getSession().broadcastRotationUpdate(player.getController().getPlayerID(), rotationalDirection);
                }
            }
        }
    }

    /**
     * The following method checks the course for lasers and passes the respective tile including its field types to
     * the handleLaserByDirection method.
     */
    private void findLasers() {
        addDelay(2000);
        this.getSession().broadcastAnimation(EAnimation.WALL_SHOOTING);

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
        addDelay(2000);
        this.getSession().broadcastAnimation(EAnimation.PLAYER_SHOOTING);

        for(Player player : players) {
            Robot playerRobot = player.getPlayerRobot();
            Tile robotTile = playerRobot.getCurrentTile();

            int robotTileXCoordinate = robotTile.getCoordinate().getX();
            int robotTileYCoordinate = robotTile.getCoordinate().getY();
            String robotDirection = playerRobot.getDirection();

            switch(robotDirection) {
                case "top" -> handleLaserShooting("top", 1, robotTileXCoordinate, robotTileYCoordinate -1 , 0, -1);
                case "right" -> handleLaserShooting("right", 1, robotTileXCoordinate +1  , robotTileYCoordinate, 1, 0);
                case "bottom" -> handleLaserShooting("bottom", 1, robotTileXCoordinate, robotTileYCoordinate + 1  , 0, 1);
                case "left" -> handleLaserShooting("left", 1, robotTileXCoordinate - 1 , robotTileYCoordinate, -1, 0);
            }

            if (playerRobot.getCanShootBackward()) {
                switch (robotDirection) {
                    case "top" -> handleLaserShooting("bottom", 1, robotTileXCoordinate, robotTileYCoordinate - 1, 0, -1);
                    case "right" -> handleLaserShooting("left", 1, robotTileXCoordinate + 1, robotTileYCoordinate, 1, 0);
                    case "bottom" -> handleLaserShooting("top", 1, robotTileXCoordinate, robotTileYCoordinate + 1, 0, 1);
                    case "left" -> handleLaserShooting("right", 1, robotTileXCoordinate - 1, robotTileYCoordinate, -1, 0);
                }
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
                        l.debug(player.getController().getName() + " got hit by a laser at " + tile.getCoordinate());

                        if(this.spamCardDeck.size() >= laserCount) {
                            for(int i=0; i<laserCount; i++) {
                                player.getDiscardPile().add(this.spamCardDeck.remove(0));
                            }
                            if (player.getController() instanceof PlayerController pc) {
                                String[] spamArray = new String[laserCount];
                                Arrays.fill(spamArray, "Spam");
                                new DrawDamageModel(pc.getClientInstance(), player.getController().getPlayerID(), spamArray).send();
                            } else {
                                l.error("Agent draw damage not implemented yet.");
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


                        if ((laserOrientation.equals("top") && wallOrientation.equals("top")) ||
                                (laserOrientation.equals("bottom") && wallOrientation.equals("bottom")) ||
                                (laserOrientation.equals("right") && wallOrientation.equals("right")) ||
                                (laserOrientation.equals("left") && wallOrientation.equals("left"))) {

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
                    if(currentRegisterIndex == 4) {
                        if(energyBank > 0) {
                            player.setEnergyCollected(currentEnergy + 1);
                            energyBank -= 1;
                        }
                    } else if(availableEnergy > 0) {
                        player.setEnergyCollected(currentEnergy + 1);
                    }

                    this.getSession().broadcastEnergyUpdate(player.getController().getPlayerID(), player.getEnergyCollected(), "EnergySpace");

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

                    this.getSession().broadcastCheckPointReached(player.getController().getPlayerID(), player.getCheckpointsCollected());

                    if(player.getCheckpointsCollected() == availableCheckPoints) {
                        l.debug("Collected checkpoints: " + player.getCheckpointsCollected() +
                                ", Checkpoints needed: " + availableCheckPoints);
                        l.debug("Player has collected last checkpoint.");
                        endGame(player);
                        this.getSession().broadcastGameFinish(player.getController().getPlayerID());
                    }
                }
            }
        }
    }

    public void endGame(Player winner) {
        getSession().handleGameFinished(winner.getController().getPlayerID());
        if (this.activationPhaseThread != null)
        {
            this.activationPhaseThread.interrupt();
            this.activationPhaseThread = null;
        }
        return;
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
                l.debug("Resetting register {} for player {}.", i + 1, player.getController().getName());
            }
        }

        for(Player player : players) {
            player.shuffleAndRefillDeck();
            l.debug("Shuffling and refilling deck for player {}.", player.getController().getName());
        }

        for (Player player : players) {
            if (player.getHasAdminPrivilegeUpgrade()) {
                player.setChosenRegisterAdminPrivilegeUpgrade(null);
                l.debug("Player {} reset chosenRegisterAdminPrivilegeUpgrade to null.", player.getController().getName());
            }
        }
    }

    // endregion Activation Phase Helpers

    /**
     * One run of a register in the activation phase. This method must
     * be called five times in a row to complete the activation phase.
     * @return True if the activation phase should continue. False otherwise.
     */
    private boolean runActivationPhase() throws InterruptedException
    {
        l.debug("Starting register phase {}.", this.currentRegisterIndex + 1);


        this.determinePriorities();
        this.sortPlayersByPriorityInDesc();
        this.getSession().broadcastCurrentCards(this.currentRegisterIndex);

        for (Player p : this.players) {
            if (p.getRegisters()[this.currentRegisterIndex] != null) {
                l.info("Player {} is playing card {}.", p.getController().getPlayerID(), p.getRegisters()[this.currentRegisterIndex].getCardType());
                p.getRegisters()[this.currentRegisterIndex].playCard(p, this.currentRegisterIndex);
                Thread.sleep(Types.EDelay.CARD_PLAY.i);
                continue;
            }

            l.warn("Player {} does not have a card in register {}. If this was after a reboot, this can be ignored.", p.getController().getPlayerID(), this.currentRegisterIndex + 1);
        }

        addDelay(2000);
        this.activateConveyorBelts();
        this.activatePushPanels();
        this.activateGears();
        this.findLasers();
        this.shootRobotLasers();
        this.checkEnergySpaces();
        this.checkCheckpoints();
        this.moveCheckpoints();
        this.currentRegisterIndex++;

        return this.currentRegisterIndex < GameMode.REGISTER_PHASE_COUNT;
    }

    /**
     * The following method introduces a timeout. The length depends on the amount of milliseconds passed to the method.
     * @param milliseconds length of timeout
     *
     * @deprecated DO NOT USE. This method can easily crash the server. Marked as deprecated to prevent further usage.
     */
    public void addDelay(int milliseconds) {
        try {
            this.activationPhaseThread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void triggerActivationPhase()
    {
        for (Player p : players) {
            l.debug("P Player {} - Cards in register {}.",
                    p.getController().getName(),
                    p.getRegistersAsStringArray());
        }

        if (this.activationPhaseThread != null && this.activationPhaseThread.isAlive())
        {
            l.warn("Activation Phase is already running. Skipping . . .");
            return;
        }

        this.activationPhaseThread = this.createActivationThread();
        this.activationPhaseThread.start();

        return;
    }

    /**
     * Interface for a new phase in the game. All phase updates should be called through this method.
     *
     * @param phase New phase to be set
     */
    public void handleNewPhase(EGamePhase phase) {
        l.info("Session [{}] is entering a new phase. From {} to {}.", this.getSession().getSessionID(), this.gamePhase, phase);

        this.getSession().broadcastNewGamePhase(phase);

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
    public void startProgrammingTimerService()
    {
        this.getSession().broadcastProgrammingTimerStart();

        if (this.programmingPhaseTimerService != null && this.programmingPhaseTimerService.isAlive())
        {
            l.error("Programming Phase Timer Service is already running.");
            EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
            return;
        }

        this.programmingPhaseTimerService = new Thread(() ->
        {
            final long startTime = System.currentTimeMillis();
            try
            {
                Thread.sleep(30_000);
            }
            catch (final InterruptedException e)
            {
                l.info("All Players have set their register cards in time. Time left [{}s].Starting next phase . . ." , ( (double) 30_000 - (System.currentTimeMillis() - startTime) ) / 1_000);
            }

            l.info("Programming Phase Timer Service was interrupted or finished waiting successfully.");

            this.executePostProgrammingPhaseTimerServiceBehavior();

            return;
        });

        this.programmingPhaseTimerService.setName(String.format("ProgrammingPhaseTimerService-%s", this.getSession().getSessionID()));
        this.programmingPhaseTimerService.start();

        return;
    }

    public void executePostProgrammingPhaseTimerServiceBehavior()
    {
        if (this.getUnfinishedPlayersDuringProgrammingPhase().length > 0)
        {
            l.info("The following players did not finish their programming phase in time: {}.", Arrays.toString(this.getUnfinishedPlayersDuringProgrammingPhase()));
            this.getSession().broadcastProgrammingTimerFinish(this.getUnfinishedPlayersDuringProgrammingPhase());
            this.discardAndDrawBlind(Arrays.stream(this.getUnfinishedPlayersDuringProgrammingPhase()).mapToObj(i -> Objects.requireNonNull(this.getSession().getOwnershipableByID(i)).getPlayer()).collect(Collectors.toCollection(ArrayList::new)));
        }

        this.programmingPhaseTimerService = null;

        this.handleNewPhase(EGamePhase.ACTIVATION);

        return;
    }

    private int[] getUnfinishedPlayersDuringProgrammingPhase()
    {
        ArrayList<Integer> unfinishedPlayers = new ArrayList<Integer>();
        for (final Player p : this.players)
        {
            if (!p.hasPlayerFinishedProgramming())
            {
                unfinishedPlayers.add(p.getController().getPlayerID());
                continue;
            }

            continue;
        }

        return unfinishedPlayers.stream().mapToInt(i -> i).toArray();
    }

    public void discardAndDrawBlind(final ArrayList<Player> players)
    {
        for (final Player p : players)
        {
            p.executeIncompleteProgrammingBehavior();
            continue;
        }

        return;
    }

    /**
     * The following method is called whenever a card in a register needs to be replaced by
     * another card (by default, top card of player deck).
     * @param player player who needs to replace their cards
     * @param card card that will be added instead
     */
    public void replaceCardInRegister(Player player, IPlayableCard card) {
        player.getDiscardPile().add(player.getCardByRegisterIndex(currentRegisterIndex));
        player.getRegisters()[currentRegisterIndex] = null;

        IPlayableCard topCardFromDiscardPile = player.getPlayerDeck().get(0);
        String newCard = ((Card) topCardFromDiscardPile).getCardType();
        player.setCardInRegister(currentRegisterIndex, topCardFromDiscardPile);

        this.getSession().broadcastReplacedCard(player.getController().getPlayerID(), currentRegisterIndex, newCard);
    }

    /* TODO Remove player after connection loss */
    public void removePlayer(final int playerID)
    {
        l.error("Removing player with ID {} from game. Not implemented yet.", playerID);
        return;
    }

    public void onClose() throws InterruptedException
    {
        if (this.activationPhaseThread != null)
        {
            this.activationPhaseThread.interrupt();
            this.activationPhaseThread.join();
            this.activationPhaseThread = null;
        }

        l.debug("Game Mode of Session [{}] closed successfully.", this.getSession().getSessionID());

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
        return switch (courseName) {
            case "Dizzy Highway", "DizzyHighway" -> 1;
            case "Extra Crispy", "ExtraCrispy", "Lost Bearings", "LostBearings" -> 4;
            case "Death Trap", "DeathTrap" -> 5;
            default -> 0;
        };
    }

    public PlayerController[] getRemotePlayers()
    {
       return this.gameState.getSession().getRemotePlayers().toArray(new PlayerController[0]);
    }

    public IOwnershipable[] getControllers()
    {
        return this.gameState.getControllers();
    }

    public Session getSession()
    {
        return this.gameState.getSession();
    }

    public int getEnergyBank() {
        return energyBank;
    }

    public void setEnergyBank(int energyBank) {
        this.energyBank = energyBank;
    }

    private Thread createActivationThread()
    {

        final Thread t = new Thread(
        () ->
        {
            l.debug("Activation Phase started.");

            this.currentRegisterIndex = 0;

            try
            {
                while (this.runActivationPhase())
                {
                    l.debug("Register {} in Activation Phase ended. Waiting 5s for the next register iteration . . .", this.currentRegisterIndex);

                    //noinspection BusyWait
                    Thread.sleep(Types.EDelay.REGISTER_PHASE_ITERATION.i);

                    continue;
                }

                this.endRound();

                l.debug("Activation Phase ended successfully. Waiting 2s for the next phase . . .");

                Thread.sleep(Types.EDelay.PHASE_CHANGE.i);
            }
            catch (final InterruptedException e)
            {
                l.warn("Activation Phase was interrupted. If this was during session close, this can be ignored.");
                l.warn(e.getMessage());
                return;
            }

            this.handleNewPhase(EGamePhase.UPGRADE);
            this.activationPhaseThread = null;

            return;
        });

        t.setName(String.format("PhaseIIIService-%s", this.getSession().getSessionID()));

        return t;
    }

    private void evaluateUpgradePhasePriorities()
    {
        l.debug("Determining priority for all players during this upgrade phase.");
        this.upgradePhasePlayersSortedByPriority.clear();

        final Coordinate antennaLocation = this.course.getPriorityAntennaCoordinate();

        final int[] distances = new int[this.players.size()];
        for (int i = 0; i < this.players.size(); ++i)
        {
            final Coordinate    robotCoordinate     = this.players.get(i).getPlayerRobot().getCurrentTile().getCoordinate();
                                distances[i]        = Math.abs(antennaLocation.getX() - robotCoordinate.getX()) + Math.abs(antennaLocation.getY() - robotCoordinate.getY());
            continue;
        }

        int currentPriority = this.players.size();
        for (int i = 0; i < this.players.size(); ++i)
        {
            int     minDistance     = Integer.MAX_VALUE;
            int     minIndex        = -1;

            for (int j = 0; j < distances.length; ++j)
            {
                if (distances[j] < minDistance)
                {
                    minDistance     = distances[j];
                    minIndex        = j;
                }

                continue;
            }

            //Now, includes AdminPrivilege logic. Still needs to be tested once shop is available.
            if (minIndex == -1)
            {
                l.fatal("Could not determine priority for upgrade phase.");
                EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
                return;
            }

            final Player currentPlayer = this.players.get(minIndex);

            this.upgradePhasePlayersSortedByPriority.add(currentPlayer);
            currentPriority--;
            distances[minIndex] = Integer.MAX_VALUE;

            continue;
        }

        l.debug("Upgrade Phase priorities determined: {}.", this.upgradePhasePlayersSortedByPriority);

        return;
    }

    private boolean isUpgradeShopRightSize()
    {
        /* The shop must always have the size of all controllers connected. */
        return this.players.size() == this.upgradeShop.size();
    }

    private boolean doesUpgradeShopContains(final String card)
    {
        for (final AUpgradeCard upgradeCard : this.upgradeShop)
        {
            if (upgradeCard.getCardType().equals(card))
            {
                return true;
            }

            continue;
        }

        return false;
    }

    private int getUpgradeCardCost(final String card)
    {
        for (final AUpgradeCard upgradeCard : this.upgradeShop)
        {
            if (upgradeCard.getCardType().equals(card))
            {
                return upgradeCard.getCost();
            }

            continue;
        }

        l.error("Could not find cost for upgrade card {}.", card);

        return -1;
    }

    public boolean isProgrammingTimerServiceRunning()
    {
        return this.programmingPhaseTimerService != null && this.programmingPhaseTimerService.isAlive();
    }

    public Thread getProgrammingTimerService()
    {
        return this.programmingPhaseTimerService;
    }

    // endregion Getters and Setters

}
