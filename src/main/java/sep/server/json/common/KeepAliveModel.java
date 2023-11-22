package sep.server.json.common;

import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;

public class KeepAliveModel extends AModel
{
    public KeepAliveModel(ClientInstance ci)
    {
        super(ci);
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject j = new JSONObject();
        j.put("messageType", "Alive");
        return j;
    }

}
