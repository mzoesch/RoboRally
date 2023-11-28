package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class CheckPoint implements FieldType {
    private int checkpointNumber;

    public CheckPoint(int thisCheckpointNumber) {
        checkpointNumber = thisCheckpointNumber;
    }
    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("type","CheckPoint");
        fieldInfo.put("type", isOnBoard);
        fieldInfo.put("count", checkpointNumber);
        return fieldInfo;
    }
}
