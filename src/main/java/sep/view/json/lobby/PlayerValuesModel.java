package sep.view.json.lobby;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

public final class PlayerValuesModel extends AServerRequestModel
{
    private final String PLAYERNAME;
    private final int FIGURE_ID;

    public PlayerValuesModel(String PLAYERNAME, int FIGURE_ID)
    {
        this.PLAYERNAME = PLAYERNAME;
        this.FIGURE_ID = FIGURE_ID;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("name", this.PLAYERNAME);
        body.put("figure", this.FIGURE_ID);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerValues");
        j.put("messageBody", body);

        return j;
    }

}
