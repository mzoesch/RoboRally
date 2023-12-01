package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class PushPanel implements FieldType {

    private int[] activateAtRegister;

    private String orientation;

    public PushPanel(String pushOrientation,int[] activationRegisters) {
        orientation = pushOrientation;

        activateAtRegister = activationRegisters;
    }

    public int[] getActivateAtRegister() {
        return activateAtRegister;
    }

    public String getOrientation() {
        return orientation;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("orientations", new JSONArray().put(orientation));
        JSONArray registers = new JSONArray();
        for(int i : activateAtRegister){
            registers.put(i);
        }
        fieldInfo.put("registers", registers);
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","PushPanel");
        return fieldInfo;
    }
}
