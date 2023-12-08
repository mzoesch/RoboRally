package sep.server.json.common;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class ConnectionUpdateModel extends AModel {

    private final int playerID;

    private final String action;

    private final boolean connected;

    public ConnectionUpdateModel(ClientInstance ci, int playerID, String action, boolean connected) {
        super(ci);

        this.playerID = playerID;
        this.action = action;
        this.connected = connected;
    }


    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("isConnected", this.connected);
        body.put("action", this.action);

        JSONObject j = new JSONObject();
        j.put("messageType", "ConnectionUpdate");
        j.put("messageBody", body);

        return j;
    }
}
