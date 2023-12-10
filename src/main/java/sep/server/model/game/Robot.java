package sep.server.model.game;

import sep.server.json.game.effects.MovementModel;
import sep.server.json.game.effects.RebootDirectionModel;
import sep.server.json.game.effects.RebootModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

    public void reboot() {
        for (Player player : GameState.gameMode.getPlayers()) {
            if (this.equals(player.getPlayerRobot()) && GameState.gameMode.getSpamDeck().size() >= 2) {
                Tile sourceTile = this.getCurrentTile();
                Tile restartPoint = null;
                //TODO get rebootDirection from client
                String rebootDirection = null;

                player.getDiscardPile().add(GameState.gameMode.getSpamDeck().get(0));
                player.getDiscardPile().add(GameState.gameMode.getSpamDeck().get(0));

                for (int i = 0; i < player.getRegisters().length; i++) {
                    player.getDiscardPile().add(player.getCardByRegisterIndex(i));
                    player.setCardInRegister(i, null);
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
                    for(Player player1 : GameState.gameMode.getPlayers()) {
                        new RebootModel(player1.getPlayerController().getClientInstance(),
                                player.getPlayerController().getPlayerID()).send();
                        new RebootDirectionModel(player1.getPlayerController().getClientInstance(),
                                rebootDirection).send();
                        new MovementModel(player1.getPlayerController().getClientInstance(),
                                player.getPlayerController().getPlayerID(),
                                restartPoint.getCoordinate().getX(),
                                restartPoint.getCoordinate().getY()).send();
                    }
                }
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
}
