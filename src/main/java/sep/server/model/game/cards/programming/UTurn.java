package sep.server.model.game.cards.programming;

import sep.server.model.game.cards.IPlayableCard;

public class UTurn extends AProgrammingCard implements IPlayableCard {

    public UTurn(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard() {}
}
