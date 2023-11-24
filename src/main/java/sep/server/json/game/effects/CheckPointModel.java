package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class CheckPointModel extends AModel {

    private final int playerID;
    private final int checkpointNumbers;

    public CheckPointModel(ClientInstance ci, int playerID, int checkpointNumbers) {
        super(ci);
        this.playerID = playerID;
        this.checkpointNumbers = checkpointNumbers;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("number", this.checkpointNumbers);

        JSONObject j = new JSONObject();
        j.put("messageType", "CheckPointReached");
        j.put("messageBody", body);

        return j;
    }
}
