package sep.server.model.game.cards.upgrade;

import sep.server.model.game.cards.Card;

public abstract class AUpgradeCard extends Card {
  int cost;

  public AUpgradeCard(String cardType, int cost) {
    super(cardType);
    this.cost = cost;
  }

  public int getCost() {
    return cost;
  }
  public abstract void activateUpgrade();
}
