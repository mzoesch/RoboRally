package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class Pit implements FieldType {

    public Pit(){

    }
    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","Pit");
        return fieldInfo;
    }
}
