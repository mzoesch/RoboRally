package sep.server.model.game.tiles;

import org.json.JSONObject;

public class EnergySpace implements FieldType {

    private int availableEnergy;

    public EnergySpace(int availableEnergy) {

        this.availableEnergy = availableEnergy;
    }

    public JSONObject toJSON(String isOnBoard){
        JSONObject fieldInfo = new JSONObject();
        fieldInfo.put("type","EnergySpace");
        fieldInfo.put("isOnBoard", isOnBoard);
        fieldInfo.put("count", availableEnergy);
        return fieldInfo;
    }
}
