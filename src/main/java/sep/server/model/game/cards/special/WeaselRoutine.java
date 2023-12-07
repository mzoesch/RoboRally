package sep.server.model.game.cards.special;

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
        String auswahl = null;


        switch (auswahl) {
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
