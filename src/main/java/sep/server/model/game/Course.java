package sep.server.model.game;

import sep.server.model.game.builder.CourseBuilder;
import sep.server.model.game.tiles.*;
import java.util.ArrayList;

/**
 * Klasse, in der das aktuelle Spielbrett gespeichert ist
 */
public class Course {

    private final ArrayList<ArrayList<Tile>> course;

    private final String startingDirection;

    /**
     * Erstellt das Spielbrett abhängig vom übergebenen Kartennamen
     *
     * @param courseName Name des entsprechenden Spielfelds
     */
    public Course(String courseName) {
        CourseBuilder courseBuilder = new CourseBuilder();
        course = courseBuilder.buildCourse(courseName);
        startingDirection = courseBuilder.getStartingPosition(courseName);
    }

    public void activateBoard() {
    }

    /**
     * Updates the position of the robot on the game board.
     *
     * @param robot The robot who get updated
     * @param newCoordinate The new coordinate where the robot is moved
     */
    public void updateRobotPosition(Robot robot, Coordinate newCoordinate) {

        //Update Old Tile in Course
        getTileByCoordinate(robot.getCurrentTile().getCoordinate()).setRobot(null);

        //Update New Tile in Course
        getTileByCoordinate(newCoordinate).setRobot(robot);

        //Update Robot
        robot.setCurrentTile(getTileByCoordinate(newCoordinate));

    }

    /**
     * Checks if the  coordinate is within the bounds of the game board.
     *
     * @param coordinate The coordinate to be checked
     * @return True if the coordinate is within the game board bounds, otherwise false.
     */
    public boolean isCoordinateWithinBounds(Coordinate coordinate) {
        int x = coordinate.getXCoordinate();
        int y = coordinate.getYCoordinate();

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
        return course.get(coordinate.getXCoordinate()).get(coordinate.getYCoordinate());
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
        return course.get(coordinate.getXCoordinate()).get(coordinate.getYCoordinate());
    }

    public Tile getRightNeighbor(Tile tile){
        Coordinate coordinate = tile.getCoordinate().getRightNeighbor();
        return course.get(coordinate.getXCoordinate()).get(coordinate.getYCoordinate());
    }

    public Tile getBottomNeighbor(Tile tile){
        Coordinate coordinate = tile.getCoordinate().getBottomNeighbor();
        return course.get(coordinate.getXCoordinate()).get(coordinate.getYCoordinate());
    }

    public Tile getLeftNeighbor(Tile tile){
        Coordinate coordinate = tile.getCoordinate().getLeftNeighbor();
        return course.get(coordinate.getXCoordinate()).get(coordinate.getYCoordinate());
    }

    public String getStartingDirection() {
        return startingDirection;
    }

    public ArrayList<ArrayList<Tile>> getCourse() {
        return course;
    }
}
