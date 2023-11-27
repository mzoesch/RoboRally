package sep.server.model.game;

import sep.server.model.game.courseBuilder.CourseBuilder;
import sep.server.model.game.tiles.*;
import java.util.ArrayList;

/**
 * Klasse, in der das aktuelle Spielbrett gespeichert ist
 */
public class Course {

    private final ArrayList<ArrayList<Tile>> course;

    /**
     * Erstellt das Spielbrett abhängig vom übergebenen Kartennamen
     * @param courseName Name des entsprechenden Spielfelds
     */
    public Course(String courseName) {
        CourseBuilder courseBuilder = new CourseBuilder();
        course = courseBuilder.buildCourse(courseName);
    }

    public void activateBoard() {}

    public Tile getTileByCoordinate(Coordinate coordinate){
        return course.get(coordinate.getXCoordinate()).get(coordinate.getYCoordinate());
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
}
