package sep.server.model.game.builder;

import sep.server.model.game.Tile;
import sep.server.model.game.tiles.*;

import java.util.ArrayList;

/**
 * Class creating the corresponding course
 */
public class CourseBuilder {

    public CourseBuilder() {
    }

    /**
     * Builds the corresponding course from the individual boards
     * @param courseName name of the course
     * @return complete course as ArrayList<ArrayList<Tile>>
     */
    public  ArrayList<ArrayList<Tile>> buildCourse(String courseName){
        switch(courseName){
            case("Dizzy Highway") -> {
                ArrayList<ArrayList<Tile>> boardStartA = buildBoard("StartA");
                ArrayList<ArrayList<Tile>> board5B = buildBoard("5B");
                ArrayList<ArrayList<Tile>> entireCourse = appendRight(boardStartA, board5B);
                settingCoordinates(entireCourse);
                return entireCourse;
            }
            case("Lost Bearings") -> {
                ArrayList<ArrayList<Tile>> boardStartA = buildBoard("StartA");
                ArrayList<ArrayList<Tile>> board1A = buildBoard("1A");
                ArrayList<ArrayList<Tile>> entireCourse = appendRight(boardStartA, board1A);
                settingCoordinates(entireCourse);
                return entireCourse;
            }
            case("Extra Crispy") -> {
                ArrayList<ArrayList<Tile>> boardStartA = buildBoard("StartA");
                ArrayList<ArrayList<Tile>> board4A = buildBoard("4A");
                ArrayList<ArrayList<Tile>> entireCourse = appendRight(boardStartA, board4A);
                settingCoordinates(entireCourse);
                return entireCourse;
            }
            case("Test") -> {
                return buildBoard("Test");
            }
        }

        //TODO Notlösung, falls CourseName nicht richtig übergeben wird
        ArrayList<ArrayList<Tile>> boardStartA = buildBoard("StartA");
        ArrayList<ArrayList<Tile>> board5B = buildBoard("5B");
        ArrayList<ArrayList<Tile>> entireCourse = appendRight(boardStartA, board5B);
        settingCoordinates(entireCourse);
        return entireCourse;
    }

    /**
     * Is passed a course and sets the neighbour coordinates for all tiles
     * @param course passed course
     */
    public  void settingCoordinates(ArrayList<ArrayList<Tile>> course){
        for(int i = 0; i <  course.size(); i++){

            ArrayList<Tile> courseYRow = course.get(i);
            for(int a = 0; a < courseYRow.size(); a++){
                Tile tile = courseYRow.get(a);
                tile.setCoordinate(i,a);

                int xCoordinate = tile.getCoordinate().getX();
                int yCoordinate = tile.getCoordinate().getY();

                //topNeighbor
                try{
                    Coordinate topNeighbor = courseYRow.get(yCoordinate-1).getCoordinate();
                    tile.getCoordinate().setTopNeighbor(topNeighbor);
                }
                catch (Exception indexOutOfBoundsException){
                    tile.getCoordinate().setTopNeighbor(null);
                }

                //rightNeighbor
                try{
                    Coordinate rightNeighbor = course.get(xCoordinate+1).get(yCoordinate).getCoordinate();
                    tile.getCoordinate().setRightNeighbor(rightNeighbor);
                }
                catch (Exception indexOutOfBoundsException){
                    tile.getCoordinate().setRightNeighbor(null);
                }

                //bottomNeighbor
                try{
                    Coordinate bottomNeighbor = courseYRow.get(yCoordinate+1).getCoordinate();
                    tile.getCoordinate().setBottomNeighbor(bottomNeighbor);
                }
                catch (Exception indexOutOfBoundsException){
                    tile.getCoordinate().setBottomNeighbor(null);
                }

                //rightNeighbor
                try{
                    Coordinate leftNeighbor = course.get(xCoordinate-1).get(yCoordinate).getCoordinate();
                    tile.getCoordinate().setLeftNeighbor(leftNeighbor);
                }
                catch (Exception indexOutOfBoundsException){
                    tile.getCoordinate().setLeftNeighbor(null);
                }
            }
        }
    }

    public String getStartingTurningDirection(String courseName){
        switch(courseName){
            case("Dizzy Highway") -> {
                return "clockwise";}
        }
        return "clockwise";
    }
    /**
     * Appends a board to the right of another board. Only works for boards of the same length.
     * @param leftBoard left board
     * @param rightBoard right board (is appended)
     * @return ArrayList that represents the entire board
     */
    public  ArrayList<ArrayList<Tile>> appendRight(ArrayList<ArrayList<Tile>> leftBoard, ArrayList<ArrayList<Tile>> rightBoard){

        leftBoard.addAll(rightBoard);
        return leftBoard;
    }

    /**
     * Appends a board to the bottom of another board. Only works for boards of the same width.
     * @param topBoard top board
     * @param bottomBoard bottom board (is appended)
     * @return ArrayList that represents the entire board
     */
    public  ArrayList<ArrayList<Tile>> appendBottom(ArrayList<ArrayList<Tile>> topBoard, ArrayList<ArrayList<Tile>> bottomBoard){
        for(int i = 0; i< topBoard.size(); i++){
            topBoard.get(i).addAll(bottomBoard.get(i));
        }
        return topBoard;
    }

    /**
     * Builds a board depending on board name
     * @param boardName name of the board to be created
     * @return created board
     */
    public ArrayList<ArrayList<Tile>> buildBoard(String boardName){
        switch(boardName){
            case("Test") -> {
                return buildTestA();
            }
            case("StartA") -> {
                return buildStartA();
            }
            case("5B") -> {
                return build5B();
            }
            case("1A") -> {
                return build1A();
            }
            case("4A") -> {
                return build4A();
            }
        }
        return null;
    }

    /**
     * Builds the test board from protocol v0.1
     * @return test board as ArrayList
     */
    public ArrayList<ArrayList<Tile>> buildTestA(){
        ArrayList<ArrayList<Tile>> board = new ArrayList<>();
        ArrayList<Tile> arrayListY = new ArrayList<>();
        ArrayList<FieldType> fieldtypes = new ArrayList<>();

        //(0,0)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[]{"right", "bottom"}));
        arrayListY.add(new Tile("1B", new Coordinate(0,0),fieldtypes));
        fieldtypes = new ArrayList<>();

        //(0,1)
        fieldtypes.add(new PushPanel("left", new int[]{2, 4}));
        arrayListY.add(new Tile("2B", new Coordinate(0,1),fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        //(1,0)
        fieldtypes.add(new Wall(new String[]{"top", "right"}));
        fieldtypes.add(new Laser("bottom", 2 ));
        arrayListY.add(new Tile("4A", new Coordinate(1,0),fieldtypes));
        fieldtypes = new ArrayList<>();

        //(1,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A", new Coordinate(1,1),fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);

        return board;

    }

    /**
     * Builds board StartA (part of DizzyHighway, Lost Bearings, Extra Crispy and Death Trap)
     * @return StartA as ArrayList
     */
    public ArrayList<ArrayList<Tile>> buildStartA()
    {
        ArrayList<ArrayList<Tile>> board = new ArrayList<>();
        ArrayList<Tile> arrayListY = new ArrayList<>();
        ArrayList<FieldType> fieldtypes = new ArrayList<>();

        // (0,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,3)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(0,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,4)
        fieldtypes.add(new Antenna("right"));
        arrayListY.add(new Tile("StartA",new Coordinate(0,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,6)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(0,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(0,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (1,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(1,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,1)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(1,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,2)
        fieldtypes.add(new Wall(new String[]{"top"}));
        arrayListY.add(new Tile("StartA",new Coordinate(1,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(1,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,4)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(1,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,5)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(1,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(1,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,7)
        fieldtypes.add(new Wall(new String[]{"bottom"}));
        arrayListY.add(new Tile("StartA",new Coordinate(1,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,8)
        fieldtypes.add(new StartPoint());
        arrayListY.add(new Tile("StartA",new Coordinate(1,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(1,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (2,0)
        fieldtypes.add(new ConveyorBelt(1, "right", new String[]{"left"}));
        arrayListY.add(new Tile("StartA",new Coordinate(2,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,4)
        fieldtypes.add(new Wall(new String[]{"right"}));
        arrayListY.add(new Tile("StartA",new Coordinate(2,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,5)
        fieldtypes.add(new Wall(new String[]{"right"}));
        arrayListY.add(new Tile("StartA",new Coordinate(2,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("StartA",new Coordinate(2,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,9)
        fieldtypes.add(new ConveyorBelt(1, "right", new String[]{"left"}));
        arrayListY.add(new Tile("StartA",new Coordinate(2,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        return board;
    }

    /**
     * Builds board 5B (part of DizzyHighway)
     * @return board 5B as ArrayList
     */
    public ArrayList<ArrayList<Tile>> build5B(){

        ArrayList<ArrayList<Tile>> board = new ArrayList<>();
        ArrayList<Tile> arrayListY = new ArrayList<>();
        ArrayList<FieldType> fieldtypes = new ArrayList<>();

        // (0,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,7)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[] {"left"}));
        arrayListY.add(new Tile("5B",new Coordinate(0,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,8)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[] {"left"}));
        arrayListY.add(new Tile("5B",new Coordinate(0,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,9)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(0,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (1,0)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,1)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top", "right"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,2)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,3)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,4)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,5)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,6)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,7)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top", "left"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,8)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[] {"top", "left"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(1,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (2,0)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("5B",new Coordinate(2,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,1)
        fieldtypes.add(new ConveyorBelt(2, "left", new String[] {"top", "right"}));
        arrayListY.add(new Tile("5B",new Coordinate(2,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,2)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(2,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,8)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[]{"left"}));
        arrayListY.add(new Tile("5B",new Coordinate(2,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (3,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,1)
        fieldtypes.add(new ConveyorBelt(2, "left", new String[] {"right"}));
        arrayListY.add(new Tile("5B",new Coordinate(3,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,3)
        fieldtypes.add(new Wall(new String[]{"top"}));
        arrayListY.add(new Tile("5B",new Coordinate(3,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,4)
        fieldtypes.add(new Wall(new String[]{"bottom"}));
        fieldtypes.add(new Laser("top", 1));
        arrayListY.add(new Tile("5B",new Coordinate(3,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,6)
        fieldtypes.add(new Wall(new String[]{"left"}));
        arrayListY.add(new Tile("5B",new Coordinate(3,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,8)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[]{"left"}));
        arrayListY.add(new Tile("5B",new Coordinate(3,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (4,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,1)
        fieldtypes.add(new ConveyorBelt(2, "left", new String[] {"right"}));
        arrayListY.add(new Tile("5B",new Coordinate(4,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,3)
        fieldtypes.add(new RestartPoint("bottom"));
        arrayListY.add(new Tile("5B",new Coordinate(4,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,5)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(4,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,6)
        fieldtypes.add(new Wall(new String[]{"right"}));
        fieldtypes.add(new Laser("left", 1));
        arrayListY.add(new Tile("5B",new Coordinate(4,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,8)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[]{"left"}));
        arrayListY.add(new Tile("5B",new Coordinate(4,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (5,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,1)
        fieldtypes.add(new ConveyorBelt(2, "left", new String[] {"right"}));
        arrayListY.add(new Tile("5B",new Coordinate(5,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,3)
        fieldtypes.add(new Wall(new String[]{"left"}));
        fieldtypes.add(new Laser("right", 1));
        arrayListY.add(new Tile("5B",new Coordinate(5,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,4)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(5,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,8)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[]{"left"}));
        arrayListY.add(new Tile("5B",new Coordinate(5,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (6,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,1)
        fieldtypes.add(new ConveyorBelt(2, "left", new String[] {"right"}));
        arrayListY.add(new Tile("5B",new Coordinate(6,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,3)
        fieldtypes.add(new Wall(new String[]{"right"}));
        arrayListY.add(new Tile("5B",new Coordinate(6,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,5)
        fieldtypes.add(new Wall(new String[]{"top"}));
        fieldtypes.add(new Laser("bottom", 1));
        arrayListY.add(new Tile("5B",new Coordinate(6,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,6)
        fieldtypes.add(new Wall(new String[]{"bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(6,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,8)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[]{"left"}));
        arrayListY.add(new Tile("5B",new Coordinate(6,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (7,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,1)
        fieldtypes.add(new ConveyorBelt(2, "left", new String[] {"right"}));
        arrayListY.add(new Tile("5B",new Coordinate(7,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,7)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(7,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,8)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[]{"bottom","left"}));
        arrayListY.add(new Tile("5B",new Coordinate(7,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,9)
        fieldtypes.add(new ConveyorBelt(2,"top", new String[]{"bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(7,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (8,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(8,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,1)
        fieldtypes.add(new ConveyorBelt(2, "left", new String[] {"right", "bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,2)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"right", "bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,3)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,4)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,5)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,6)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,7)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,8)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[]{"bottom","left"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,9)
        fieldtypes.add(new ConveyorBelt(2,"top", new String[]{"bottom"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,9), fieldtypes));
        fieldtypes = new ArrayList<>();


        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (9,0)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(9,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,1)
        fieldtypes.add(new ConveyorBelt(2, "left", new String[] {"right"}));
        arrayListY.add(new Tile("5B",new Coordinate(9,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,2)
        fieldtypes.add(new ConveyorBelt(2, "left", new String[] {"right"}));
        arrayListY.add(new Tile("5B",new Coordinate(9,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,3)
        fieldtypes.add(new CheckPoint(1));
        arrayListY.add(new Tile("5B",new Coordinate(9,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        return board;
    }

    /**
     * Builds board 1A (part of Lost Bearings)
     * @return board 1A as ArrayList
     */
    public ArrayList<ArrayList<Tile>> build1A() {

        ArrayList<ArrayList<Tile>> board = new ArrayList<>();
        ArrayList<Tile> arrayListY = new ArrayList<>();
        ArrayList<FieldType> fieldtypes = new ArrayList<>();

        // (0,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(0,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,1)
        fieldtypes.add(new ConveyorBelt(1, "left", new String[] {"right"}));
        arrayListY.add(new Tile("1A",new Coordinate(0,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(0,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(0,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(0,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(0,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(0,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(0,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(0,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,9)
        fieldtypes.add(new ConveyorBelt(1, "right", new String[] {"left"}));
        arrayListY.add(new Tile("1A",new Coordinate(0,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (1,0)
        fieldtypes.add(new ConveyorBelt(1, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("1A",new Coordinate(1,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,1)
        fieldtypes.add(new ConveyorBelt(1, "left", new String[] {"top"}));
        arrayListY.add(new Tile("1A",new Coordinate(1,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(1,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(1,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(1,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,5)
        fieldtypes.add(new CheckPoint(2));
        arrayListY.add(new Tile("1A",new Coordinate(1,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(1,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(1,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,8)
        fieldtypes.add(new ConveyorBelt(1, "bottom", new String[] {"left"}));
        arrayListY.add(new Tile("1A",new Coordinate(1,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,9)
        fieldtypes.add(new ConveyorBelt(1, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("1A",new Coordinate(1,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (2,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(2,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(2,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,2)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("1A",new Coordinate(2,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,3)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("1A",new Coordinate(2,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,4)
        fieldtypes.add(new Gear("left"));
        arrayListY.add(new Tile("1A",new Coordinate(2,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,5)
        fieldtypes.add(new Gear("right"));
        arrayListY.add(new Tile("1A",new Coordinate(2,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,6)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("1A",new Coordinate(2,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,7)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("1A",new Coordinate(2,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(2,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (2,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(2,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (3,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(3,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,1)
        fieldtypes.add(new ConveyorBelt(1, "left", new String[] {"right"}));
        arrayListY.add(new Tile("1A",new Coordinate(3,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,2)
        fieldtypes.add(new Pit());
        arrayListY.add(new Tile("1A",new Coordinate(3,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,3)
        fieldtypes.add(new Wall(new String[] {"left"}));
        arrayListY.add(new Tile("1A",new Coordinate(3,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(3,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(3,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,6)
        fieldtypes.add(new Wall(new String[] {"left"}));
        fieldtypes.add(new Laser("right", 4));
        arrayListY.add(new Tile("1A",new Coordinate(3,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,7)
        fieldtypes.add(new Pit());
        arrayListY.add(new Tile("1A",new Coordinate(3,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(3,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (3,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(3,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (4,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(4,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,1)
        fieldtypes.add(new ConveyorBelt(1, "left", new String[] {"right"}));
        arrayListY.add(new Tile("1A",new Coordinate(4,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(4,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,3)
        fieldtypes.add(new Laser("left", 1)); // TODO was muss der lasercount sein?
        arrayListY.add(new Tile("1A",new Coordinate(4,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,4)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("1A",new Coordinate(4,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,5)
        fieldtypes.add(new Gear("right"));
        arrayListY.add(new Tile("1A",new Coordinate(4,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,6)
        fieldtypes.add(new Laser("right", 1)); // TODO was muss der lasercount sein?
        arrayListY.add(new Tile("1A",new Coordinate(4,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(4,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(4,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (4,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(4,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (5,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(5,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,1)
        fieldtypes.add(new ConveyorBelt(1, "right", new String[] {"left"}));
        arrayListY.add(new Tile("1A",new Coordinate(5,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,2)
        fieldtypes.add(new CheckPoint(3));
        arrayListY.add(new Tile("1A",new Coordinate(5,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,3)
        fieldtypes.add(new Laser("left", 1));
        arrayListY.add(new Tile("1A",new Coordinate(5,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,4)
        fieldtypes.add(new Gear("left"));
        arrayListY.add(new Tile("1A",new Coordinate(5,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,5)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("1A",new Coordinate(5,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,6)
        fieldtypes.add(new Laser("right", 1));
        arrayListY.add(new Tile("1A",new Coordinate(5,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,7)
        fieldtypes.add(new CheckPoint(4));
        arrayListY.add(new Tile("1A",new Coordinate(5,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(5,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (5,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(5,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (6,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(6,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,1)
        fieldtypes.add(new ConveyorBelt(1, "right", new String[] {"left"}));
        arrayListY.add(new Tile("1A",new Coordinate(6,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,2)
        fieldtypes.add(new Pit());
        arrayListY.add(new Tile("1A",new Coordinate(6,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,3)
        fieldtypes.add(new Wall(new String[] {"right"}));
        fieldtypes.add(new Laser("left", 4));
        arrayListY.add(new Tile("1A",new Coordinate(6,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(6,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(6,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,6)
        fieldtypes.add(new Wall(new String[] {"right"}));
        arrayListY.add(new Tile("1A",new Coordinate(6,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,7)
        fieldtypes.add(new Pit());
        arrayListY.add(new Tile("1A",new Coordinate(6,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(6,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (6,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(6,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (7,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(7,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(7,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,2)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("1A",new Coordinate(7,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,3)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("1A",new Coordinate(7,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,4)
        fieldtypes.add(new Gear("right"));
        arrayListY.add(new Tile("1A",new Coordinate(7,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,5)
        fieldtypes.add(new Gear("left"));
        arrayListY.add(new Tile("1A",new Coordinate(7,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,6)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("1A",new Coordinate(7,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,7)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("1A",new Coordinate(7,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(7,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (7,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(7,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (8,0)
        fieldtypes.add(new ConveyorBelt(1, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("1A",new Coordinate(8,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,1)
        fieldtypes.add(new ConveyorBelt(1, "top", new String[] {"right"}));
        arrayListY.add(new Tile("1A",new Coordinate(8,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(8,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(8,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,4)
        fieldtypes.add(new CheckPoint(1));
        arrayListY.add(new Tile("1A",new Coordinate(8,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(8,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(8,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(8,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,8)
        fieldtypes.add(new ConveyorBelt(1, "right", new String[] {"bottom"}));
        arrayListY.add(new Tile("1A",new Coordinate(8,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (8,9)
        fieldtypes.add(new ConveyorBelt(1, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("1A",new Coordinate(8,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (9,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(9,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,1)
        fieldtypes.add(new ConveyorBelt(1, "left", new String[] {"right"}));
        arrayListY.add(new Tile("1A",new Coordinate(9,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(9,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(9,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(9,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(9,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(9,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(9,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,8)
        fieldtypes.add(new ConveyorBelt(1, "right", new String[] {"left"}));
        arrayListY.add(new Tile("1A",new Coordinate(9,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (9,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("1A",new Coordinate(9,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        return board;
    }


    /**
     * Builds board 4A (part of Extra Crispy)
     * @return board 4A as ArrayList
     */
    public ArrayList<ArrayList<Tile>> build4A() {

        ArrayList<ArrayList<Tile>> board = new ArrayList<>();
        ArrayList<Tile> arrayListY = new ArrayList<>();
        ArrayList<FieldType> fieldtypes = new ArrayList<>();

        // (0,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A",new Coordinate(0,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A",new Coordinate(0,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A",new Coordinate(0,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A",new Coordinate(0,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,4)
        fieldtypes.add(new Wall(new String[] {"top"}));
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("4A",new Coordinate(0,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,5)
        fieldtypes.add(new Wall(new String[] {"bottom"}));
        arrayListY.add(new Tile("4A",new Coordinate(0,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A",new Coordinate(0,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A",new Coordinate(0,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A",new Coordinate(0,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (0,9)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("4A",new Coordinate(0,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        board.add(arrayListY);
        arrayListY = new ArrayList<>();

        // (1,0)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("4A",new Coordinate(1,0), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,1)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("4A",new Coordinate(1,1), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,2)
        fieldtypes.add(new ConveyorBelt(2, "bottom", new String[] {"top"}));
        arrayListY.add(new Tile("4A",new Coordinate(1,2), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,3)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"right"}));
        arrayListY.add(new Tile("4A",new Coordinate(1,3), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A",new Coordinate(1,4), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("4A",new Coordinate(1,5), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,6)
        fieldtypes.add(new ConveyorBelt(2, "right", new String[] {"bottom"}));
        arrayListY.add(new Tile("4A",new Coordinate(1,6), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,7)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("4A",new Coordinate(1,7), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,8)
        fieldtypes.add(new ConveyorBelt(2, "top", new String[] {"bottom"}));
        arrayListY.add(new Tile("4A",new Coordinate(1,8), fieldtypes));
        fieldtypes = new ArrayList<>();

        // (1,9)
        fieldtypes.add(new Wall(new String[] {"left"}));
        fieldtypes.add(new Laser("right", 3));
        arrayListY.add(new Tile("4A",new Coordinate(1,9), fieldtypes));
        fieldtypes = new ArrayList<>();

        return board;
    }

}
