package sep.server.model.game.tiles;

import org.json.JSONObject;

public interface FieldType {
    public JSONObject toJSON(String isOnBoard);
}
