package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class ConveyorBelt implements FieldType {

    private static String[] incomingFlowDirection;
    private static String outcomingFlowDirection;
    private static int speed;

    public ConveyorBelt(int beltSpeed, String outDirection, String [] inDirection) {

        outcomingFlowDirection = outDirection;
        incomingFlowDirection = inDirection;
        speed = beltSpeed;
    }

    public static String[] getIncomingFlowDirection() {
        return incomingFlowDirection;
    }

    public static String getOutcomingFlowDirection() {
        return outcomingFlowDirection;
    }

    public static int getSpeed() {
        return speed;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("type","ConveyorBelt");
        fieldInfo.put("type", isOnBoard);
        fieldInfo.put("speed", speed);

        JSONArray orientations = new JSONArray();
        orientations.put(outcomingFlowDirection);
        for(String incomingDirection : incomingFlowDirection){
            orientations.put(incomingDirection);
        }
        fieldInfo.put("orientations", orientations);
        return fieldInfo;
    }
}
