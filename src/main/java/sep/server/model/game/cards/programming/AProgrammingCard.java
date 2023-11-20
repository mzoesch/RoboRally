package sep.server.model.game.cards.programming;

import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public class AProgrammingCard extends Card implements IPlayableCard {
    public AProgrammingCard(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard() {}
}
