package sep.server.model.game.cards.programming;

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
        player.moveRobotOneTileBackwards();
        new MovementModel(player.getPlayerController().getClientInstance(),
                player.getPlayerController().getPlayerID(),
                player.getPlayerRobot().getCurrentTile().getCoordinate().getXCoordinate(),
                player.getPlayerRobot().getCurrentTile().getCoordinate().getYCoordinate()).send();
    }
}
