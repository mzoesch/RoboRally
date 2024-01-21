package sep.server.model.game.cards.special;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.Player;
import sep.server.model.game.builder.CourseBuilder;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a special programming card with a SpeedRoutine effect.
 * The SpeedRoutine card allows the player's robot to move three tiles forward.
 */
public class SpeedRoutine extends ASpecialProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(CourseBuilder.class);

    public SpeedRoutine(String cardType) {
        super(cardType);
        this.cardType = "SpeedRoutine";
    }

    /**
     * Plays the SpeedRoutine card, moving the player's robot three tiles forward
     * and broadcasting the updated position to the session.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().moveRobotOneTileForwards();
        player.getPlayerRobot().moveRobotOneTileForwards();
        player.getPlayerRobot().moveRobotOneTileForwards();
        player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
        l.info("SpeedRoutine card played: Player's robot moved three tiles forward.");
    }
}
