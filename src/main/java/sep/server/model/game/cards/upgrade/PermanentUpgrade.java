package sep.server.model.game.cards.upgrade;

import sep.server.model.game.Player;

public class PermanentUpgrade extends AUpgradeCard {
    public PermanentUpgrade(String cardType, int cost) {
        super(cardType, cost);
    }

    @Override
    public void activateUpgrade(Player player) {}
}
