package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class Laser implements FieldType {

    private static int laserCount;

    private final String orientation;

    public Laser(String laserOrientation, int thisLaserCount) {

        orientation = laserOrientation;
        laserCount = thisLaserCount;
    }

    public static int getLaserCount() {
        return laserCount;
    }

    public String getOrientation() {
        return orientation;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("orientations", new JSONArray().put(orientation));
        fieldInfo.put("count", laserCount);
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","Laser");
        return fieldInfo;
    }
}
