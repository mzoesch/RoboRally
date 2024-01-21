package sep.server.model.game.cards.damage;

import sep.server.model.game.Player;

/**
 * Represents a Spam damage card.
 */
public class SpamDamage extends ADamageCard {

    public SpamDamage(String cardType) {
        super(cardType);
        this.cardType = "SpamDamage";
    }

    /**
     * Plays the Spam damage card.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.updateRegisterAfterDamageCardWasPlayed("SpamDamage", currentRoundNumber);
    }
}
