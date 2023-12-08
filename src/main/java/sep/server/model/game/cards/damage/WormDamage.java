package sep.server.model.game.cards.damage;

import sep.server.model.game.Player;

public class WormDamage extends ADamageCard {
    public WormDamage(String cardType) {
        super(cardType);
        this.cardType = "WormDamage";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber)
    {

    }
}
