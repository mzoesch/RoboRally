package sep.server.model.game.tiles;

import org.json.JSONObject;

public class Gear implements FieldType {

    private String rotationalDirection;

    public Gear(String gearRotationalDirection) {
        rotationalDirection = gearRotationalDirection;
    }

    public String getRotationalDirection() {
        return rotationalDirection;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("orientations", rotationalDirection);
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","Gear");
        return fieldInfo;
    }
}
