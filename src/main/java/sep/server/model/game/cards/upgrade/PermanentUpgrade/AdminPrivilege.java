package sep.server.model.game.cards.upgrade.PermanentUpgrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.Player;

/**
 * Represents a permanent upgrade card with AdminPrivilege effect.
 * AdminPrivilege grants the player to give their robot priority for one register once per round.
 */
public class AdminPrivilege extends APermanentUpgrade{

    private static final Logger logger = LogManager.getLogger(AdminPrivilege.class);

    public AdminPrivilege(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "AdminPrivilege";
        this.cost = 3;
    }

    /**
     * Activates the AdminPrivilege upgrade for the specified player, setting hasAdminPrivilegeUpgrade to true.
     *
     * @param player the player to activate the upgrade for
     */
    @Override
    public void activateUpgrade(Player player) {
        logger.info("Update Card (AdminPrivilege) activated for player '{}'. Setting hasAdminPrivilegeUpgrade to true.",
                player.getController().getName());

        player.setHasAdminPrivilegeUpgrade(true);
    }
}
