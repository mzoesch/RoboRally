package sep.server.model.game;

import java.util.ArrayList;
import java.util.Collections;

import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;
import sep.server.model.game.tiles.Coordinate;

public class Player {
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

  public void moveRobotOneTile() {
    Robot robot = getPlayerRobot();
    Course course = robot.getCourse();
    Tile currentTile = robot.getCurrentTile();
    Coordinate currentCoordinate = currentTile.getCoordinate();
    String currentDirection = robot.getDirection();
    Coordinate newCoordinate = null;

    switch (currentDirection) {
      case "NORTH":
        newCoordinate = new Coordinate(currentCoordinate.getXCoordinate(), currentCoordinate.getYCoordinate() - 1);
        break;
      case "SOUTH":
        newCoordinate = new Coordinate(currentCoordinate.getXCoordinate(), currentCoordinate.getYCoordinate() + 1);
        break;
      case "EAST":
        newCoordinate = new Coordinate(currentCoordinate.getXCoordinate() + 1, currentCoordinate.getYCoordinate());
        break;
      case "WEST":
        newCoordinate = new Coordinate(currentCoordinate.getXCoordinate() - 1, currentCoordinate.getYCoordinate());
        break;
      default:
        break;
    }

    if (isValidMove(newCoordinate)) {
      Tile newTile = course.getTileByCoordinate(newCoordinate);
      currentTile.setRobot(null);
      newTile.setRobot(robot);
      robot.setCurrentTile(newTile);
    }
  }

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

  public boolean isValidMove (Coordinate coordinate){
    return true;
  }
}
