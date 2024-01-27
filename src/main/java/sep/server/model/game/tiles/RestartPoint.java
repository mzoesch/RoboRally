package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestartPoint implements FieldType{
    private String orientation;
    public RestartPoint(String orientation){
        this.orientation = orientation;
    }
    public String getRestartOrientation(){
        return orientation;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","RestartPoint");
        JSONArray orientations = new JSONArray();
        orientations.put(orientation);
        fieldInfo.put("orientations", orientations);
        return fieldInfo;
    }
}
