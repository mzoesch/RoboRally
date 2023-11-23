package sep.server.json.game.programmingphase;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class ShuffleCodingModel extends AModel {

    private final int playerID;

    public ShuffleCodingModel(ClientInstance ci, int playerID) {
        super(ci);
        this.playerID = playerID;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "ShuffleCoding");
        j.put("messageBody", new JSONObject().put("clientID", this.playerID));

        return j;
    }
}
