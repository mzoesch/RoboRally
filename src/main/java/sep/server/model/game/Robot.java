package sep.server.model.game;

import sep.server.json.game.effects.MovementModel;
import sep.server.json.game.effects.RebootDirectionModel;
import sep.server.json.game.effects.RebootModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.tiles.Coordinate;

import java.util.Objects;

public class Robot {
    private static final Logger l = LogManager.getLogger(GameState.class);

    String direction;
    private final Course course;

    /** @deprecated Make to gateway method */
    private Tile currentTile;

    public Robot(Course course) {
        this.course = course;
        currentTile = null;
    }

    public String getDirection()
    {
        return direction;
    }

    public void setDirection(String direction)
    {
        this.direction = direction;
    }

    public Tile getCurrentTile()
    {
        return currentTile;
    }

    public void setCurrentTile(Tile currentTile)
    {
        this.currentTile = currentTile;
    }

    public Course getCourse()
    {
        return course;
    }

    /**
     * Validates a passed starting point: Is it on the course? Is it a starting point? Is it still free?
     * @param x x coordinate of the starting point
     * @param y y coordinate of the starting point
     * @return 1 if successful, 0 if not on course, -1 if occupied by another robot, -2 if not a starting point
     */
    public int validStartingPoint(int x, int y) {
        Tile chosenStart = course.getTileByNumbers(x, y);
        if (chosenStart != null) {
            if (chosenStart.isOccupied()) {
                return -1;
            } else if (!chosenStart.isStartingPoint()) {
                return -2;
            } else
                return 1;
        } else {
            return 0;
        }
    }

    public void setStartingPoint(int x, int y) {
        Tile chosenStart = course.getTileByNumbers(x, y);
        direction = setStartDirection();
        currentTile = chosenStart;
    }

    private String setStartDirection() {
        switch (course.getStartingTurningDirection()) {
            case ("clockwise") -> {
                return "right";
            }
            case ("counterclockwise") -> {
                return "left";
            }
            case ("") -> {
                return "top";
            }
        }
        return "bottom";
    }

    /**
     * Moves the robot one tile based on the given direction.
     * Updates the robot's position.
     * @param forward True if the robot should move forwards, false if backwards.
     */
    public void moveRobotOneTile(final boolean forward) {
        final int dir = forward ? 1 : -1;
        final Coordinate currentCoordinate = this.getCurrentTile().getCoordinate();
        Coordinate tCoordinate = null;

        switch (this.getDirection().toLowerCase()) {
            case "north", "top" -> tCoordinate = new Coordinate(currentCoordinate.getX(), currentCoordinate.getY() - dir);
            case "south", "bottom" -> tCoordinate = new Coordinate(currentCoordinate.getX(), currentCoordinate.getY() + dir);
            case "east", "right" -> tCoordinate = new Coordinate(currentCoordinate.getX() + dir, currentCoordinate.getY());
            case "west", "left" -> tCoordinate = new Coordinate(currentCoordinate.getX() - dir, currentCoordinate.getY());
            default -> l.error("Player {}'s robot has an invalid direction: {}", this.determineRobotOwner().getPlayerController().getPlayerID(), this.getDirection());
        }

        if (tCoordinate == null) {
            l.error("Player {}'s robot has an invalid direction: {}", this.determineRobotOwner().getPlayerController().getPlayerID(), this.getDirection());
            return;
        }

        l.trace("Player {}'s robot wants to move from ({}, {}) to ({}, {}).", this.determineRobotOwner().getPlayerController().getPlayerID(), currentCoordinate.getX(), currentCoordinate.getY(), tCoordinate.getX(), tCoordinate.getY());

        if (!this.getCourse().isCoordinateWithinBounds(tCoordinate)) {
            l.debug("Player {}'s robot moved to {} and fell off the board. Rebooting . . .", this.determineRobotOwner().getPlayerController().getPlayerID(), tCoordinate.toString());
            this.reboot();
            return;
        }

        if (!this.isTraversable(this.getCurrentTile(), this.getCourse().getTileByCoordinate(tCoordinate))) {
            l.debug("Player {}'s robot wanted to traverse an impassable tile [from {} to {}]. Ignoring.", this.determineRobotOwner().getPlayerController().getPlayerID(), currentCoordinate.toString(), tCoordinate.toString());
            return;
        }

        this.getCourse().updateRobotPosition(this, tCoordinate);
        l.debug("Player {}'s robot moved [from {} to {}].", this.determineRobotOwner().getPlayerController().getPlayerID(), currentCoordinate.toString(), tCoordinate.toString());
    }

    /**
     * Moves the robot one tile forwards based on the robot's current direction.
     * Updates the robot's position.
     */
    public void moveRobotOneTileForwards() {
        moveRobotOneTile(true);
    }

    /**
     * Moves the robot one tile backwards based on the robot's current direction.
     * Updates the robot's position.
     */
    public void moveRobotOneTileBackwards() {
        moveRobotOneTile(false);
    }

    /**
     * Rotates the robot 90 degrees to the right
     * Updates the robot's direction
     */
    public void rotateRobotOnTileToTheRight(){
        String currentDirection = this.getDirection();
        String newDirection;

        switch (currentDirection.toLowerCase()) {
            case "north", "top" -> newDirection = "right";
            case "east", "right" -> newDirection = "bottom";
            case "south", "bottom" -> newDirection = "left";
            case "west", "left" -> newDirection = "top";
            default -> {
                l.error("Player {}'s robot has an invalid direction: {}", this.determineRobotOwner().getPlayerController().getPlayerID(), this.getDirection());
                return;
            }
        }
        this.setDirection(newDirection);
    }

    public void reboot() {
        Player robotOwner = determineRobotOwner();
        Tile sourceTile = this.getCurrentTile();
        Tile restartPoint = null;
        //TODO get rebootDirection from client
        String rebootDirection = null;

        if(GameState.gameMode.getSpamDeck().size() >= 2) {
            robotOwner.getDiscardPile().add(GameState.gameMode.getSpamDeck().get(0));
            robotOwner.getDiscardPile().add(GameState.gameMode.getSpamDeck().get(0));
        }

        for (int i = 0; i < robotOwner.getRegisters().length; i++) {
            robotOwner.getDiscardPile().add(robotOwner.getCardByRegisterIndex(i));
            robotOwner.setCardInRegister(i, null);
        }

        if(Objects.equals(GameState.getCourseName(), "DizzyHighway")) {
            restartPoint = course.getTileByNumbers(4,3);
            //TODO cover case when robot rebooted on Start Board
        }

        this.setCurrentTile(restartPoint);

        if(rebootDirection == null) {
            rebootDirection = "top";
        }
        this.setDirection(rebootDirection);

        if(restartPoint != null) {
            for(Player player : GameState.gameMode.getPlayers()) {
                new RebootModel(player.getPlayerController().getClientInstance(),
                        robotOwner.getPlayerController().getPlayerID()).send();
                new RebootDirectionModel(player.getPlayerController().getClientInstance(),
                        rebootDirection).send();
                new MovementModel(player.getPlayerController().getClientInstance(),
                        robotOwner.getPlayerController().getPlayerID(),
                        restartPoint.getCoordinate().getX(),
                        restartPoint.getCoordinate().getY()).send();
            }
        }
    }

    /**
     * The following method checks if a tile is traversable meaning if it is an antenna, if it has a wall,
     * or if it is occupied by an unmovable robot.
     * @param source tile the robot is coming from
     * @param t1 tile the robot is moving to
     * @return true if traversable, false if not
     */
    public boolean isTraversable(final Tile source, final Tile t1) {
        if (t1.hasAntennaModifier()) {
            l.trace("Robot is unmovable because of the antenna modifier");
            return false;
        }

        if (source.hasWallModifier()) {
            if (source.isEastOf(t1) && source.isWallWest()) {
                l.trace("Robot cannot traverse west because of a wall modifier.");
                return false;
            }

            if (source.isWestOf(t1) && source.isWallEast()) {
                l.trace("Robot cannot traverse east because of a wall modifier.");
                return false;
            }

            if (source.isNorthOf(t1) && source.isWallSouth()) {
                l.trace("Robot cannot traverse south because of a wall modifier.");
                return false;
            }

            if (source.isSouthOf(t1) && source.isWallNorth()) {
                l.trace("Robot cannot traverse north because of a wall modifier.");
                return false;
            }
        }

        if (t1.hasWallModifier()) {
            if (source.isEastOf(t1) && t1.isWallEast()) {
                l.trace("Robot cannot traverse west because of a wall modifier.");
                return false;
            }

            if (source.isWestOf(t1) && t1.isWallWest()) {
                l.trace("Robot cannot traverse east because of a wall modifier.");
                return false;
            }

            if (source.isNorthOf(t1) && t1.isWallNorth()) {
                l.trace("Robot cannot traverse south because of a wall modifier.");
                return false;
            }

            if (source.isSouthOf(t1) && t1.isWallSouth()) {
                l.trace("Robot cannot traverse north because of a wall modifier.");
                return false;
            }
        }

        if (t1.hasUnmovableRobot()){
            l.trace("Robot is unmovable because of another unmovable robot");
            return false;
        }
        return true;
    }

    /**
     * The following method is used to find the owner (player) of a robot.
     * @return owner
     */
    public Player determineRobotOwner() {
        for(Player player : GameState.gameMode.getPlayers()) {
            if(player.getPlayerRobot() == this) {
                return player;
            }
        }
        return null;
    }
}
