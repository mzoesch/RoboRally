package sep.server.model.game.cards.programming;

import sep.server.model.game.cards.IPlayableCard;

public class LeftTurn extends AProgrammingCard implements IPlayableCard {
    public LeftTurn(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard() {}
}
