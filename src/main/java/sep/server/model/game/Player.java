package sep.server.model.game;

import java.util.ArrayList;
import java.util.Collections;

import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.tiles.Coordinate;
import sep.server.viewmodel.PlayerController;

public class Player {
  PlayerController playerController;
  Robot playerRobot;
  ArrayList<IPlayableCard> playerDeck;
  ArrayList<IPlayableCard> discardPile;
  ArrayList<IPlayableCard> playerHand;
  IPlayableCard[] registers;
  int priority;
  int checkpointsCollected;
  int energyCollected;
  ArrayList<AUpgradeCard> upgradeCards;

  public Player(Robot playerRobot,
                ArrayList<IPlayableCard> playerDeck, ArrayList<IPlayableCard> discardPile, IPlayableCard[] registers, int priority,
                int checkpointsCollected, int energyCollected, ArrayList<AUpgradeCard> upgradeCards) {
    this.playerRobot = playerRobot;
    this.playerDeck = playerDeck;
    this.discardPile = discardPile;
    this.registers = registers;
    this.priority = priority;
    this.checkpointsCollected = checkpointsCollected;
    this.energyCollected = energyCollected;
    this.upgradeCards = upgradeCards;
  }

  public PlayerController getPlayerController() {
    return playerController;
  }

  public void setPlayerController(PlayerController playerController) {
    this.playerController = playerController;
  }

  public Robot getPlayerRobot() {
    return playerRobot;
  }

  public void setPlayerRobot(Robot playerRobot) {
    this.playerRobot = playerRobot;
  }

  public ArrayList<IPlayableCard> getPlayerDeck() {
    return playerDeck;
  }

  public void setPlayerDeck(ArrayList<IPlayableCard> playerDeck) {
    this.playerDeck = playerDeck;
  }

  public ArrayList<IPlayableCard> getDiscardPile() {
    return discardPile;
  }

  public void setDiscardPile(ArrayList<IPlayableCard> discardPile) {
    this.discardPile = discardPile;
  }
  public ArrayList<IPlayableCard> getPlayerHand() {
    return playerHand;
  }

  public void setPlayerHand(ArrayList<IPlayableCard> playerHand) {
    this.playerHand = playerHand;
  }


  public IPlayableCard[] getRegisters() {
    return registers;
  }

  public void setRegisters(IPlayableCard[] registers) {
    this.registers = registers;
  }

  public IPlayableCard getCardInRegister(int registerIndex) {
      return registers[registerIndex];
  }

  public void setCardInRegister(int registerIndex, IPlayableCard newCard) {
    IPlayableCard previousCard = registers[registerIndex];

    if(previousCard != null) {
      discardPile.add(previousCard);
    }

    registers[registerIndex] = null;
    registers[registerIndex] = newCard;
  }

  public int getPriority() {
    return priority;
  }

  public void setPriority(int priority) {
    this.priority = priority;
  }

  public int getCheckpointsCollected() {
    return checkpointsCollected;
  }

  public void setCheckpointsCollected(int checkpointsCollected) {
    this.checkpointsCollected = checkpointsCollected;
  }

  public int getEnergyCollected() {
    return energyCollected;
  }

  public void setEnergyCollected(int energyCollected) {
    this.energyCollected = energyCollected;
  }

  public ArrayList<AUpgradeCard> getUpgradeCards() {
    return upgradeCards;
  }

  public void setUpgradeCards(ArrayList<AUpgradeCard> upgradeCards) {
    this.upgradeCards = upgradeCards;
  }

  public void shuffleAndRefillDeck() {
    Collections.shuffle(discardPile);
    playerDeck.addAll(playerDeck.size(), discardPile); // Refill playerDeck with the shuffled discardPile at the end of PlayerDeck
    discardPile.clear();
  }

  /**
   * Converts the player's hand of playable cards into a String array.
   *
   * This method is necessary for creating an object of the CardsYouGotNowModel class
   */
  public String[] getPlayerHandAsStringArray() {
    ArrayList<IPlayableCard> hand = this.getPlayerHand();
    String[] handArray = new String[hand.size()];
    for (int i = 0; i < hand.size(); i++) {
      handArray[i] = hand.get(i).toString();
    }
    return handArray;
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
      case "NORTH":
        newCoordinate = new Coordinate(currentCoordinate.getXCoordinate(), currentCoordinate.getYCoordinate() - directionModifier);
        break;
      case "SOUTH":
        newCoordinate = new Coordinate(currentCoordinate.getXCoordinate(), currentCoordinate.getYCoordinate() + directionModifier);
        break;
      case "EAST":
        newCoordinate = new Coordinate(currentCoordinate.getXCoordinate() + directionModifier, currentCoordinate.getYCoordinate());
        break;
      case "WEST":
        newCoordinate = new Coordinate(currentCoordinate.getXCoordinate() - directionModifier, currentCoordinate.getYCoordinate());
        break;
      default:
        break;
    }

    // Check if the robot is still on the board
    if (!course.isCoordinateWithinBounds(newCoordinate)) {
      robot.reboot();
      return;
    }

    // Check if the move is possible
    if (!robot.isMovable(course.getTileByCoordinate(newCoordinate))) {
      return;
    }

    // Update Robot Position in Course and in Robot
    course.updateRobotPosition(robot, newCoordinate);
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

  public void addCardToRegister(IPlayableCard card, int position) {
    if (position >= 0 && position < 4) {
      registers[position] = card;
      //session.sendKartenAuswahlBestätigen();
      checkRegisterStatus();
    }
  }

  public void checkRegisterStatus() {
    boolean isFull = true;
    for (IPlayableCard card : registers) {
      if (card == null) {
        isFull = false;
        break;
      }
    }
    if (isFull) {
      //session.sendAuswahlBeenden();
    }
  }






}
