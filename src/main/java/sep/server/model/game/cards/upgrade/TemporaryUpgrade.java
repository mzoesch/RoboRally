package sep.server.model.game.cards.upgrade;

import sep.server.model.game.Player;

public class TemporaryUpgrade extends AUpgradeCard {

  public TemporaryUpgrade(String cardType, int cost) {
    super(cardType, cost);
  }

  @Override
  public void activateUpgrade(Player player) {}
}
