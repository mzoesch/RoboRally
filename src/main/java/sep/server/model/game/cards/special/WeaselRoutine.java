package sep.server.model.game.cards.special;

import sep.server.model.game.cards.IPlayableCard;

public class WeaselRoutine extends ASpecialProgrammingCard implements IPlayableCard {

    public WeaselRoutine(String cardType, boolean isProgrammable) {
        super(cardType);
    }

    @Override
    public void playCard() {}
}
