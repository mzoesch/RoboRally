package sep.server.model.game;

import sep.server.json.IJSONSerializable;
import org.json.JSONObject;
import org.json.JSONArray;

import sep.server.model.game.tiles.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class Tile {
    private final Coordinate coordinate;
    private Robot occupiedBy = null;
    private ArrayList<FieldType> fieldTypes;

    //braucht es das onBoard-Attribut? (aus Protokoll v0.1)
    private String isOnBoard;

    public Tile(String onBoard, Coordinate fieldCoordinate, ArrayList<FieldType> arrayFieldTypes) {
        isOnBoard = onBoard;
        coordinate = fieldCoordinate;
        fieldTypes = arrayFieldTypes;
    }

    public Robot getRobot() {
        return occupiedBy;
    }

    //TODO setRobot verhindert aktuell nicht, dass besetzes Feld neu belegt wird!
    public void setRobot(Robot newRobot) {
        occupiedBy = newRobot;
    }

    public Coordinate getCoordinate() {
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

        //Noch zu implementieren
        return true;
    }

    public JSONArray toJSON() {
        JSONArray tileInfo = new JSONArray();

        //TODO entspricht noch nicht Protokoll v0.1!!!
        if (fieldTypes.get(0).toJSON(isOnBoard) == null) {
            tileInfo.put("null");
        }
        else{
            for (int i = 0; i < fieldTypes.size(); i++) {
                tileInfo.put(fieldTypes.get(i).toJSON(isOnBoard));
            }
        }
        return tileInfo;
    }

    public String getIsOnBoard() {
        return isOnBoard;
    }
}






