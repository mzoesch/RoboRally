package sep.server.model.game.cards.programming;

import sep.server.json.game.effects.PlayerTurningModel;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class RightTurn extends AProgrammingCard implements IPlayableCard {

    public RightTurn(String cardType) {
        super(cardType);
        this.cardType = "RightTurn";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.rotateRobotOneTileToTheRight();
        for(Player player1 : GameState.gameMode.getPlayers()) {
            new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                    player.getPlayerController().getPlayerID(),
                    "clockwise").send();
        }
    }
}
