package sep.server.model.game.cards.upgrade.PermanentUpgrade;

import sep.server.model.game.Player;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

public abstract class APermanentUpgrade extends AUpgradeCard {
    public APermanentUpgrade(String cardType, int cost) {
        super(cardType, cost);
    }

    @Override
    public void activateUpgrade(Player player) {}
}
