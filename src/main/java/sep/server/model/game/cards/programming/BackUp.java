package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a programming card that moves the player's robot one tile backwards.
 */
public class BackUp extends AProgrammingCard implements IPlayableCard {

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
    }
}
