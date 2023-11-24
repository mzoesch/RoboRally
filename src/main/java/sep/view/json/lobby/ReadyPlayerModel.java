package sep.view.json.lobby;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

public class ReadyPlayerModel extends AServerRequestModel
{
    private final boolean bReady;

    public ReadyPlayerModel(boolean bReady)
    {
        super();
        this.bReady = bReady;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject j = new JSONObject();
        j.put("messageType", "SetStatus");
        j.put("messageBody", new JSONObject().put("ready", this.bReady));

        return j;
    }

}
