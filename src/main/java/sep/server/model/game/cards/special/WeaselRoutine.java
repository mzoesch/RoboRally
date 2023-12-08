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
        String auswahl = null;

        switch (auswahl) {

            case "TurnLeft":
                player.rotateRobotOneTileToTheRight();
                player.rotateRobotOneTileToTheRight();
                player.rotateRobotOneTileToTheRight();
                for(Player player1 : GameState.gameMode.getPlayers()) {
                    new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                            player.getPlayerController().getPlayerID(),
                            "counterclockwise").send();
                }
                break;

            case "TurnRight":
                player.rotateRobotOneTileToTheRight();
                for(Player player1 : GameState.gameMode.getPlayers()) {
                    new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                            player.getPlayerController().getPlayerID(),
                            "clockwise").send();
                }
                break;

            case "UTurn":
                player.rotateRobotOneTileToTheRight();
                player.rotateRobotOneTileToTheRight();
                for(Player player1 : GameState.gameMode.getPlayers()) {
                    new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                            player.getPlayerController().getPlayerID(),
                            "clockwise").send();
                    new PlayerTurningModel(player1.getPlayerController().getClientInstance(),
                            player.getPlayerController().getPlayerID(),
                            "clockwise").send();
                }
                break;
            default:
                return;
        }

    }
}
