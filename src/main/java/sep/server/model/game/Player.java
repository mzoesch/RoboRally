package sep.server.model.game;

import java.util.ArrayList;
import java.util.Collections;

import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

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
}
