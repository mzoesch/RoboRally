package sep.server.model.game.cards.programming;

import sep.server.model.game.cards.IPlayableCard;

public class RightTurn extends AProgrammingCard implements IPlayableCard {

    public RightTurn(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard() {}
}
