package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class MoveII extends AProgrammingCard implements IPlayableCard {
    public MoveII(String cardType) {
        super(cardType);
        this.cardType = "MoveII";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().moveRobotOneTileForwards();
        player.getPlayerRobot().moveRobotOneTileForwards();
        player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());

    }
}
