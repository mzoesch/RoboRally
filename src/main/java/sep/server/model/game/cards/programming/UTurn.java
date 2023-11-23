package sep.server.model.game.cards.programming;

import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class UTurn extends AProgrammingCard implements IPlayableCard {

    public UTurn(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Robot robot) {

        String currentDirection = robot.getDirection();
        String newDirection;

        switch (currentDirection) {
            case "NORTH":
                newDirection = "SOUTH";
                break;
            case "SOUTH":
                newDirection = "NORTH";
                break;
            case "EAST":
                newDirection = "WEST";
                break;
            case "WEST":
                newDirection = "EAST";
                break;
            default:
                newDirection = currentDirection;
                break;
        }

        robot.setDirection(newDirection);
    }
}
