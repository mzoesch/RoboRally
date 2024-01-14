package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class TurnLeft extends AProgrammingCard implements IPlayableCard {
    public TurnLeft(String cardType) {
        super(cardType);
        this.cardType = "TurnLeft";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().rotateRobotOnTileToTheLeft();

        if(!player.getPlayerRobot().isRebootTriggered()) {
            player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");
        } else {
            player.getPlayerRobot().setRebootTriggered(false);
        }
    }

}
