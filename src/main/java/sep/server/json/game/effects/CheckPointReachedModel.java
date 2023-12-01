package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class CheckPointReachedModel extends AModel {

    private final int playerID;
    private final int checkPointNumbers;

    public CheckPointReachedModel(ClientInstance ci, int playerID, int checkPointNumbers) {
        super(ci);
        this.playerID = playerID;
        this.checkPointNumbers = checkPointNumbers;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("number", this.checkPointNumbers);

        JSONObject j = new JSONObject();
        j.put("messageType", "CheckPointReached");
        j.put("messageBody", body);

        return j;
    }
}
