package sep.server.json.lobby;

import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;

public class PlayerReadyModel extends AModel
{
    private final int playerID;
    private final boolean bIsReady;

    public PlayerReadyModel(ClientInstance ci, int playerID, boolean bIsReady)
    {
        super(ci);
        this.playerID = playerID;
        this.bIsReady = bIsReady;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject b = new JSONObject();
        b.put("clientID", this.playerID);
        b.put("ready", this.bIsReady);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerStatus");
        j.put("messageBody", b);

        return j;
    }

}
