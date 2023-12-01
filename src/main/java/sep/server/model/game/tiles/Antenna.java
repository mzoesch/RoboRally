package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class Antenna implements FieldType {
    private String direction;
    private String isOnBoard;

    public Antenna(String antennaDirection) {
        direction = antennaDirection;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("orientations", new JSONArray().put(direction));
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","Antenna");
        return fieldInfo;
    }
}
