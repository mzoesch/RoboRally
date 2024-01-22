package sep.server.model.game.cards.programming;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a programming card that rotates the player's robot to the left.
 */
public class TurnLeft extends AProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(GameState.class);

    public TurnLeft(String cardType) {
        super(cardType);
        this.cardType = "TurnLeft";
    }

    /**
     * Plays the TurnLeft programming card, rotating the player's robot to the left.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().rotateRobotOnTileToTheLeft();

        if(!player.getPlayerRobot().isRebootTriggered()) {
            player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");
            l.info("Player {} played TurnLeft card, rotating robot counterclockwise.", player.getController().getPlayerID());
        } else {
            player.getPlayerRobot().setRebootTriggered(false);
        }
    }

}
