package sep.server.model.game.cards.upgrade;

public class TemporaryUpgrade extends UpgradeCard {
  int duration;

  public TemporaryUpgrade(String cardType, boolean isProgrammable, int cost, int duration) {
    super(cardType, isProgrammable, cost);
    this.duration = duration;
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  @Override
  public void activateUpgrade() {}
}
