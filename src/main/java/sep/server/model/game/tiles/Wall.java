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
        JSONArray wallOrientations = new JSONArray();
        for(int i = 0; i < orientations.length; i++ ){
            wallOrientations.put(orientations[i]);
        }
        fieldInfo.put("orientations", wallOrientations);
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","Wall");
        return fieldInfo;
    }
}
