package sep.server.model.game.cards;

import sep.server.model.game.Player;

public interface IPlayableCard
{
    public abstract void playCard(Player player, int currentRoundNumber);
    public abstract String getCardType();

    @Override
    public abstract String toString();
}
