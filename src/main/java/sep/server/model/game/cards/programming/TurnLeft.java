package sep.server.model.game.cards.programming;

import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.json.game.effects.PlayerTurningModel;

public class TurnLeft extends AProgrammingCard implements IPlayableCard {
    public TurnLeft(String cardType) {
        super(cardType);
        this.cardType = "TurnLeft";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().rotateRobotOnTileToTheRight();
        player.getPlayerRobot().rotateRobotOnTileToTheRight();
        player.getPlayerRobot().rotateRobotOnTileToTheRight();
        player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");

    }

}
