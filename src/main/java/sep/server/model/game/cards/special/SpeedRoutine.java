package sep.server.model.game.cards.special;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class SpeedRoutine extends ASpecialProgrammingCard implements IPlayableCard {
    public SpeedRoutine(String cardType) {
        super(cardType);
        this.cardType = "SpeedRoutine";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.moveRobotOneTileForwards();
        player.moveRobotOneTileForwards();
        player.moveRobotOneTileForwards();
    }
}
