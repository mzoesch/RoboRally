package sep.server.model.game.cards.special;

import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public class ASpecialProgrammingCard extends Card implements IPlayableCard {
    public ASpecialProgrammingCard(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard() {}
}
