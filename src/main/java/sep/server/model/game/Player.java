package sep.server.model.game;

import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.tiles.Coordinate;
import sep.server.model.game.builder.DeckBuilder;
import sep.server.viewmodel.PlayerController;
import sep.server.viewmodel.Session;

import java.util.ArrayList;
import java.util.Collections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Player
{
    private static final Logger l = LogManager.getLogger(Player.class);

    private int priority; //TODO aktuell überflüssig oder?; falls nicht, muss sie im Konstruktor gesetzt werden

    private final Session session;
    private final PlayerController playerController;
    private final Robot playerRobot;

    private final ArrayList<IPlayableCard> playerDeck;
    private final ArrayList<IPlayableCard> discardPile;
    private final ArrayList<AUpgradeCard> upgradeCards;
    private final IPlayableCard[] registers;
    private final ArrayList<IPlayableCard> playerHand;
    private int energyCollected;
    private int checkpointsCollected;



    private GameMode gameMode;

    public Player(final PlayerController playerController, final Course currentCourse, final Session session)
    {
        this.session = session;
        this.playerController = playerController;
        this.playerRobot = new Robot(currentCourse);

        this.playerDeck = new DeckBuilder().buildProgrammingDeck();
        Collections.shuffle(this.playerDeck);
        this.discardPile = new ArrayList<>();
        this.upgradeCards = new ArrayList<>();
        this.registers = new IPlayableCard[5];
        this.playerHand = new ArrayList<>();

        this.energyCollected = GameMode.STARTING_ENERGY;
        this.checkpointsCollected = 0;

        this.gameMode = session.getGameState().getAuthGameMode();

        return;
    }

    public void shuffleAndRefillDeck() {
        Collections.shuffle(discardPile);
        playerDeck.addAll(playerDeck.size(), discardPile); // Refill playerDeck with the shuffled discardPile at the end of PlayerDeck
        discardPile.clear();
    }

    /**
    * Moves the robot one tile based on the given direction.
    * Updates the robot's position.
    *
    * @param forward True if the robot should move forwards, false if backwards.
    */
    public void moveRobotOneTile(boolean forward) {
        Robot robot = getPlayerRobot();
        Course course = robot.getCourse();
        String currentDirection = robot.getDirection();
        Tile currentTile = robot.getCurrentTile();
        Coordinate currentCoordinate = currentTile.getCoordinate();
        Coordinate newCoordinate = null;

        int directionModifier = forward ? 1 : -1;

        switch (currentDirection) {
            case "NORTH", "top":
                newCoordinate = new Coordinate(currentCoordinate.getXCoordinate(), currentCoordinate.getYCoordinate() - directionModifier);
                break;
            case "SOUTH", "bottom":
                newCoordinate = new Coordinate(currentCoordinate.getXCoordinate(), currentCoordinate.getYCoordinate() + directionModifier);
                break;
            case "EAST", "right":
                newCoordinate = new Coordinate(currentCoordinate.getXCoordinate() + directionModifier, currentCoordinate.getYCoordinate());
                break;
            case "WEST", "left":
                newCoordinate = new Coordinate(currentCoordinate.getXCoordinate() - directionModifier, currentCoordinate.getYCoordinate());
                break;
            default:
                break;
        }

        if (newCoordinate == null) {
            l.error("Player {}'s robot has an invalid direction: {}", this.getPlayerController().getPlayerID(), currentDirection);
            return;
        }

        // Check if the robot is still on the board
        if (!course.isCoordinateWithinBounds(newCoordinate)) {
            l.debug("Player {}'s robot moved to {} and fell off the board. Rebooting . . .", this.getPlayerController().getPlayerID(), newCoordinate.toString());
            robot.reboot();
            return;
        }

        // Check if the move is possible
        if (!robot.isMovable(course.getTileByCoordinate(newCoordinate))) {
            l.debug("Player {}'s robot wanted to move to an unmovable tile [from {} to {}]. Ignoring.", this.getPlayerController().getPlayerID(), currentCoordinate.toString(), newCoordinate.toString());
            return;
        }

        // Update Robot Position in Course and in Robot
        course.updateRobotPosition(robot, newCoordinate);
        l.debug("Player {}'s robot moved to ({}, {}).", this.getPlayerController().getPlayerID(), newCoordinate.getXCoordinate(), newCoordinate.getYCoordinate());

        return;
    }

    /**
    * Moves the robot one tile forwards based on the robot's current direction.
    * Updates the robot's position.
    */
    public void moveRobotOneTileForwards() {
        moveRobotOneTile(true);
    }

    /**
    * Moves the robot one tile backwards based on the robot's current direction.
    * Updates the robot's position.
    */
    public void moveRobotOneTileBackwards() {
        moveRobotOneTile(false);
    }

    /**
    * Rotates the robot 90 degrees to the right
    * Updates the robot's direction
    */
    public void rotateRobotOneTileToTheRight(){
        Robot robot = getPlayerRobot();
        String currentDirection = robot.getDirection();
        String newDirection;

        switch (currentDirection) {
            case "NORTH":
                newDirection = "EAST";
                break;
            case "EAST":
                newDirection = "SOUTH";
                break;
            case "SOUTH":
                newDirection = "WEST";
                break;
            case "WEST":
                newDirection = "NORTH";
                break;
            default:
                newDirection = currentDirection;
                break;
        }

        robot.setDirection(newDirection);
    }

    /**
     * Adds a playable card to the specified register position.
     * If all registers are full after the addition, it notifies the session that the selection is finished. If all
     * players have finished their selection, the next phase will be started.
     *
     * @param card Name of the card to be added to the register
     * @param pos  Position of the register to add the card to (zero-based)
     */
    public void setCardToRegister(final String card, final int pos)
    {
        if (this.hasPlayerFinishedProgramming())
        {
            l.warn("Player {} has already finished programming and, therefore, cannot change their programming registers anymore.", this.playerController.getPlayerName());
            return;
        }

        if (pos < 0 || pos > 4)
        {
            l.error("Invalid register position: " + pos);
            return;
        }

        final IPlayableCard playableCard = this.getCardByName(card);

        if (playableCard == null)
        {
            this.registers[pos] = null;
            this.session.sendCardSelected(this.playerController.getPlayerID(), pos, false);
            return;
        }

        this.registers[pos] = playableCard;
        this.session.sendCardSelected(getPlayerController().getPlayerID(), pos, true);

        if (this.hasPlayerFinishedProgramming())
        {
            l.debug("Player " + this.playerController.getPlayerName() + " has finished programming.");
            this.session.sendSelectionFinished(this.playerController.getPlayerID());

            if (this.session.haveAllPlayersFinishedProgramming())
            {
                l.debug("All players have finished programming in time. Interrupting timer.");
                // TODO Interrupt timer
                this.session.getGameState().getAuthGameMode().handleNewPhase(EGamePhase.ACTIVATION);
                return;
            }

            this.session.getGameState().getAuthGameMode().startTimer();
        }

        return;
    }

    public void handleIncompleteProgramming() {

        discardPile.addAll(playerHand);
        playerHand.clear();
        shuffleAndRefillDeck();

        for (int i = 0; i < registers.length; i++) {
            if (registers[i] == null) {
                registers[i] = playerDeck.remove(0);
            }
        }
        session.sendCardsYouGotNow(getPlayerController(), getRegistersAsStringArray());
    }

    // region Getters and Setters

    /**
     * @return True if all registers are full, false otherwise
     */
    public boolean hasPlayerFinishedProgramming()
    {
        for (final IPlayableCard c : this.registers)
        {
            if (c == null)
            {
                return false;
            }

            continue;
        }

        return true;
    }

    public IPlayableCard getCardByName(final String cardName)
    {
        for (IPlayableCard c : this.playerHand)
        {
            if (c.getCardType().equals(cardName))
            {
                return c;
            }

            continue;
        }

        return null;
    }

    public PlayerController getPlayerController()
    {
        return this.playerController;
    }

    public Robot getPlayerRobot()
    {
        return this.playerRobot;
    }

    public ArrayList<IPlayableCard> getPlayerDeck()
    {
        return this.playerDeck;
    }

    public ArrayList<IPlayableCard> getDiscardPile()
    {
        return this.discardPile;
    }

    public ArrayList<IPlayableCard> getPlayerHand()
    {
        return this.playerHand;
    }

    public IPlayableCard[] getRegisters()
    {
        return this.registers;
    }

    public IPlayableCard getCardByRegisterIndex(final int idx)
    {
        return this.registers[idx];
    }

    public String[] getRegistersAsStringArray()
    {
        final String[] registersArray = new String[this.registers.length];
        for (int i = 0; i < this.registers.length; i++)
        {
            registersArray[i] = this.registers[i].getCardType();
        }

        return registersArray;
    }

    public String[] getPlayerHandAsStringArray()
    {
        final String[] handArray = new String[this.playerHand.size()];
        for (int i = 0; i < this.playerHand.size(); i++)
        {
            handArray[i] = this.playerHand.get(i).getCardType();
        }

        return handArray;
    }

    public void setCardInRegister(final int idx, final IPlayableCard newCard)
    {
        if (this.registers[idx] != null)
        {
            this.discardPile.add(this.registers[idx]);
        }

        this.registers[idx] = newCard;

        return;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(final int priority)
    {
        this.priority = priority;
        return;
    }

    public int getCheckpointsCollected()
    {
        return checkpointsCollected;
    }

    public void setCheckpointsCollected(final int checkpointsCollected)
    {
        this.checkpointsCollected = checkpointsCollected;
        return;
    }
    public GameMode getGameMode() {
        return gameMode;
    }

    public int getEnergyCollected()
    {
        return energyCollected;
    }

    public void setEnergyCollected(int energyCollected)
    {
        this.energyCollected = energyCollected;
    }

    public ArrayList<AUpgradeCard> getUpgradeCards()
    {
        return upgradeCards;
    }

    // endregion Getters and Setters

}
