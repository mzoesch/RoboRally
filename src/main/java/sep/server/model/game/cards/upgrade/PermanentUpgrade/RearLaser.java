package sep.server.model.game.cards.upgrade.PermanentUpgrade;

import sep.server.model.game.Player;

public class RearLaser extends APermanentUpgrade{

    public RearLaser(String cardType, int cost) {
        super(cardType, cost);
        this.cardType = "RearLaser";
        this.cost = 2;
    }

    public void activateUpgrade(Player player) {
        player.getPlayerRobot().setCanShootBackward(true);
    }
}
