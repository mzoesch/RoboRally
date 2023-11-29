package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class RestartPoint implements FieldType{

    public RestartPoint(){
    }
    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","RestartPoint");
        return fieldInfo;
    }
}
