package sep.server.model.game.cards.special;

import sep.server.json.game.effects.MovementModel;
import sep.server.json.game.effects.PlayerTurningModel;
import sep.server.model.game.GameState;
import sep.server.model.game.Player;
import sep.server.model.game.cards.IPlayableCard;

public class SandboxRoutine extends ASpecialProgrammingCard implements IPlayableCard {
    public SandboxRoutine(String cardType) {
        super(cardType);
        this.cardType = "SandboxRoutine";
    }
    @Override
    public void playCard(Player player, int currentRoundNumber)  {

        //TODO Abfrage machen
        String auswahl = null;


        switch (auswahl) {

            case "Move1":
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());
                break;

            case "Move2":
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());

                break;

            case "Move3":
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getPlayerRobot().moveRobotOneTileForwards();
                player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());

                break;

            case "BackUp":
                player.getPlayerRobot().moveRobotOneTileBackwards();
                player.getAuthGameMode().getSession().broadcastPositionUpdate(player.getController().getPlayerID(), player.getPosition());

                break;

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
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");
                player.getAuthGameMode().getSession().broadcastRotationUpdate(player.getController().getPlayerID(), "counterclockwise");
                break;
            default:
               return;
        }


    }
}
