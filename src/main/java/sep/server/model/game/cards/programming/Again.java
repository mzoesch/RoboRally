package sep.server.model.game.cards.programming;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.Player;
import sep.server.model.game.builder.CourseBuilder;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.damage.ADamageCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

/**
 * Represents a programming card that allows a player to repeat the action of the previous round's card.
 */
public class Again extends AProgrammingCard implements IPlayableCard {

    private static final Logger l = LogManager.getLogger(CourseBuilder.class);

    public Again(String cardType) {
        super(cardType);
        this.cardType = "Again";
    }

    /**
     * Plays the Again programming card, allowing the player to repeat the action of the previous round's card.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber) {
        if (currentRoundNumber == 0) {
            l.warn("Player {} played Again card in the first round, ignoring the card.", player.getController().getPlayerID());
            return;
        }

        IPlayableCard[] registers = player.getRegisters();
        IPlayableCard previousRoundCard = registers[currentRoundNumber];

        l.debug("Player {} played Again card, repeating the action of the previous round's card: {}", player.getController().getPlayerID(), previousRoundCard.getCardType());

        if (previousRoundCard instanceof ADamageCard) {
            handleDamageCard(player, currentRoundNumber);
        } else if (previousRoundCard instanceof AUpgradeCard) {
            handleUpgradeCard(player,  currentRoundNumber);
        } else {
            previousRoundCard.playCard(player, currentRoundNumber - 1);
        }
    }

    /**
     * Handles the case when the previous round's card is a damage card.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    private void handleDamageCard(Player player,  int currentRoundNumber) {
        if (player.getPlayerDeck().isEmpty()) {
            player.shuffleAndRefillDeck();
        }

        IPlayableCard drawnCard = player.getPlayerDeck().remove(0);
        player.setCardInRegister(currentRoundNumber-1, drawnCard);
        drawnCard.playCard(player, currentRoundNumber - 1);
    }

    /**
     * Handles the case when the previous round's card is an upgrade card.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    private void handleUpgradeCard(Player player,  int currentRoundNumber) {
        //hasn't been implemented as it is not needed so far
        l.warn("Handling of Upgrade Cards for Again programming card is incomplete.");
    }

}


