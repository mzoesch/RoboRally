package sep.server.model.game.cards.programming;

import sep.server.model.game.Player;
import sep.server.model.game.Robot;
import sep.server.model.game.cards.IPlayableCard;

public class MoveIII extends AProgrammingCard implements IPlayableCard {

    public MoveIII(String cardType) {
        super(cardType);
    }

    @Override
    public void playCard(Player player, Robot robot, int currentRoundNumber) {

        // Assuming the game board is numbered from top to bottom (decrement the Y-coordinate when moving north)

        String currentDirection = robot.getDirection();

        switch (currentDirection) {
            case "NORTH":
                robot.setPositionY(robot.getPositionY() - 3);
                break;
            case "SOUTH":
                robot.setPositionY(robot.getPositionY() + 3);
                break;
            case "EAST":
                robot.setPositionX(robot.getPositionX() + 3);
                break;
            case "WEST":
                robot.setPositionX(robot.getPositionX() - 3);
                break;
            default:
                break;
        }
    }
}
