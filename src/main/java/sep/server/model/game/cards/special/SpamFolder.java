package sep.server.model.game.cards.special;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.Player;
import sep.server.model.game.builder.CourseBuilder;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.damage.SpamDamage;

/**
 * Represents a special programming card with a SpamFolder effect.
 * The SpamFolder card allows the player to retrieve a SpamDamage card from their discard pile
 * and add it back to the SpamDamage deck.
 */
public class SpamFolder extends ASpecialProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(CourseBuilder.class);

    public SpamFolder(String cardType) {
        super(cardType);
        this.cardType = "SpamFolder";
    }

    /**
     * Plays the SpamFolder card, retrieving a SpamDamage card from the discard pile
     * and adding it back to the SpamDamage deck.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        for (int i = 0; i < player.getDiscardPile().size(); i++) {
            IPlayableCard currentCard = player.getDiscardPile().get(i);

            if (currentCard instanceof SpamDamage) {
                player.getDiscardPile().remove(i);
                player.getAuthGameMode().getSpamDeck().add((SpamDamage) currentCard);
                l.info("SpamFolder card played: Retrieved a SpamDamage card from discard pile.");
                return;
            }
        }
        l.info("SpamFolder card played: No SpamDamage card found in the discard pile.");
    }
}

