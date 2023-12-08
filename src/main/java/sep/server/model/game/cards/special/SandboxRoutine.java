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
                player.moveRobotOneTileForwards();
                for(Player player1 : GameState.gameMode.getPlayers()) {
                    new MovementModel(player1.getPlayerController().getClientInstance(),
                            player.getPlayerController().getPlayerID(),
                            player.getPlayerRobot().getCurrentTile().getCoordinate().getX(),
                            player.getPlayerRobot().getCurrentTile().getCoordinate().getY()).send();
                }
                break;

            case "Move2":
                player.moveRobotOneTileForwards();
                player.moveRobotOneTileForwards();
                for(Player player1 : GameState.gameMode.getPlayers()) {
                    new MovementModel(player1.getPlayerController().getClientInstance(),
                            player.getPlayerController().getPlayerID(),
                            player.getPlayerRobot().getCurrentTile().getCoordinate().getX(),
                            player.getPlayerRobot().getCurrentTile().getCoordinate().getY()).send();
                }
                break;

            case "Move3":
                player.moveRobotOneTileForwards();
                player.moveRobotOneTileForwards();
                player.moveRobotOneTileForwards();
                for(Player player1 : GameState.gameMode.getPlayers()) {
                    new MovementModel(player1.getPlayerController().getClientInstance(),
                            player.getPlayerController().getPlayerID(),
                            player.getPlayerRobot().getCurrentTile().getCoordinate().getX(),
                            player.getPlayerRobot().getCurrentTile().getCoordinate().getY()).send();
                }
                break;

            case "BackUp":
                player.moveRobotOneTileBackwards();
                for(Player player1 : GameState.gameMode.getPlayers()) {
                    new MovementModel(player1.getPlayerController().getClientInstance(),
                            player.getPlayerController().getPlayerID(),
                            player.getPlayerRobot().getCurrentTile().getCoordinate().getX(),
                            player.getPlayerRobot().getCurrentTile().getCoordinate().getY()).send();
                }
                break;

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
