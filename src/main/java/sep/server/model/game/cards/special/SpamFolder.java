package sep.server.model.game.cards.special;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.damage.ADamageCard;
import sep.server.model.game.cards.damage.SpamDamage;

public class SpamFolder extends ASpecialProgrammingCard implements IPlayableCard {
    public SpamFolder(String cardType) {
        super(cardType);
        this.cardType = "SpamFolder";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        for (int i = 0; i < player.getDiscardPile().size(); i++) {
            IPlayableCard currentCard = player.getDiscardPile().get(i);

            if (currentCard instanceof SpamDamage) {
                player.getDiscardPile().remove(i);
                player.getGameMode().getSpamDeck().add((SpamDamage) currentCard);
                break;
            }
        }
    }
}

