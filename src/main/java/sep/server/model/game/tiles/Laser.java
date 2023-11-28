package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class Laser implements FieldType {

    private static int laserCount;

    private String orientation;

    public Laser(String laserOrientation, int thisLaserCount) {

        orientation = laserOrientation;
        laserCount = thisLaserCount;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("type","Laser");
        fieldInfo.put("type", isOnBoard);
        fieldInfo.put("count", laserCount);
        fieldInfo.put("orientations", new JSONArray().put(orientation));
        return fieldInfo;
    }
}
