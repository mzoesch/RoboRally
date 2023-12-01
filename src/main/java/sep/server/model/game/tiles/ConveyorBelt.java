package sep.server.model.game.tiles;

import org.json.JSONArray;
import org.json.JSONObject;

public class ConveyorBelt implements FieldType {

    private String[] incomingFlowDirection;
    private String outcomingFlowDirection;
    private int speed;

    public ConveyorBelt(int beltSpeed, String outDirection, String [] inDirection) {

        outcomingFlowDirection = outDirection;
        incomingFlowDirection = inDirection;
        speed = beltSpeed;
    }

    public String[] getIncomingFlowDirection() {
        return incomingFlowDirection;
    }

    public String getOutcomingFlowDirection() {
        return outcomingFlowDirection;
    }

    public int getSpeed() {
        return speed;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        JSONArray orientations = new JSONArray();
        orientations.put(outcomingFlowDirection);
        for(String incomingDirection : incomingFlowDirection){
            orientations.put(incomingDirection);
        }
        fieldInfo.put("orientations", orientations);
        fieldInfo.put("speed", speed);
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("type","ConveyorBelt");
        return fieldInfo;
    }
}
