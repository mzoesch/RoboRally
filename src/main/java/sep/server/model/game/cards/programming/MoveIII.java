package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class MoveIII extends AProgrammingCard implements IPlayableCard {

    public MoveIII(String cardType) {
        super(cardType);
        this.cardType = "MoveIII";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().moveRobotOneTileForwards();
        player.getPlayerRobot().moveRobotOneTileForwards();
        player.getPlayerRobot().moveRobotOneTileForwards();

        if(!player.getPlayerRobot().isRebootTriggered()) {
            player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
        } else {
            player.getPlayerRobot().setRebootTriggered(false);
        }
    }

}
