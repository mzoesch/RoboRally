package sep.server.model.game.cards.damage;

import sep.server.model.game.Player;

public class TrojanHorseDamage extends ADamageCard {
    public TrojanHorseDamage(String cardType) {
        super(cardType);
        this.cardType = "TrojanHorseDamage";
    }
    @Override
    public void playCard(Player player, int currentRoundNumber)
    {

    }
}
