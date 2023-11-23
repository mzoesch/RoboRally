package sep.server.model.game;

import java.util.ArrayList;
import java.util.Collections;

import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.upgrade.UpgradeCard;

public class Player {
  String playerName;
  int playerID;
  int playerScore;
  Robot playerRobot;
  ArrayList<Card> playerDeck;
  ArrayList<Card> discardPile;
  Card[] registers;
  ArrayList<EnergyCube> energyCollected;
  ArrayList<UpgradeCard> upgradeCards;

  public Player(String playerName, int playerID, int playerScore, Robot playerRobot,
                ArrayList<Card> playerDeck, ArrayList<Card> discardPile, Card[] registers,
                ArrayList<EnergyCube> energyCollected, ArrayList<UpgradeCard> upgradeCards) {
    this.playerName = playerName;
    this.playerID = playerID;
    this.playerScore = playerScore;
    this.playerRobot = playerRobot;
    this.playerDeck = playerDeck;
    this.discardPile = discardPile;
    this.registers = registers;
    this.energyCollected = energyCollected;
    this.upgradeCards = upgradeCards;
  }

  public String getPlayerName() {
    return playerName;
  }

  public void setPlayerName(String playerName) {
    this.playerName = playerName;
  }

  public int getPlayerID() {
    return playerID;
  }

  public void setPlayerID(int playerID) {
    this.playerID = playerID;
  }

  public int getPlayerScore() {
    return playerScore;
  }

  public void setPlayerScore(int playerScore) {
    this.playerScore = playerScore;
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

  public ArrayList<EnergyCube> getEnergyCollected() {
    return energyCollected;
  }

  public void setEnergyCollected(ArrayList<EnergyCube> energyCollected) {
    this.energyCollected = energyCollected;
  }

  public ArrayList<UpgradeCard> getUpgradeCards() {
    return upgradeCards;
  }

  public void setUpgradeCards(ArrayList<UpgradeCard> upgradeCards) {
    this.upgradeCards = upgradeCards;
  }

  public void shuffleAndRefillDeck() {
    Collections.shuffle(discardPile);
    playerDeck.addAll(playerDeck.size(), discardPile); // Refill playerDeck with the shuffled discardPile at the end of PlayerDeck
    discardPile.clear();
  }
}
