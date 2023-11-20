package sep.server.model.game.cards.special;

import sep.server.model.game.cards.IPlayableCard;

public class SandboxRoutine extends ASpecialProgrammingCard implements IPlayableCard {
    public SandboxRoutine(String cardType) {
        super(cardType);
    }
    @Override
    public void playCard() {}
}
