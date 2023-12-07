package sep.server.model.game.cards.special;

import sep.server.json.game.effects.MovementModel;
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
                player.moveRobotOneTileForwards();
                break;
            case "Move2":
                player.moveRobotOneTileForwards();
                player.moveRobotOneTileForwards();
                break;
            case "Move3":
                player.moveRobotOneTileForwards();
                player.moveRobotOneTileForwards();
                player.moveRobotOneTileForwards();
                break;
            case "BackUp":
                player.moveRobotOneTileBackwards();
                break;
            case "TurnLeft":
                player.rotateRobotOneTileToTheRight();
                player.rotateRobotOneTileToTheRight();
                player.rotateRobotOneTileToTheRight();
                break;
            case "TurnRight":
                player.rotateRobotOneTileToTheRight();
                break;
            case "UTurn":
                player.rotateRobotOneTileToTheRight();
                player.rotateRobotOneTileToTheRight();
                break;
            default:
               return;
        }


    }
}
