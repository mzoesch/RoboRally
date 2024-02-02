package sep.server.model.game.cards.programming;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a programming card that moves the player's robot three tiles forward.
 */
public class MoveIII extends AProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(GameState.class);

    public MoveIII(String cardType) {
        super(cardType);
        this.cardType = "MoveIII";
    }

    /**
     * Plays the MoveIII programming card, moving the player's robot three tiles forward.
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

        if (!player.getPlayerRobot().isRebootTriggered())
        {
            player.getPlayerRobot().moveRobotOneTileForwards();
        }

        if(!player.getPlayerRobot().isRebootTriggered()) {
            player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
            l.info("Player {} played MoveIII card, moving three tiles forward.", player.getController().getPlayerID());
        } else {
            player.getPlayerRobot().setRebootTriggered(false);
        }
    }

}
