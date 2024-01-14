package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class TurnRight extends AProgrammingCard implements IPlayableCard {

    public TurnRight(String cardType) {
        super(cardType);
        this.cardType = "TurnRight";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().rotateRobotOnTileToTheRight();

        if(!player.getPlayerRobot().isRebootTriggered()) {
            player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
        } else {
            player.getPlayerRobot().setRebootTriggered(false);
        }
    }
}
