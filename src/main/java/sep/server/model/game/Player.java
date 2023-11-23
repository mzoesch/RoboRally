package sep.server.model.game;

import java.util.ArrayList;
import java.util.Collections;

import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

public class Player {
  Robot playerRobot;
  ArrayList<Card> playerDeck;
  ArrayList<Card> discardPile;
  Card[] registers;
  int checkpointsCollected;
  int energyCollected;
  ArrayList<AUpgradeCard> upgradeCards;

  public Player(Robot playerRobot,
                ArrayList<Card> playerDeck, ArrayList<Card> discardPile, Card[] registers,
                int checkpointsCollected, int energyCollected, ArrayList<AUpgradeCard> upgradeCards) {
    this.playerRobot = playerRobot;
    this.playerDeck = playerDeck;
    this.discardPile = discardPile;
    this.registers = registers;
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

  public ArrayList<Card> getPlayerDeck() {
    return playerDeck;
  }

  public void setPlayerDeck(ArrayList<Card> playerDeck) {
    this.playerDeck = playerDeck;
  }

  public ArrayList<Card> getDiscardPile() {
    return discardPile;
  }

  public void setDiscardPile(ArrayList<Card> discardPile) {
    this.discardPile = discardPile;
  }

  public Card[] getRegisters() {
    return registers;
  }

  public void setRegisters(Card[] registers) {
    this.registers = registers;
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
