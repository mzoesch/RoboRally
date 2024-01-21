package sep.server.model.game.cards.upgrade.PermanentUpgrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.Player;

/**
 * Represents a Rear Laser permanent upgrade card.
 * This upgrade allows the player's robot to shoot backward.
 */
public class RearLaser extends APermanentUpgrade{

    private static final Logger logger = LogManager.getLogger(RearLaser.class);

    public RearLaser(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "RearLaser";
        this.cost = 2;
    }

    /**
     * Activates the RearLaser permanent upgrade for the specified player.
     * Sets the player's robot ability to shoot backward to true.
     *
     * @param player the player to activate the RearLaser upgrade for
     */
    public void activateUpgrade(Player player) {
        logger.info("Update Card (RearLaser) activated for player '{}'. Setting canShootBackward to true.",
                player.getController().getName());

        player.getPlayerRobot().setCanShootBackward(true);
    }
}
