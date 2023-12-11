package sep.view.json.lobby;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

public final class PlayerValuesModel extends AServerRequestModel
{
    private final String playerName;
    private final int figureID;

    public PlayerValuesModel(final String playerName, final int figureID)
    {
        this.playerName = playerName;
        this.figureID = figureID;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("name", this.playerName);
        body.put("figure", this.figureID);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerValues");
        j.put("messageBody", body);

        return j;
    }

}
