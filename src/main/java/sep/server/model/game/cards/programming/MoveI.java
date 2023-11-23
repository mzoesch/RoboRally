package sep.server.model.game.cards.programming;

import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class MoveI extends AProgrammingCard implements IPlayableCard {
    public MoveI(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Robot robot) {}
}
