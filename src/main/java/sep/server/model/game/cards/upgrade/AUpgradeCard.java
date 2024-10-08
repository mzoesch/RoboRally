package sep.server.model.game.cards.upgrade;

import sep.server.model.game.Player;
import sep.server.model.game.cards.Card;

/**
 * Represents an abstract upgrade card.
 */
public abstract class AUpgradeCard extends Card {

  protected int cost;

  public AUpgradeCard(String cardType, int cost) {
    super(cardType);
    this.cost = cost;
  }

  @Override
  public String getCardType() {
    return super.getCardType();
  }

  @Override
  public String toString() {
    return super.getCardType();
  }

  public int getCost() {
    return cost;
  }

  public abstract void activateUpgrade(Player player);

}
