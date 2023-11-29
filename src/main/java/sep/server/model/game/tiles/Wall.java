package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class Wall implements FieldType {
    private String[] orientations;

    public Wall(String[] wallOrientations){
            this.orientations = wallOrientations;
    }
    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("type","Wall");
        fieldInfo.put("isOnBoard", isOnBoard);

        JSONArray wallOrientations = new JSONArray();
        for(String orientation : orientations){
            wallOrientations.put(orientation);
        }

        return fieldInfo;
    }
}
