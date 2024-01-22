package sep.server.model.game.cards;

import sep.server.model.game.Player;

/**
 * Represents an interface for playable cards in the game meaning all card types that can be added to the player deck.
 * All playable card types should implement this interface.
 */
public interface IPlayableCard {

    public abstract void playCard(Player player, int currentRoundNumber);

    public abstract String getCardType();

    @Override
    public abstract String toString();
}
