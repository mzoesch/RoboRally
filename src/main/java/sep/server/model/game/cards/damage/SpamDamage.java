package sep.server.model.game.cards.damage;

import sep.server.model.game.Player;
import sep.server.model.game.cards.Card;
import sep.server.model.game.cards.IPlayableCard;

public class SpamDamage extends ADamageCard {
    public SpamDamage(String cardType) {
        super(cardType);
        this.cardType = "SpamDamage";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber)
    {
        player.updateRegisterAfterDamageCardWasPlayed("SpamDamage", currentRoundNumber);

    }
}
