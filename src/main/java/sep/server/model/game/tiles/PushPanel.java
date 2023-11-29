package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class PushPanel implements FieldType {

    private static int[] activateAtRegister;

    private static String orientation;

    public PushPanel(String pushOrientation,int[] activationRegisters) {
        orientation = pushOrientation;

        activateAtRegister = activationRegisters;
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
