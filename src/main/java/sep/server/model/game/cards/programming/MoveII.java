package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class MoveII extends AProgrammingCard implements IPlayableCard {
    public MoveII(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Player player) {
        player.moveRobotOneTile();
        player.moveRobotOneTile();
    }
}
