package sep.server.model.game.cards.programming;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a programming card that increases the player's energy by 1.
 */
public class PowerUp extends AProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(GameState.class);

    public PowerUp(String cardType) {
        super(cardType);
        this.cardType = "PowerUp";
    }

    /**
     * Plays the PowerUp programming card, increasing the player's energy by 1.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        int currentEnergy = player.getEnergyCollected();
        int newEnergy= currentEnergy +1;
        player.setEnergyCollected(newEnergy);

        if(!player.getPlayerRobot().isRebootTriggered()) {
            player.getAuthGameMode().getSession().broadcastEnergyUpdate(player.getController().getPlayerID(), newEnergy, "PowerUpCard");
            l.info("Player {} played PowerUp card, increasing energy by 1.", player.getController().getPlayerID());
        } else {
            player.getPlayerRobot().setRebootTriggered(false);
        }
    }
}
