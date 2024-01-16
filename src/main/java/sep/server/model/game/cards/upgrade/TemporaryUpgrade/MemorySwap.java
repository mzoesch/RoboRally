package sep.server.model.game.cards.upgrade.TemporaryUpgrade;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

import java.util.ArrayList;

public class MemorySwap extends ATemporaryUpgrade{

    public MemorySwap(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "MemorySwap";
        this.cost = 1;
    }

    public void activateUpgrade(Player player) {

        ArrayList<IPlayableCard> drawnCardsFromDeck = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            if (player.getPlayerDeck().isEmpty()) {
                player.shuffleAndRefillDeck();
            }
            IPlayableCard drawnCard = player.getPlayerDeck().remove(0);
            drawnCardsFromDeck.add(drawnCard);
        }

        //TO-DO: Auswahl auf Clientseite
        ArrayList<IPlayableCard> drawnCardsFromHand = new ArrayList<>();

        player.getPlayerDeck().addAll(0, drawnCardsFromHand);

        for (int i = 0; i < 3; i++) {
            int handIndex = player.getPlayerHand().indexOf(drawnCardsFromHand.get(i));
            player.getPlayerHand().set(handIndex, drawnCardsFromDeck.get(i));
        }

    }
}
