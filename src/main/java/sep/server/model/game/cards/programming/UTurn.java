package sep.server.model.game.cards.programming;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a programming card that performs a U-turn, rotating the player's robot 180 degrees.
 */
public class UTurn extends AProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(GameState.class);

    public UTurn(String cardType) {
        super(cardType);
        this.cardType = "UTurn";
    }

    /**
     * Plays the UTurn programming card, rotating the player's robot 180 degrees.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().rotateRobotOnTileToTheRight();
        player.getPlayerRobot().rotateRobotOnTileToTheRight();

        if(!player.getPlayerRobot().isRebootTriggered()) {
            player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
            player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
            l.info("Player {} played UTurn card, performing a 180-degree rotation.", player.getController().getPlayerID());
        } else {
            player.getPlayerRobot().setRebootTriggered(false);
        }
    }
}
