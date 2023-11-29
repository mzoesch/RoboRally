package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;
import sep.server.json.game.effects.PlayerTurningModel;

public class LeftTurn extends AProgrammingCard implements IPlayableCard {
    public LeftTurn(String cardType) {
        super(cardType);
        this.cardType = "LeftTurn";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber) {
        player.rotateRobotOneTileToTheRight();
        player.rotateRobotOneTileToTheRight();
        player.rotateRobotOneTileToTheRight();
        new PlayerTurningModel(player.getPlayerController().getClientInstance(),
                player.getPlayerController().getPlayerID(),
                "clockwise").send();
    }

}
