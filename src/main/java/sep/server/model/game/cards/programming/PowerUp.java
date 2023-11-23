package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class PowerUp extends AProgrammingCard implements IPlayableCard {

    public PowerUp(String cardType) {
        super(cardType);
    }


    @Override
    public void playCard(Player player, Robot robot, int currentRoundNumber) {
        int currentEnergy = player.getEnergyCollected();
        player.setEnergyCollected(currentEnergy + 1);
    }
}
