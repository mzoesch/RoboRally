package sep.server.model.game.cards.upgrade.TemporaryUpgrade;

import sep.server.model.game.Player;

public class MemorySwap extends ATemporaryUpgrade{

    public MemorySwap(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "MemorySwap";
        this.cost = 1;
    }

    public void activateUpgrade(Player player) {

    }
}
