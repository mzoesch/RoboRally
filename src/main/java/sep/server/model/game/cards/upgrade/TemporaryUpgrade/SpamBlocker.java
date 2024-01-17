package sep.server.model.game.cards.upgrade.TemporaryUpgrade;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.damage.SpamDamage;

public class SpamBlocker extends ATemporaryUpgrade{

    private static final Logger logger = LogManager.getLogger(SpamBlocker.class);

    public SpamBlocker(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "SpamBlocker";
        this.cost = 3;
    }

    public void activateUpgrade(Player player) {

        logger.info("SpamBlocker activated for player '{}'. Removing SpamDamage card from hand.",
                player.getController().getName());

        for (int i = 0; i < player.getPlayerHand().size(); i++) {
            IPlayableCard currentCard = player.getPlayerHand().get(i);

            if (currentCard instanceof SpamDamage) {

                player.getPlayerHand().remove(i);
                player.getAuthGameMode().getSpamDeck().add((SpamDamage) currentCard);

                if (player.getPlayerDeck().isEmpty()) {
                    player.shuffleAndRefillDeck();}

                player.getPlayerHand().add(player.getPlayerDeck().remove(i));

                i--;
            }
        }
    }
}
