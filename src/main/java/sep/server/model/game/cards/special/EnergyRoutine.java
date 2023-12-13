package sep.server.model.game.cards.special;

import sep.server.json.game.effects.EnergyModel;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class EnergyRoutine extends ASpecialProgrammingCard implements IPlayableCard {
    public EnergyRoutine(String cardType) {
        super(cardType);
        this.cardType = "EnergyRoutine";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber){
        int currentEnergy = player.getEnergyCollected();
        int newEnergy= currentEnergy +1;
        player.setEnergyCollected(newEnergy);

        player.getAuthGameMode().getSession().broadcastEnergyUpdate(player.getController().getPlayerID(), newEnergy, "EnergyRoutine");

    }
}
