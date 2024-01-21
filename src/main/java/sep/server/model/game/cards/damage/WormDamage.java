package sep.server.model.game.cards.damage;

import sep.server.model.game.Player;

/**
 * Represents a Worm damage card.
 */
public class WormDamage extends ADamageCard {
    public WormDamage(String cardType) {
        super(cardType);
        this.cardType = "WormDamage";
    }

    /**
     * Plays the Worm damage card.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().reboot();
    }
}
