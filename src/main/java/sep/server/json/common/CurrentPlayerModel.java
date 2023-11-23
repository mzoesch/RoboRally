package sep.server.json.common;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class CurrentPlayerModel extends AModel {

    private final int playerID;


    public CurrentPlayerModel(ClientInstance ci, int playerID) {
        super(ci);
        this.playerID = playerID;

        return;
    }

    @Override
    public JSONObject toJSON() {

        JSONObject j = new JSONObject();
        j.put("messageType", "CurrentPlayer");
        j.put("messageBody", new JSONObject().put("clientID", this.playerID));

        return j;
    }
}
