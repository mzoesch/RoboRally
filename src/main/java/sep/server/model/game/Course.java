package sep.server.model.game;

import sep.server.model.game.courseBuilder.CourseBuilder;
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
}
