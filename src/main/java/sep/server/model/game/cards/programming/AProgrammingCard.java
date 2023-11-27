package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public abstract class AProgrammingCard extends Card implements IPlayableCard {
    public AProgrammingCard(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard() {}

    public abstract void playCard(Player player);
}
