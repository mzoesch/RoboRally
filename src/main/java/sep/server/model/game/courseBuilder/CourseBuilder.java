package sep.server.model.game.courseBuilder;

import sep.server.model.game.Tile;
import sep.server.model.game.tiles.*;

import java.util.ArrayList;

/**
 * Klasse, die das jeweilige Spielfeld erstellt
 */
public class CourseBuilder {

    public CourseBuilder() {
    }

    /**
     * Baut das entsprechende Spielfeld aus den einzelnen Boards
     * @param courseName Name des Spielfelds
     * @return komplettes Spielfeld als ArrayList<ArrayList<Tile>>
     */
    public  ArrayList<ArrayList<Tile>> buildCourse(String courseName){
        switch(courseName){
            case("DizzyHighway") -> {
                ArrayList<ArrayList<Tile>> boardStartA = buildBoard("StartA");
                ArrayList<ArrayList<Tile>> board5B = buildBoard("5B");
                ArrayList<ArrayList<Tile>> entireCourse = appendRight(boardStartA, board5B);
                settingCoordinates(entireCourse);
                return entireCourse;
            }
            case("Test") -> {
                return buildBoard("Test");
            }
        }
        return null;
    }

    /**
     * Bekommt ein Spielfeld übergeben und setzt die Nachbarkoordinaten der einzelnen Tiles
     * @param course Spielfeld
     */
    public  void settingCoordinates(ArrayList<ArrayList<Tile>> course){
        for(int i = 0; i <  course.size(); i++){

            ArrayList<Tile> courseYRow = course.get(i);
            for(int a = 0; a < courseYRow.size(); a++){
                Tile tile = courseYRow.get(i);
                int xCoordinate = tile.getCoordinate().getXCoordinate();
                int yCoordinate = tile.getCoordinate().getYCoordinate();

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

    /**
     * Fügt ein Board von rechts an ein anderes Board an (funktioniert aktuell nur bei gleicher Länge)
     * @param leftBoard linkes Board
     * @param rightBoard rechtes Board (wird angefügt)
     * @return ArrayList, die das zusammengefügtes Board repräsentiert
     */
    public  ArrayList<ArrayList<Tile>> appendRight(ArrayList<ArrayList<Tile>> leftBoard, ArrayList<ArrayList<Tile>> rightBoard){

        leftBoard.addAll(rightBoard);
        return leftBoard;
    }

    /**
     * Fügt ein Board unterhalb des anderen Boards hinzu (funktioniert aktuell nur bei gleicher Breite)
     * @param topBoard oberes Board
     * @param bottomBoard unteres Board
     * @return ArrayList, die das zusammengefügte Board repräsentiert
     */
    public  ArrayList<ArrayList<Tile>> appendBottom(ArrayList<ArrayList<Tile>> topBoard, ArrayList<ArrayList<Tile>> bottomBoard){

        for(int i = 0; i< topBoard.size(); i++){
            topBoard.get(i).addAll(bottomBoard.get(i));
        }
        return topBoard;
    }

    /**
     * Baut ein Board abhängig vom Boardname
     * @param boardName Name des zu erstellenden Boards
     * @return erstelltes Board
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

        }
        return null;
    }

    /**
     * Baut das TestBoard aus Protokollv0.1
     * @return ArrayList des TestBoards
     */
    public ArrayList<ArrayList<Tile>> buildTestA(){
        ArrayList<ArrayList<Tile>> board = new ArrayList<>();
        ArrayList<Tile> arrayListY = new ArrayList<>();
        ArrayList<FieldType> fieldtypes = new ArrayList<>();

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

    /**
     * Baut das Board StartA (Teil von DizzyHighway)
     * @return ArrayList des Boards StartA
     */
    public ArrayList<ArrayList<Tile>> buildStartA()
    {
        ArrayList<ArrayList<Tile>> board = new ArrayList<>();
        ArrayList<Tile> arrayListY = new ArrayList<>();
        ArrayList<FieldType> fieldtypes = new ArrayList<>();

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

    /**
     * Baut das Board 5B (Teil von DizzyHighway)
     * @return ArrayList des Boards 5B
     */
    public ArrayList<ArrayList<Tile>> build5B(){

        ArrayList<ArrayList<Tile>> board = new ArrayList<>();
        ArrayList<Tile> arrayListY = new ArrayList<>();
        ArrayList<FieldType> fieldtypes = new ArrayList<>();

        // (0,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,1), fieldtypes));
        fieldtypes.clear();

        // (0,1)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,2), fieldtypes));
        fieldtypes.clear();

        // (0,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,3), fieldtypes));
        fieldtypes.clear();

        // (0,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,0), fieldtypes));
        fieldtypes.clear();

        // (0,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,0), fieldtypes));
        fieldtypes.clear();

        // (0,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,0), fieldtypes));
        fieldtypes.clear();

        // (0,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(0,0), fieldtypes));
        fieldtypes.clear();

        // (0,7)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[] {"WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(0,7), fieldtypes));
        fieldtypes.clear();

        // (0,8)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[] {"WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(0,8), fieldtypes));
        fieldtypes.clear();

        // (0,9)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(0,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (1,0)
        fieldtypes.add(new ConveyorBelt(2, "SOUTH", new String[] {"NORTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,0), fieldtypes));
        fieldtypes.clear();

        // (1,1)
        fieldtypes.add(new ConveyorBelt(2, "SOUTH", new String[] {"NORTH", "EAST"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,1), fieldtypes));
        fieldtypes.clear();

        // (1,2)
        fieldtypes.add(new ConveyorBelt(2, "SOUTH", new String[] {"NORTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,2), fieldtypes));
        fieldtypes.clear();

        // (1,3)
        fieldtypes.add(new ConveyorBelt(2, "SOUTH", new String[] {"NORTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,3), fieldtypes));
        fieldtypes.clear();

        // (1,4)
        fieldtypes.add(new ConveyorBelt(2, "SOUTH", new String[] {"NORTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,4), fieldtypes));
        fieldtypes.clear();

        // (1,5)
        fieldtypes.add(new ConveyorBelt(2, "SOUTH", new String[] {"NORTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,5), fieldtypes));
        fieldtypes.clear();

        // (1,6)
        fieldtypes.add(new ConveyorBelt(2, "SOUTH", new String[] {"NORTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,6), fieldtypes));
        fieldtypes.clear();

        // (1,7)
        fieldtypes.add(new ConveyorBelt(2, "SOUTH", new String[] {"NORTH", "WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,7), fieldtypes));
        fieldtypes.clear();

        // (1,8)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[] {"NORTH", "WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(1,8), fieldtypes));
        fieldtypes.clear();

        // (1,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(1,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (2,0)
        fieldtypes.add(new ConveyorBelt(2, "SOUTH", new String[] {"NORTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(2,0), fieldtypes));
        fieldtypes.clear();

        // (2,1)
        fieldtypes.add(new ConveyorBelt(2, "WEST", new String[] {"NORTH", "EAST"}));
        arrayListY.add(new Tile("5B",new Coordinate(2,0), fieldtypes));
        fieldtypes.clear();

        // (2,2)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(2,2), fieldtypes));
        fieldtypes.clear();

        // (2,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,3), fieldtypes));
        fieldtypes.clear();

        // (2,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,4), fieldtypes));
        fieldtypes.clear();

        // (2,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,5), fieldtypes));
        fieldtypes.clear();

        // (2,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,6), fieldtypes));
        fieldtypes.clear();

        // (2,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,7), fieldtypes));
        fieldtypes.clear();

        // (2,8)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[]{"WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(2,8), fieldtypes));
        fieldtypes.clear();

        // (2,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(2,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (3,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,0), fieldtypes));
        fieldtypes.clear();

        // (3,1)
        fieldtypes.add(new ConveyorBelt(2, "WEST", new String[] {"NORTH", "EAST"}));
        arrayListY.add(new Tile("5B",new Coordinate(3,1), fieldtypes));
        fieldtypes.clear();

        // (3,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,2), fieldtypes));
        fieldtypes.clear();

        // (3,3)
        fieldtypes.add(new Wall(new String[]{"NORTH"}));
        fieldtypes.add(new Laser("North", 1));
        arrayListY.add(new Tile("5B",new Coordinate(3,3), fieldtypes));
        fieldtypes.clear();

        // (3,4)
        fieldtypes.add(new Wall(new String[]{"SOUTH"}));
        fieldtypes.add(new Laser("North", 1));
        arrayListY.add(new Tile("5B",new Coordinate(3,4), fieldtypes));
        fieldtypes.clear();

        // (3,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,5), fieldtypes));
        fieldtypes.clear();

        // (3,6)
        fieldtypes.add(new Wall(new String[]{"WEST"}));
        fieldtypes.add(new Laser("WEST", 1));
        arrayListY.add(new Tile("5B",new Coordinate(3,6), fieldtypes));
        fieldtypes.clear();

        // (3,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,7), fieldtypes));
        fieldtypes.clear();

        // (3,8)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[]{"WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(3,8), fieldtypes));
        fieldtypes.clear();

        // (3,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(3,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (4,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,0), fieldtypes));
        fieldtypes.clear();

        // (4,1)
        fieldtypes.add(new ConveyorBelt(2, "WEST", new String[] {"EAST"}));
        arrayListY.add(new Tile("5B",new Coordinate(4,1), fieldtypes));
        fieldtypes.clear();

        // (4,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,2), fieldtypes));
        fieldtypes.clear();

        // (4,3)
        fieldtypes.add(new RestartPoint());
        arrayListY.add(new Tile("5B",new Coordinate(4,3), fieldtypes));
        fieldtypes.clear();

        // (4,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,4), fieldtypes));
        fieldtypes.clear();

        // (4,5)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(4,5), fieldtypes));
        fieldtypes.clear();

        // (4,6)
        fieldtypes.add(new Wall(new String[]{"EAST"}));
        fieldtypes.add(new Laser("WEST", 1));
        arrayListY.add(new Tile("5B",new Coordinate(4,6), fieldtypes));
        fieldtypes.clear();

        // (4,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,7), fieldtypes));
        fieldtypes.clear();

        // (4,8)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[]{"WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(4,8), fieldtypes));
        fieldtypes.clear();

        // (4,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(4,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (5,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,0), fieldtypes));
        fieldtypes.clear();

        // (5,1)
        fieldtypes.add(new ConveyorBelt(2, "WEST", new String[] {"EAST"}));
        arrayListY.add(new Tile("5B",new Coordinate(5,1), fieldtypes));
        fieldtypes.clear();

        // (5,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,2), fieldtypes));
        fieldtypes.clear();

        // (5,3)
        fieldtypes.add(new Wall(new String[]{"WEST"}));
        fieldtypes.add(new Laser("EAST", 1));
        arrayListY.add(new Tile("5B",new Coordinate(5,3), fieldtypes));
        fieldtypes.clear();

        // (5,4)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(5,4), fieldtypes));
        fieldtypes.clear();

        // (5,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,5), fieldtypes));
        fieldtypes.clear();

        // (5,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,6), fieldtypes));
        fieldtypes.clear();

        // (5,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,7), fieldtypes));
        fieldtypes.clear();

        // (5,8)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[]{"WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(5,8), fieldtypes));
        fieldtypes.clear();

        // (5,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(5,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (6,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,0), fieldtypes));
        fieldtypes.clear();

        // (6,1)
        fieldtypes.add(new ConveyorBelt(2, "WEST", new String[] {"EAST"}));
        arrayListY.add(new Tile("5B",new Coordinate(6,1), fieldtypes));
        fieldtypes.clear();

        // (6,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,2), fieldtypes));
        fieldtypes.clear();

        // (6,3)
        fieldtypes.add(new Wall(new String[]{"EAST"}));
        fieldtypes.add(new Laser("EAST", 1));
        arrayListY.add(new Tile("5B",new Coordinate(6,3), fieldtypes));
        fieldtypes.clear();

        // (6,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,4), fieldtypes));
        fieldtypes.clear();

        // (6,5)
        fieldtypes.add(new Wall(new String[]{"NORTH"}));
        fieldtypes.add(new Laser("SOUTH", 1));
        arrayListY.add(new Tile("5B",new Coordinate(6,5), fieldtypes));
        fieldtypes.clear();

        // (6,6)
        fieldtypes.add(new Wall(new String[]{"SOUTH"}));
        fieldtypes.add(new Laser("SOUTH", 1));
        arrayListY.add(new Tile("5B",new Coordinate(6,6), fieldtypes));
        fieldtypes.clear();

        // (6,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,7), fieldtypes));
        fieldtypes.clear();

        // (6,8)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[]{"WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(6,8), fieldtypes));
        fieldtypes.clear();

        // (6,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(6,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (7,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,0), fieldtypes));
        fieldtypes.clear();

        // (7,1)
        fieldtypes.add(new ConveyorBelt(2, "WEST", new String[] {"EAST"}));
        arrayListY.add(new Tile("5B",new Coordinate(7,1), fieldtypes));
        fieldtypes.clear();

        // (7,2)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,2), fieldtypes));
        fieldtypes.clear();

        // (7,3)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,3), fieldtypes));
        fieldtypes.clear();

        // (7,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,4), fieldtypes));
        fieldtypes.clear();

        // (7,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,5), fieldtypes));
        fieldtypes.clear();

        // (7,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(7,6), fieldtypes));
        fieldtypes.clear();

        // (7,7)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(7,7), fieldtypes));
        fieldtypes.clear();

        // (7,8)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[]{"SOUTH","WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(7,8), fieldtypes));
        fieldtypes.clear();

        // (7,9)
        fieldtypes.add(new ConveyorBelt(2,"NORTH", new String[]{"SOUTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(7,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        arrayListY.clear();

        // (8,0)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(8,0), fieldtypes));
        fieldtypes.clear();

        // (8,1)
        fieldtypes.add(new ConveyorBelt(2, "WEST", new String[] {"EAST", "SOUTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,1), fieldtypes));
        fieldtypes.clear();

        // (8,2)
        fieldtypes.add(new ConveyorBelt(2, "NORTH", new String[] {"EAST", "SOUTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,2), fieldtypes));
        fieldtypes.clear();

        // (8,3)
        fieldtypes.add(new ConveyorBelt(2, "NORTH", new String[] {"SOUTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,3), fieldtypes));
        fieldtypes.clear();

        // (8,4)
        fieldtypes.add(new ConveyorBelt(2, "NORTH", new String[] {"SOUTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,4), fieldtypes));
        fieldtypes.clear();

        // (8,5)
        fieldtypes.add(new ConveyorBelt(2, "NORTH", new String[] {"SOUTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,5), fieldtypes));
        fieldtypes.clear();

        // (8,6)
        fieldtypes.add(new ConveyorBelt(2, "NORTH", new String[] {"SOUTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,6), fieldtypes));
        fieldtypes.clear();

        // (8,7)
        fieldtypes.add(new ConveyorBelt(2, "NORTH", new String[] {"SOUTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,7), fieldtypes));
        fieldtypes.clear();

        // (8,8)
        fieldtypes.add(new ConveyorBelt(2, "EAST", new String[]{"SOUTH","WEST"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,8), fieldtypes));
        fieldtypes.clear();

        // (8,9)
        fieldtypes.add(new ConveyorBelt(2,"NORTH", new String[]{"SOUTH"}));
        arrayListY.add(new Tile("5B",new Coordinate(8,9), fieldtypes));
        fieldtypes.clear();


        board.add(arrayListY);
        arrayListY.clear();

        // (9,0)
        fieldtypes.add(new EnergySpace(1));
        arrayListY.add(new Tile("5B",new Coordinate(9,0), fieldtypes));
        fieldtypes.clear();

        // (9,1)
        fieldtypes.add(new ConveyorBelt(2, "WEST", new String[] {"EAST"}));
        arrayListY.add(new Tile("5B",new Coordinate(9,1), fieldtypes));
        fieldtypes.clear();

        // (9,2)
        fieldtypes.add(new ConveyorBelt(2, "WEST", new String[] {"EAST"}));
        arrayListY.add(new Tile("5B",new Coordinate(9,2), fieldtypes));
        fieldtypes.clear();

        // (9,3)
        fieldtypes.add(new CheckPoint(1));
        arrayListY.add(new Tile("5B",new Coordinate(9,3), fieldtypes));
        fieldtypes.clear();

        // (9,4)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,4), fieldtypes));
        fieldtypes.clear();

        // (9,5)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,5), fieldtypes));
        fieldtypes.clear();

        // (9,6)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,6), fieldtypes));
        fieldtypes.clear();

        // (9,7)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,7), fieldtypes));
        fieldtypes.clear();

        // (9,8)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,8), fieldtypes));
        fieldtypes.clear();

        // (9,9)
        fieldtypes.add(new Empty());
        arrayListY.add(new Tile("5B",new Coordinate(9,9), fieldtypes));
        fieldtypes.clear();

        board.add(arrayListY);
        return board;
    }
}
