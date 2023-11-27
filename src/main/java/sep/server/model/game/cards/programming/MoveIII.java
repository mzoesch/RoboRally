package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class MoveIII extends AProgrammingCard implements IPlayableCard {

    public MoveIII(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Player player) {
        player.moveRobotOneTile();
        player.moveRobotOneTile();
        player.moveRobotOneTile();
    }

}
