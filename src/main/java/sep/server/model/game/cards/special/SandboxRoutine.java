package sep.server.model.game.cards.special;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a special programming card with a sandbox routine effect.
 * The SandboxRoutine card allows the player to choose a movement or rotation action.
 */
public class SandboxRoutine extends ASpecialProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(GameState.class);

    public SandboxRoutine(String cardType) {
        super(cardType);
        this.cardType = "SandboxRoutine";
    }

    /**
     * Plays the SandboxRoutine card, allowing the player to choose a movement or rotation action.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber)  {

        //TODO implement logic to obtain user's choice
        String userSelection = null; //placeholder for user's choice

        switch (userSelection) {

            case "Move1":
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
                break;

            case "Move2":
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
                break;

            case "Move3":
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
                break;

            case "BackUp":
                player.getPlayerRobot().moveRobotOneTileBackwards();
                player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
                break;

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
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");
                break;

            default:
                l.warn("Invalid choice for SandboxRoutine card: {}", userSelection);

        }
    }
}
