package sep.server.model.game.cards.programming;

import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.json.game.effects.MovementModel;

public class BackUp extends AProgrammingCard implements IPlayableCard {
    public BackUp(String cardType) {
        super(cardType);
        this.cardType = "BackUp";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.getPlayerRobot().moveRobotOneTileBackwards();
        for(Player player1 : GameState.gameMode.getPlayers()) {
            new MovementModel(player1.getPlayerController().getClientInstance(),
                    player.getPlayerController().getPlayerID(),
                    player.getPlayerRobot().getCurrentTile().getCoordinate().getX(),
                    player.getPlayerRobot().getCurrentTile().getCoordinate().getY()).send();
        }
    }
}
