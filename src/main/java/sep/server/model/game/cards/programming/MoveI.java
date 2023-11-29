package sep.server.model.game.cards.programming;

import sep.server.json.game.effects.MovementModel;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class MoveI extends AProgrammingCard implements IPlayableCard {
    public MoveI(String cardType) {
        super(cardType);
        this.cardType = "MoveI";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.moveRobotOneTileForwards();
        for(Player player1 : GameState.gameMode.getPlayers()) {
            new MovementModel(player1.getPlayerController().getClientInstance(),
                    player.getPlayerController().getPlayerID(),
                    player.getPlayerRobot().getCurrentTile().getCoordinate().getXCoordinate(),
                    player.getPlayerRobot().getCurrentTile().getCoordinate().getYCoordinate()).send();
        }
    }
}
