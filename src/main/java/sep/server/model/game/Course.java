package sep.server.model.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.model.game.builder.CourseBuilder;
import sep.server.model.game.tiles.*;
import java.util.ArrayList;

/**
 * Class saving current game course
 */
public class Course {
    private static final Logger l = LogManager.getLogger(GameState.class);

    private final ArrayList<ArrayList<Tile>> course;

    private final String startingTurningDirection;

    /**
     * Creates course depending on course name passed.
     * @param courseName name of corresponding course
     */
    public Course(String courseName) {
        super();
        CourseBuilder courseBuilder = new CourseBuilder();
        course = courseBuilder.buildCourse(courseName);
        startingTurningDirection = courseBuilder.getStartingTurningDirection(courseName);
    }

    /**
     * Updates the position of the robot on the game board.
     * @param robot The robot who get updated
     * @param newCoordinate The new coordinate where the robot is moved
     */
    public void updateRobotPosition(Robot robot, Coordinate newCoordinate) {
        getTileByCoordinate(robot.getCurrentTile().getCoordinate()).setRobot(null);
        getTileByCoordinate(newCoordinate).setRobot(robot);
        robot.setCurrentTile(getTileByCoordinate(newCoordinate));
    }

    /**
     * Checks if the  coordinate is within the bounds of the game board.
     * @param coordinate The coordinate to be checked
     * @return True if the coordinate is within the game board bounds, otherwise false.
     */
    public boolean isCoordinateWithinBounds(Coordinate coordinate) {
        int x = coordinate.getX();
        int y = coordinate.getY();
        return x >= 0 && x < course.size() && y >= 0 && y < course.get(0).size();
    }

    /**
     * Checks if coordinate compatible with passed x and y values is within the bounds of the game board.
     * @param x X-Coordinate
     * @param y Y-Coordinate
     * @return True if corresponding coordinate is within the game board bounds, false if not
     */
    public boolean areCoordinatesWithinBounds(int x, int y) {
        return x >= 0 && x < course.size() && y >= 0 && y < course.get(0).size();
    }

    public Tile getTileByCoordinate(Coordinate coordinate){
        return course.get(coordinate.getX()).get(coordinate.getY());
    }

    public Tile getTileByNumbers(int x, int y){
        try{
            return course.get(x).get(y);
        }
        catch(ArrayIndexOutOfBoundsException e){
            return null;
        }
    }

    public Tile getTopNeighbor(Tile tile){
        Coordinate coordinate = tile.getCoordinate().getTopNeighbor();
        return course.get(coordinate.getX()).get(coordinate.getY());
    }

    public Tile getRightNeighbor(Tile tile){
        Coordinate coordinate = tile.getCoordinate().getRightNeighbor();
        return course.get(coordinate.getX()).get(coordinate.getY());
    }

    public Tile getBottomNeighbor(Tile tile){
        Coordinate coordinate = tile.getCoordinate().getBottomNeighbor();
        return course.get(coordinate.getX()).get(coordinate.getY());
    }

    public Tile getLeftNeighbor(Tile tile){
        Coordinate coordinate = tile.getCoordinate().getLeftNeighbor();
        return course.get(coordinate.getX()).get(coordinate.getY());
    }

    public String getStartingTurningDirection() {
        return startingTurningDirection;
    }

    public ArrayList<ArrayList<Tile>> getCourse() {
        return course;
    }

    public Course getThis(){
        return this;
    }

    public Coordinate getPriorityAntennaCoordinate() {
        for (ArrayList<Tile> ts : this.course) {
            for (Tile t : ts) {
                if (t.getFieldTypes().stream().anyMatch(elem -> elem instanceof Antenna)) {
                    return t.getCoordinate();
                }
            }
        }
        return null;
    }

}
