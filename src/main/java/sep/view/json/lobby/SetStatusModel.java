package sep.view.json.lobby;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

public class SetStatusModel extends AServerRequestModel
{
    private final boolean bReady;

    public SetStatusModel(boolean bReady)
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
