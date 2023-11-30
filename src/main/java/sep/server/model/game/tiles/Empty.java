package sep.server.model.game.tiles;

import org.json.JSONObject;

public class Empty implements FieldType{

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","Empty");
        return fieldInfo;
    }
}
