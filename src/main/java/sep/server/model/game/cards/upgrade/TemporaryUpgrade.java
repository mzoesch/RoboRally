package sep.server.model.game.cards.upgrade;

public class TemporaryUpgrade extends AUpgradeCard {

  public TemporaryUpgrade(String cardType, int cost) {
    super(cardType, cost);
  }

  @Override
  public void activateUpgrade() {}
}
