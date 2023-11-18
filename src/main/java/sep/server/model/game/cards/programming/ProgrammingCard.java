package sep.server.model.game.cards.programming;

import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public class ProgrammingCard extends Card implements IPlayableCard {
    public ProgrammingCard(String cardType, boolean isProgrammable) {
        super(cardType, isProgrammable);
        setProgrammable(true);
    }

    @Override
    public void playCard() {}
}
