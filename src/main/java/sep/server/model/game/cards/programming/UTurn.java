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
        player.rotateRobotOnTileToTheRight();
        player.rotateRobotOnTileToTheRight();
        for(Player player1 : GameState.gameMode.getPlayers()) {
            new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                    player.getPlayerController().getPlayerID(),
                    "clockwise").send();
            new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                    player.getPlayerController().getPlayerID(),
                    "clockwise").send();
        }
    }
}
