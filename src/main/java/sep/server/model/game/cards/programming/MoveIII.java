package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class MoveIII extends AProgrammingCard implements IPlayableCard {

    public MoveIII(String cardType) {
        super(cardType);
        this.cardType = "MoveIII";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.moveRobotOneTileForwards();
        player.moveRobotOneTileForwards();
        player.moveRobotOneTileForwards();
    }

}
