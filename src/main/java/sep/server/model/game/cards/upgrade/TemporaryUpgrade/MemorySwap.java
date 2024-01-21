package sep.server.model.game.cards.upgrade.TemporaryUpgrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

import java.util.ArrayList;

/**
 * Represents the MemorySwap upgrade card.
 * This card allows a player to draw three new cards and add three cards from their hand to their deck.
 */
public class MemorySwap extends ATemporaryUpgrade{

    private static final Logger l = LogManager.getLogger(MemorySwap.class);

    public MemorySwap(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "MemorySwap";
        this.cost = 1;
    }

    /**
     * Activates the MemorySwap upgrade for a player.
     * Swaps specified cards from the player's hand with cards drawn from their deck.
     *
     * @param player The player for whom the MemorySwap upgrade is activated.
     */
    public void activateUpgrade(Player player) {
        ArrayList<IPlayableCard> drawnCardsFromDeck = new ArrayList<>();
        ArrayList<IPlayableCard> drawnCardsFromHand = new ArrayList<>();
        String[] memorySwapCards = player.getMemorySwapCards();

        for (int i = 0; i < 3; i++) {
            if (player.getPlayerDeck().isEmpty()) {
                player.shuffleAndRefillDeck();
            }
            IPlayableCard drawnCard = player.getPlayerDeck().remove(0);
            drawnCardsFromDeck.add(drawnCard);
        }

        for (IPlayableCard card : player.getPlayerHand()) {
            String cardName = card.getCardType();
            for (String memorySwapCardName : memorySwapCards) {
                if (cardName.equals(memorySwapCardName)) {
                    drawnCardsFromHand.add(card);
                    break;
                }
            }
        }

        player.getPlayerDeck().addAll(0, drawnCardsFromHand);

        for (int i = 0; i < 3; i++) {
            int handIndex = player.getPlayerHand().indexOf(drawnCardsFromHand.get(i));
            player.getPlayerHand().set(handIndex, drawnCardsFromDeck.get(i));
        }

        l.debug("Upgrade (MemorySwap) activated for player '{}'.  Player's updated hand: {}",
                player.getController().getName(), player.getPlayerHand());

        player.setMemorySwapCards(null);
    }
}
