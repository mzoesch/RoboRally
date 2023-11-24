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
        ArrayList<ArrayList<Tile>> board = new ArrayList<ArrayList<Tile>>();
        ArrayList<Tile> arrayListY = new ArrayList<>();
        ArrayList<FieldType> fieldtypes = new ArrayList<FieldType>();

        // (0,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,0), fieldtypes));
        fieldtypes.clear();

        // (0,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,1), fieldtypes));
        fieldtypes.clear();

        // (0,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,2), fieldtypes));
        fieldtypes.clear();

        // (0,3)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(0,3), fieldtypes));
        fieldtypes.clear();

        // (0,4)
        fieldtypes.add(new Antenna("EAST"));
        arrayListY.add(new Tile("StartA",new Coordinate(0,4), fieldtypes));
        fieldtypes.clear();

        // (0,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,5), fieldtypes));
        fieldtypes.clear();

        // (0,6)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(0,6), fieldtypes));
        fieldtypes.clear();

        // (0,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,7), fieldtypes));
        fieldtypes.clear();

        // (0,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,8), fieldtypes));
        fieldtypes.clear();

        // (0,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (1,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(1,0), fieldtypes));
        fieldtypes.clear();

        // (1,1)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(1,1), fieldtypes));
        fieldtypes.clear();

        // (1,2)
        fieldtypes.add(new Wall(new String[]{"NORTH"}));
        arrayListY.add(new Tile("StartA",new Coordinate(1,2), fieldtypes));
        fieldtypes.clear();

        // (1,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(1,3), fieldtypes));
        fieldtypes.clear();

        // (1,4)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(1,4), fieldtypes));
        fieldtypes.clear();

        // (1,5)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(1,5), fieldtypes));
        fieldtypes.clear();

        // (1,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(1,6), fieldtypes));
        fieldtypes.clear();

        // (1,7)
        fieldtypes.add(new Wall(new String[]{"SOUTH"}));
        arrayListY.add(new Tile("StartA",new Coordinate(1,7), fieldtypes));
        fieldtypes.clear();

        // (1,8)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(1,8), fieldtypes));
        fieldtypes.clear();

        // (1,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(1,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (2,0)
        fieldtypes.add(new ConveyorBelt(1, "EAST", new String[]{"WEST"}));
        arrayListY.add(new Tile("StartA",new Coordinate(2,0), fieldtypes));
        fieldtypes.clear();

        // (2,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,1), fieldtypes));
        fieldtypes.clear();

        // (2,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,2), fieldtypes));
        fieldtypes.clear();

        // (2,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,3), fieldtypes));
        fieldtypes.clear();

        // (2,4)
        fieldtypes.add(new Wall(new String[]{"EAST"}));
        arrayListY.add(new Tile("StartA",new Coordinate(2,4), fieldtypes));
        fieldtypes.clear();

        // (2,5)
        fieldtypes.add(new Wall(new String[]{"EAST"}));
        arrayListY.add(new Tile("StartA",new Coordinate(2,5), fieldtypes));
        fieldtypes.clear();

        // (2,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,6), fieldtypes));
        fieldtypes.clear();

        // (2,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,7), fieldtypes));
        fieldtypes.clear();

        // (2,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,8), fieldtypes));
        fieldtypes.clear();

        // (2,9)
        fieldtypes.add(new ConveyorBelt(1, "EAST", new String[]{"WEST"}));
        arrayListY.add(new Tile("StartA",new Coordinate(2,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        return board;
    }

    public ArrayList<ArrayList<Tile>> buildTestA(){
        ArrayList<ArrayList<Tile>> board = new ArrayList<ArrayList<Tile>>();
        ArrayList<Tile> arrayListY = new ArrayList<Tile>();
        ArrayList<FieldType> fieldtypes = new ArrayList<FieldType>();

        //(0,0)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[]{"right", "bottom"}));
        arrayListY.add(new Tile("1B", new Coordinate(0,0),fieldtypes));
        fieldtypes.clear();

        //(0,1)
        fieldtypes.add(new PushPanel("left", new int[]{2, 4}));
        arrayListY.add(new Tile("1B", new Coordinate(0,1),fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        //(1,0)
        fieldtypes.add(new Wall(new String[]{"top", "right"}));
        fieldtypes.add(new Laser("bottom", 2 ));
        arrayListY.add(new Tile("4A", new Coordinate(1,0),fieldtypes));
        fieldtypes.clear();

        //(1,1)
        arrayListY.add(new Tile("4A", new Coordinate(1,0),fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        return board;

    }
}
