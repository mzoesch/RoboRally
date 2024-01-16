package sep.server.model.game.cards.upgrade.TemporaryUpgrade;

import sep.server.model.game.Player;

public class SpamBlocker extends ATemporaryUpgrade{

    public SpamBlocker(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "SpamBlocker";
        this.cost = 3;
    }

    public void activateUpgrade(Player player) {

    }
}
