package sep.server.model.game.cards.special;

import sep.server.json.game.effects.PlayerTurningModel;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class WeaselRoutine extends ASpecialProgrammingCard implements IPlayableCard {

    public WeaselRoutine(String cardType, boolean isProgrammable) {
        super(cardType);
        this.cardType = "WeaselRoutine";
    }

    @Override
    public void playCard(Player player, int currentRoundNumber){
        //TODO Abfrage machen
        String selection = null;

        switch (selection) {

            case "TurnLeft":
                player.getPlayerRobot().rotateRobotOnTileToTheLeft();
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");
                break;

            case "TurnRight":
                player.getPlayerRobot().rotateRobotOnTileToTheRight();
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");

                break;

            case "UTurn":
                player.getPlayerRobot().rotateRobotOnTileToTheRight();
                player.getPlayerRobot().rotateRobotOnTileToTheRight();
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "clockwise");
                break;
        }

    }
}
