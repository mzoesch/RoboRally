package sep.server.model.game;

import sep.server.model.game.tiles.*;

import org.json.JSONArray;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Tile {
    private static final Logger l = LogManager.getLogger(Tile.class);

    private Coordinate coordinate;
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

    public void setRobot(Robot newRobot) {
        l.trace("{} tile {}. ", newRobot == null ? "Removing current player from" : String.format("Setting player %d to", newRobot.getPossessor().getController().getPlayerID()), this.coordinate.toString());
        if(!isOccupied()) {
            occupiedBy = newRobot;
        }
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(int x, int y) {
        this.coordinate = new Coordinate(x,y);
    }

    public ArrayList<FieldType> getFieldTypes() {
        return fieldTypes;
    }

    public boolean isOccupied(){
        return occupiedBy != null;
    }

    public void setOccupiedBy(Robot occupiedBy) {
        this.occupiedBy = occupiedBy;
    }

    public boolean hasUnmovableRobot(String pushingDirection) {
        if(this.isOccupied()) {
            Robot currentOccupier = this.occupiedBy;
            currentOccupier.moveRobotOneTile(true, pushingDirection);

            currentOccupier.getAuthGameMode().getSession().broadcastPositionUpdate(
                    currentOccupier.determineRobotOwner().getController().getPlayerID(), currentOccupier.determineRobotOwner().getPosition());

            if(currentOccupier.getCurrentTile() == this) {
                return true;
            }

            return false;
        }
        return false;
    }

    public boolean hasAntennaModifier() {
        for (FieldType t : this.fieldTypes) {
            if (t instanceof Antenna) {
                return true;
            }
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

    public JSONArray toJSON() {
        JSONArray j = new JSONArray();
        for (FieldType f : this.fieldTypes) {
            j.put(f.toJSON(this.boardName));
        }
        return j;
    }

    public boolean isNorthOf(Tile t)
    {
        return this.coordinate.getY() < t.coordinate.getY();
    }

    public boolean isSouthOf(Tile t)
    {
        return this.coordinate.getY() > t.coordinate.getY();
    }

    public boolean isWestOf(Tile t)
    {
        return this.coordinate.getX() < t.coordinate.getX();
    }

    public boolean isEastOf(Tile t)
    {
        return this.coordinate.getX() > t.coordinate.getX();
    }

    public boolean isWallEast() {
        for (FieldType f : this.fieldTypes) {
            if (f instanceof Wall w && Arrays.stream(w.getOrientations()).anyMatch(s -> s.equalsIgnoreCase("east") || s.equalsIgnoreCase("right"))) {
                return true;
            }
        }
        return false;
    }

    public boolean isWallWest() {
        for (FieldType f : this.fieldTypes) {
            if (f instanceof Wall w && Arrays.stream(w.getOrientations()).anyMatch(s -> s.equalsIgnoreCase("west") || s.equalsIgnoreCase("left"))) {
                return true;
            }
        }
        return false;
    }

    public boolean isWallNorth() {
        for (FieldType f : this.fieldTypes) {
            if (f instanceof Wall w && Arrays.stream(w.getOrientations()).anyMatch(s -> s.equalsIgnoreCase("north") || s.equalsIgnoreCase("top"))) {
                return true;
            }
        }
        return false;
    }

    public boolean isWallSouth() {
        for (FieldType f : this.fieldTypes) {
            if (f instanceof Wall w && Arrays.stream(w.getOrientations()).anyMatch(s -> s.equalsIgnoreCase("south") || s.equalsIgnoreCase("bottom"))) {
                return true;
            }
        }
        return false;
    }

    public boolean isStartingPoint(){
        for (FieldType fieldType : fieldTypes) {
            if (fieldType instanceof StartPoint) {
                return true;
            }
        }
        return false;
    }
}
