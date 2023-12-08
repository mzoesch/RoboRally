package sep.server.model.game.cards.special;

import sep.server.json.game.effects.MovementModel;
import sep.server.model.game.GameState;
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

        for(Player player1 : GameState.gameMode.getPlayers()) {
            new MovementModel(player1.getPlayerController().getClientInstance(),
                    player.getPlayerController().getPlayerID(),
                    player.getPlayerRobot().getCurrentTile().getCoordinate().getX(),
                    player.getPlayerRobot().getCurrentTile().getCoordinate().getY()).send();
        }
    }
}
