package sep.server.model.game.courseBuilder;

import sep.server.model.game.Tile;
import sep.server.model.game.tiles.*;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class CourseBuilder {

    public CourseBuilder() {
    }

    public  ArrayList<ArrayList<Field>> buildCourse(){
        ArrayList<ArrayList<Field>> boardStartA = buildBoard("StartA");
        ArrayList<ArrayList<Field>> board5B = buildBoard("5B");

        return appendRight(boardStartA, board5B);
    }

    public  ArrayList<ArrayList<Field>> appendRight(ArrayList<ArrayList<Field>> leftBoard, ArrayList<ArrayList<Field>> rightBoard){

        return null;
    }

    public ArrayList<ArrayList<Field>> buildBoard(String boardName){

        switch(boardName){
            case("Test") -> {
                buildTestA();

            }
            case("StartA") -> {
                return buildStartA();
            }

        }
        return null;
    }

    public ArrayList<ArrayList<Field>> buildStartA()
    {
        return null;
    }

    public ArrayList<ArrayList<Tile>> buildTestA(){
        ArrayList<ArrayList<Tile>> board = new ArrayList<ArrayList<Tile>>();
        ArrayList<Tile> arrayListY = new ArrayList<Tile>();
        ArrayList<FieldType> fieldtypes = new ArrayList<FieldType>();

        //(0,0)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[]{"right", "bottom"}));
        arrayListY.add(new Tile("1B", new Coordinate(0,0),fieldtypes));
        fieldtypes = new ArrayList<>();

        //(0,1)
        fieldtypes.add(new PushPanel("left", new int[]{2, 4}));
        arrayListY.add(new Tile("1B", new Coordinate(0,1),fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<Tile>();

        //(1,0)
        fieldtypes.add(new Wall(new String[]{"top", "right"}));
        fieldtypes.add(new Laser("bottom", 2 ));
        arrayListY.add(new Tile("4A", new Coordinate(1,0),fieldtypes));
        fieldtypes = new ArrayList<>();

        //(1,1)
        arrayListY.add(new Tile("4A", new Coordinate(1,0),fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        return board;

    }

}
