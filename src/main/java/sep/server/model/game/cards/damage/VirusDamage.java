package sep.server.model.game.cards.damage;

import sep.server.model.game.Player;

public class VirusDamage extends ADamageCard {
    public VirusDamage(String cardType) {
        super(cardType);
        this.cardType = "VirusDamage";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber)
    {

    }
}
