package sep.server.model.game.cards.special;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

/**
 * Represents a special programming card with an energy routine effect.
 * The EnergyRoutine card increases the player's energy by 1 and broadcasts the energy update.
 */
public class EnergyRoutine extends ASpecialProgrammingCard implements IPlayableCard {

    public EnergyRoutine(String cardType) {
        super(cardType);
        this.cardType = "EnergyRoutine";
    }

    /**
     * Plays the EnergyRoutine card, increasing the player's energy by 1 and broadcasting the energy update.
     *
     * @param player             the player playing the card
     * @param currentRoundNumber the current round number
     */
    @Override
    public void playCard(Player player, int currentRoundNumber){
        int currentEnergy = player.getEnergyCollected();
        int newEnergy= currentEnergy +1;
        player.setEnergyCollected(newEnergy);

        player.getAuthGameMode().getSession().broadcastEnergyUpdate(player.getController().getPlayerID(), newEnergy, "EnergyRoutine");
    }
}
