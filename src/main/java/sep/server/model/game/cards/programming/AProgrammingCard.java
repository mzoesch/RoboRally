package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Abstract class representing a programming card.
 */
public abstract class AProgrammingCard extends Card implements IPlayableCard {

    public AProgrammingCard(String cardType) {
        super(cardType);
    }

    @Override
    public String getCardType() {
        return super.getCardType();
    }

    public abstract void playCard(Player player, int currentRoundNumber);

    @Override
    public String toString() {
        return super.getCardType();
    }

}
