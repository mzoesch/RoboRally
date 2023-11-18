package sep.server.model.game.cards.programming;

import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public class SpecialProgrammingCard extends Card implements IPlayableCard {
    public SpecialProgrammingCard(String cardType, boolean isProgrammable) {
        super(cardType, isProgrammable);
        setProgrammable(true);
    }

    @Override
    public void playCard() {}
}
