package sep.view.json.lobby;

import sep.view.json.AServerRequestModel;
import sep.view.lib.EFigure;

import org.json.JSONObject;

public final class PlayerValuesModel extends AServerRequestModel
{
    private final String playerName;
    private final EFigure f;

    public PlayerValuesModel(final String playerName, final EFigure f)
    {
        this.playerName = playerName;
        this.f = f;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("name", this.playerName);
        body.put("figure", this.f.i);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerValues");
        j.put("messageBody", body);

        return j;
    }

}
