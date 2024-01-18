package sep.server.model.game.cards.upgrade.TemporaryUpgrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

import java.util.ArrayList;

public class MemorySwap extends ATemporaryUpgrade{

    private static final Logger l = LogManager.getLogger(MemorySwap.class);

    public MemorySwap(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "MemorySwap";
        this.cost = 1;
    }

    public void activateUpgrade(Player player) {

        //Liste DrawnCardsFromDeck
        ArrayList<IPlayableCard> drawnCardsFromDeck = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (player.getPlayerDeck().isEmpty()) {
                player.shuffleAndRefillDeck();
            }
            IPlayableCard drawnCard = player.getPlayerDeck().remove(0);
            drawnCardsFromDeck.add(drawnCard);
        }

        //Liste DrawnCardsFromHand
        ArrayList<IPlayableCard> drawnCardsFromHand = new ArrayList<>();
        String[] memorySwapCards = player.getMemorySwapCards();
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

    }
}
