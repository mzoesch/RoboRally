package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class PowerUp extends AProgrammingCard implements IPlayableCard {

    public PowerUp(String cardType) {
        super(cardType);
        this.cardType = "PowerUp";
    }


    @Override
    public void playCard(Player player, int currentRoundNumber) {
        int currentEnergy = player.getEnergyCollected();
        int newEnergy= currentEnergy +1;
        player.setEnergyCollected(newEnergy);

        if(!player.getPlayerRobot().isRebootTriggered()) {
            player.getAuthGameMode().getSession().broadcastEnergyUpdate(player.getController().getPlayerID(), newEnergy, "PowerUpCard");
        } else {
            player.getPlayerRobot().setRebootTriggered(false);
        }
    }
}
