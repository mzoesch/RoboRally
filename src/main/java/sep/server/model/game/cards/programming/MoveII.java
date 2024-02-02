package sep.server.model.game.cards.programming;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a programming card that moves the player's robot two tiles forward.
 */
public class MoveII extends AProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(GameState.class);

    public MoveII(String cardType) {
        super(cardType);
        this.cardType = "MoveII";
    }

    /**
     * Plays the MoveII programming card, moving the player's robot two tiles forward.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().moveRobotOneTileForwards();

        if (!player.getPlayerRobot().isRebootTriggered())
        {
            player.getPlayerRobot().moveRobotOneTileForwards();
        }

        if(!player.getPlayerRobot().isRebootTriggered()) {
            player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
            l.info("Player {} played MoveII card, moving two tiles forward.", player.getController().getPlayerID());
        } else {
            player.getPlayerRobot().setRebootTriggered(false);
        }
    }
}
