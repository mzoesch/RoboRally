package sep.server.model.game.tiles;

import org.json.JSONObject;

public class Gear implements FieldType {

    private static String rotationalDirection;

    public Gear(String gearRotationalDirection) {
        rotationalDirection = gearRotationalDirection;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("type","Gear");
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("orientations", rotationalDirection);
        return fieldInfo;
    }
}
