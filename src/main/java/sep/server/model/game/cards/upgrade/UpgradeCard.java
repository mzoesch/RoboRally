package sep.server.model.game.cards.upgrade;

import sep.server.model.game.cards.Card;

public abstract class UpgradeCard extends Card {
  int cost;

  public UpgradeCard(String cardType, boolean isProgrammable, int cost) {
    super(cardType, isProgrammable);
    this.cost = cost;
    setProgrammable(false);
  }

  public int getCost() {
    return cost;
  }
  public void setCost(int cost) {
    this.cost = cost;
  }
  public void purchaseUpgrade() {}
  public abstract void activateUpgrade();
}
