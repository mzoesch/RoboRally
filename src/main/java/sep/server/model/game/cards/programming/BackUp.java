package sep.server.model.game.cards.programming;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a programming card that moves the player's robot one tile backwards.
 */
public class BackUp extends AProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(GameState.class);

    public BackUp(String cardType) {
        super(cardType);
        this.cardType = "BackUp";
    }

    /**
     * Plays the BackUp programming card, moving the player's robot one tile backwards.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().moveRobotOneTileBackwards();
        player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
        l.info("Player {} played BackUp card, moving one tile backwards.", player.getController().getPlayerID());
    }
}
