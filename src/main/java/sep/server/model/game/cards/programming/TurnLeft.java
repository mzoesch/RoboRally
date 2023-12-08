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
        player.rotateRobotOnTileToTheRight();
        player.rotateRobotOnTileToTheRight();
        player.rotateRobotOnTileToTheRight();
        for(Player player1 : GameState.gameMode.getPlayers()) {
            new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                    player.getPlayerController().getPlayerID(),
                    "counterclockwise").send();
        }
    }

}
