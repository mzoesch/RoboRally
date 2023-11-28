package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class BackUp extends AProgrammingCard implements IPlayableCard {
    public BackUp(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.moveRobotOneTileBackwards();
    }
}
