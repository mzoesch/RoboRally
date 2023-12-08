package sep.server.model.game;

import sep.server.model.game.tiles.*;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Tile
{
    private final Coordinate coordinate;
    private Robot occupiedBy = null;
    private ArrayList<FieldType> fieldTypes;

    /** Name of the board (not course) the tile is on. */
    private final String boardName;

    public Tile(String onBoard, Coordinate fieldCoordinate, ArrayList<FieldType> arrayFieldTypes) {
        boardName = onBoard;
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

    public boolean isOccupied(){
        return occupiedBy != null;
    }

    public boolean isStartingPoint(){
        for (FieldType fieldType : fieldTypes) {
            if (fieldType instanceof StartPoint) {
                return true;
            }
        }
        return false;
    }



    public boolean hasUnmovableRobot() {

        //Noch zu implementieren
        return false;
    }

    // region Getters and Setters

    public boolean hasAntennaModifier()
    {
        for (FieldType t : this.fieldTypes)
        {
            if (t instanceof Antenna)
            {
                return true;
            }

            continue;
        }

        return false;
    }

    public boolean hasWallModifier() {
        for (FieldType fieldType : fieldTypes) {
            if (fieldType instanceof Wall) {
                return true;
            }
        }
        return false;
    }

    public String getBoardName()
    {
        return this.boardName;
    }

    public JSONArray toJSON()
    {
        JSONArray j = new JSONArray();
        for (FieldType f : this.fieldTypes)
        {
            j.put(f.toJSON(this.boardName));
        }

        return j;
    }

    // endregion Getters and Setters

}
