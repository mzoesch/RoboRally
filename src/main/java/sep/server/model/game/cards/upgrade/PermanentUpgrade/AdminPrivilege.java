package sep.server.model.game.cards.upgrade.PermanentUpgrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.Player;

public class AdminPrivilege extends APermanentUpgrade{

    private static final Logger logger = LogManager.getLogger(AdminPrivilege.class);

    public AdminPrivilege(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "AdminPrivilege";
        this.cost = 3;
    }

    @Override
    public void activateUpgrade(Player player) {

        logger.info("Update Card (AdminPrivilege) activated for player '{}'. Setting hasAdminPrivilegeUpgrade to true.",
                player.getController().getName());

        player.setHasAdminPrivilegeUpgrade(true);
    }
}
