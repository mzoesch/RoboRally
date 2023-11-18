package sep.server.model.game.cards.upgrade;

public class PermanentUpgrade extends UpgradeCard {
    public PermanentUpgrade(String cardType, boolean isProgrammable, int cost) {
        super(cardType, isProgrammable, cost);
    }

    @Override
    public void activateUpgrade() {}
}
