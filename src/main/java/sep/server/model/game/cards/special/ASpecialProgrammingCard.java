package sep.server.model.game.cards.special;

import sep.server.model.game.Player;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public abstract class ASpecialProgrammingCard extends Card implements IPlayableCard {
    public ASpecialProgrammingCard(String cardType) {
        super(cardType);
    }

    public abstract void playCard(Player player, int currentRoundNumber);
}
