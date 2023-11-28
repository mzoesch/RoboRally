package sep.server.model.game.tiles;

import org.json.JSONObject;

public class Empty implements FieldType{

    public JSONObject toJSON(String isOnBoard){
        return null;
    }
}
