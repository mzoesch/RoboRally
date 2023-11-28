package sep.server.model.game;

import sep.server.model.game.tiles.*;

import java.util.ArrayList;

public class Tile {
    private static Coordinate coordinate;
    private Robot occupiedBy = null;
    private static ArrayList<FieldType> fieldTypes;

    //braucht es das onBoard-Attribut? (aus Protokoll v0.1)
    private String isOnBoard;

    public Tile(String onBoard, Coordinate fieldCoordinate, ArrayList<FieldType> arrayFieldTypes){
        isOnBoard = onBoard;
        coordinate = fieldCoordinate;
        fieldTypes = arrayFieldTypes;
    }
    public Robot getRobot(){
        return occupiedBy;
    }

    //TODO setRobot verhindert aktuell nicht, dass besetzes Feld neu belegt wird!
    public void setRobot(Robot newRobot) {
        occupiedBy = newRobot;
    }

    public Coordinate getCoordinate(){
        return coordinate;
    }

    public ArrayList<FieldType> getFieldTypes() {
        return fieldTypes;
    }

    /**
     * Checks if the tile contains  an antenna.
     * Returns true if any type is an instance of Antenna, otherwise returns false.
     */
    public boolean hasAntenna() {
        for (FieldType fieldType : fieldTypes) {
            if (fieldType instanceof Antenna) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the tile contains  an wall.
     * Returns true if any type is an instance of wall, otherwise returns false.
     */
    public boolean hasWall() {
        for (FieldType fieldType : fieldTypes) {
            if (fieldType instanceof Wall) {
                return true;
            }
        }
        return false;
    }

    public boolean hasUnmovableRobot() {

        return true;
    }


}
