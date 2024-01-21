package sep.server.model.game.cards.upgrade.TemporaryUpgrade;

import sep.server.model.game.Player;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

/**
 * Represents an abstract temporary upgrade card.
 * Permanent upgrades have a cost and provide temporary effects when activated.
 */
public abstract class ATemporaryUpgrade extends AUpgradeCard {

  public ATemporaryUpgrade(String cardType, int cost) {
    super(cardType, cost);
  }

  @Override
  public void activateUpgrade(Player player) {}

}
