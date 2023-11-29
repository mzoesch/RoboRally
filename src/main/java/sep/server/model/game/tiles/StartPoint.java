package sep.server.model.game.tiles;

import org.json.JSONObject;

public class StartPoint implements FieldType {

    public StartPoint(){
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("type","StartPoint");
        fieldInfo.put("isOnBoard", isOnBoard);
        return fieldInfo;
    }
}

