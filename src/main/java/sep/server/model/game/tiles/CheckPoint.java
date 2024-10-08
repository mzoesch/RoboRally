package sep.server.model.game.tiles;

import org.json.JSONObject;

public class CheckPoint implements FieldType {
    private final int checkpointNumber;

    public CheckPoint(int thisCheckpointNumber) {
        checkpointNumber = thisCheckpointNumber;
    }

    public int getCheckpointNumber() {
        return checkpointNumber;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("count", checkpointNumber);
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","CheckPoint");
        return fieldInfo;
    }
}
