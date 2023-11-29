package sep.server.model.game.cards.programming;

import sep.server.json.game.effects.MovementModel;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class MoveIII extends AProgrammingCard implements IPlayableCard {

    public MoveIII(String cardType) {
        super(cardType);
        this.cardType = "MoveIII";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.moveRobotOneTileForwards();
        player.moveRobotOneTileForwards();
        player.moveRobotOneTileForwards();
        new MovementModel(player.getPlayerController().getClientInstance(),
                player.getPlayerController().getPlayerID(),
                player.getPlayerRobot().getCurrentTile().getCoordinate().getXCoordinate(),
                player.getPlayerRobot().getCurrentTile().getCoordinate().getYCoordinate()).send();
    }

}
