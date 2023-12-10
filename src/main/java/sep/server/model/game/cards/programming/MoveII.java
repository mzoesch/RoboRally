package sep.server.model.game.cards.programming;

import sep.server.json.game.effects.MovementModel;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class MoveII extends AProgrammingCard implements IPlayableCard {
    public MoveII(String cardType) {
        super(cardType);
        this.cardType = "MoveII";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().moveRobotOneTileForwards();
        player.getPlayerRobot().moveRobotOneTileForwards();
        for(Player player1 : GameState.gameMode.getPlayers()) {
            new MovementModel(player1.getPlayerController().getClientInstance(),
                    player.getPlayerController().getPlayerID(),
                    player.getPlayerRobot().getCurrentTile().getCoordinate().getX(),
                    player.getPlayerRobot().getCurrentTile().getCoordinate().getY()).send();
        }
    }
}
