package sep.view.json.lobby;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

public final class PlayerValuesModel extends AServerRequestModel
{
    private final String PLAYER_NAME;
    private final int FIGURE_ID;

    public PlayerValuesModel(String playerName, int figureId)
    {
        this.PLAYER_NAME = playerName;
        this.FIGURE_ID = figureId;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("name", this.PLAYER_NAME);
        body.put("figure", this.FIGURE_ID);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerValues");
        j.put("messageBody", body);

        return j;
    }

}
