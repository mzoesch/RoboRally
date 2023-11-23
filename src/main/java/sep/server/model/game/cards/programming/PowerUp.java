package sep.server.model.game.cards.programming;

import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class PowerUp extends AProgrammingCard implements IPlayableCard {

    public PowerUp(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Robot robot) {}
}
