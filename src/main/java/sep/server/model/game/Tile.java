package sep.server.model.game;

import sep.server.model.game.tiles.*;

import java.util.ArrayList;

public class Tile {
    private static Coordinate coordinate;
    private Robot occupiedBy = null;
    private static ArrayList<FieldType> fieldTypes;

    public Tile(Coordinate fieldCoordinate, ArrayList<FieldType> arrayFieldTypes){
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
}
