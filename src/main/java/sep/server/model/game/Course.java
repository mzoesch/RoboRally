package sep.server.model.game;

import sep.server.model.game.courseBuilder.CourseBuilder;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Course {

    private ArrayList<ArrayList<Field>> course;

    public Course(String courseName) {
        CourseBuilder courseBuilder = new CourseBuilder();
        course = courseBuilder.buildCourse();
    }

    public void activateBoard() {}
}
