package sep.server.model.game.cards.special;

import sep.server.model.game.cards.IPlayableCard;

public class SpamFolder extends ASpecialProgrammingCard implements IPlayableCard {
    public SpamFolder(String cardType) {
        super(cardType);
        this.cardType = "SpamFolder";
    }
    @Override
    public void playCard() {}
}
