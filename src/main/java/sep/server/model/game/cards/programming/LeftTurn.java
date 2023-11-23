package sep.server.model.game.cards.programming;

import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class LeftTurn extends AProgrammingCard implements IPlayableCard {
    public LeftTurn(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Robot robot) {

        String currentDirection = robot.getDirection();
        String newDirection;

        switch (currentDirection) {
            case "NORTH":
                newDirection = "WEST";
                break;
            case "EAST":
                newDirection = "NORTH";
                break;
            case "SOUTH":
                newDirection = "EAST";
                break;
            case "WEST":
                newDirection = "SOUTH";
                break;
            default:
                newDirection = currentDirection;
                break;
        }

        robot.setDirection(newDirection);
    }
}
