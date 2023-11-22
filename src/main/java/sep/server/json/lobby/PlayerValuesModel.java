package sep.server.json.lobby;

import sep.server.viewmodel.PlayerController;
import sep.server.json.AModel;

import org.json.JSONObject;

public class PlayerValuesModel extends AModel
{
    private final int playerID;
    private final String playerName;
    private final int figureID;

    public PlayerValuesModel(PlayerController pc, int playerID, String playerName, int figureID)
    {
        super(pc.getClientInstance());

        this.playerID = playerID;
        this.playerName = playerName;
        this.figureID = figureID;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("name", this.playerName);
        body.put("figure", this.figureID);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerAdded");
        j.put("messageBody", body);

        return j;
    }

}
