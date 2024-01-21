package sep.server.model.game.cards.special;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a special programming card with a WeaselRoutine effect.
 * The WeaselRoutine card allows the player's robot to perform a rotation based on player's selection.
 */
public class WeaselRoutine extends ASpecialProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(GameState.class);

    public WeaselRoutine(String cardType) {
        super(cardType);
        this.cardType = "WeaselRoutine";
    }

    /**
     * Plays the WeaselRoutine card, performing a rotation based on player's selection,
     * and broadcasting the updated rotation to the session.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber){

        //TODO implement logic to obtain user's choice
        String userSelection = null; //placeholder for user's choice

        switch (userSelection) {

            case "TurnLeft":
                player.getPlayerRobot().rotateRobotOnTileToTheLeft();
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");
                break;

            case "TurnRight":
                player.getPlayerRobot().rotateRobotOnTileToTheRight();
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
                break;

            case "UTurn":
                player.getPlayerRobot().rotateRobotOnTileToTheRight();
                player.getPlayerRobot().rotateRobotOnTileToTheRight();
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
                break;

            default:
                l.warn("Invalid choice for WeaselRoutine card: {}", userSelection);

        }
    }
}
