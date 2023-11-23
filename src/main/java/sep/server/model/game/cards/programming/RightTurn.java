package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class RightTurn extends AProgrammingCard implements IPlayableCard {

    public RightTurn(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Player player, Robot robot, int currentRoundNumber) {

        String currentDirection = robot.getDirection();
        String newDirection;

        switch (currentDirection) {
            case "NORTH":
                newDirection = "EAST";
                break;
            case "EAST":
                newDirection = "SOUTH";
                break;
            case "SOUTH":
                newDirection = "WEST";
                break;
            case "WEST":
                newDirection = "NORTH";
                break;
            default:
                newDirection = currentDirection;
                break;
        }

        robot.setDirection(newDirection);
    }
}
