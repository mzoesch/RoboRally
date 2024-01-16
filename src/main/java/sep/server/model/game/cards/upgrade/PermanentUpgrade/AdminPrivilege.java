package sep.server.model.game.cards.upgrade.PermanentUpgrade;

import sep.server.model.game.Player;

public class AdminPrivilege extends APermanentUpgrade{

    public AdminPrivilege(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "AdminPrivilege";
        this.cost = 3;
    }

    @Override
    public void activateUpgrade(Player player) {



    }
}
