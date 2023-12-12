package sep.server.model.game.cards.programming;

import sep.server.json.game.effects.PlayerTurningModel;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class UTurn extends AProgrammingCard implements IPlayableCard {

    public UTurn(String cardType) {
        super(cardType);
        this.cardType = "UTurn";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().rotateRobotOnTileToTheRight();
        player.getPlayerRobot().rotateRobotOnTileToTheRight();
        player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
        player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
    }
}
