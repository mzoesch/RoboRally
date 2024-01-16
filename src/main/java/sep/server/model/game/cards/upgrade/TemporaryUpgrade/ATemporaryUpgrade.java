package sep.server.model.game.cards.upgrade.TemporaryUpgrade;

import sep.server.model.game.Player;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

public abstract class ATemporaryUpgrade extends AUpgradeCard {

  public ATemporaryUpgrade(String cardType, int cost) {
    super(cardType, cost);
  }

  @Override
  public void activateUpgrade(Player player) {}
}
