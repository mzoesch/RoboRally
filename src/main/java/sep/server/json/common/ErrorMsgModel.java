package sep.server.json.common;

import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;

public class ErrorMsgModel extends AModel
{
    private final String msg;

    public ErrorMsgModel(ClientInstance ci, String msg)
    {
        super(ci);
        this.msg = msg;
        return;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "Error");
        j.put("messageBody", new JSONObject().put("error", this.msg));

        return j;
    }

}
