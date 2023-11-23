package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class BackUp extends AProgrammingCard implements IPlayableCard {
    public BackUp(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Player player, Robot robot) {

        // Assuming the game board is numbered from top to bottom (decrement the Y-coordinate when moving north)

        String currentDirection = robot.getDirection();

        switch (currentDirection) {
            case "NORTH":
                robot.setPositionY(robot.getPositionY() + 1);
                break;
            case "SOUTH":
                robot.setPositionY(robot.getPositionY() - 1);
                break;
            case "EAST":
                robot.setPositionX(robot.getPositionX() - 1);
                break;
            case "WEST":
                robot.setPositionX(robot.getPositionX() + 1);
                break;
            default:
                break;
        }
    }
}
