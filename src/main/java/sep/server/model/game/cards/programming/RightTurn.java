package sep.server.model.game.cards.programming;

import sep.server.json.game.effects.PlayerTurningModel;
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
        new PlayerTurningModel(player.getPlayerController().getClientInstance(),
                player.getPlayerController().getPlayerID(),
                "clockwise").send();
    }
}
