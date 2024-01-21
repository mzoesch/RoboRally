package sep.server.model.game.cards.special;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.model.game.cards.damage.ADamageCard;
import sep.server.model.game.cards.upgrade.AUpgradeCard;

/**
 * Represents a special programming card with a repeat routine effect.
 * The RepeatRoutine card allows the player to repeat the effects of the previous round's card.
 */
public class RepeatRoutine extends ASpecialProgrammingCard implements IPlayableCard {
    //TODO remove redundant code.. exactly the same as Again.java

    private static final Logger l = LogManager.getLogger(GameState.class);

    public RepeatRoutine(String cardType) {
        super(cardType);
        this.cardType = "RepeatRoutine";
    }

    /**
     * Plays the RepeatRoutine card, repeating the effects of the previous round's card.
     * If the previous card is a damage card or an upgrade card, it repeats the respective handling.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber)  {
        if (currentRoundNumber == 0) {
            return;
        }

        IPlayableCard[] registers = player.getRegisters();
        IPlayableCard previousRoundCard = registers[currentRoundNumber - 1];

        if (previousRoundCard instanceof ADamageCard) {
            handleDamageCard(player, currentRoundNumber);
        } else if (previousRoundCard instanceof AUpgradeCard) {
            handleUpgradeCard(player,  currentRoundNumber);
        } else {
            previousRoundCard.playCard(player, currentRoundNumber - 1);
        }
    }

    /**
     * Handles the case where the previous card was a damage card.
     * Draws a new card from the player's deck and replays its effects.
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
     * Handles the case where the previous card was an upgrade card.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    private void handleUpgradeCard(Player player,  int currentRoundNumber) {
        //TODO implementation
        l.warn("Handling of Upgrade Cards for Again programming card is incomplete.");
    }
}

