package sep.server.json.common;

import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;

public class ConnectionUpdateModel extends AModel
{
    private final int ctrlID;
    private final String action;
    private final boolean bConnected;

    public ConnectionUpdateModel(final ClientInstance ci, final int ctrlID, String action, final boolean bConnected)
    {
        super(ci);

        this.ctrlID = ctrlID;
        this.action = action;
        this.bConnected = bConnected;

        return;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.ctrlID);
        body.put("isConnected", this.bConnected);
        body.put("action", this.action);

        JSONObject j = new JSONObject();
        j.put("messageType", "ConnectionUpdate");
        j.put("messageBody", body);

        return j;
    }

}
