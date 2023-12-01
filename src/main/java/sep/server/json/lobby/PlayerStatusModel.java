package sep.server.json.lobby;

import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;

public class PlayerStatusModel extends AModel
{
    private final int playerID;
    private final boolean bIsReady;

    public PlayerStatusModel(ClientInstance ci, int playerID, boolean bIsReady)
    {
        super(ci);
        this.playerID = playerID;
        this.bIsReady = bIsReady;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("ready", this.bIsReady);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerStatus");
        j.put("messageBody", body);

        return j;
    }

}
