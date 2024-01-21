package sep.server.model.game.cards.upgrade.PermanentUpgrade;

import sep.server.model.game.Player;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

/**
 * Represents an abstract permanent upgrade card.
 * Permanent upgrades have a cost and provide lasting effects when activated.
 */
public abstract class APermanentUpgrade extends AUpgradeCard {

    public APermanentUpgrade(String cardType, int cost) {
        super(cardType, cost);
    }

    @Override
    public void activateUpgrade(Player player) {}
}
