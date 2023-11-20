package sep.server.json;

import org.json.JSONObject;
import org.json.JSONException;

// Do not convert to record just yet, as we will add more fields to this class.
public class DefaultClientRequestParser
{
    private final JSONObject request;

    public DefaultClientRequestParser(JSONObject request)
    {
        super();
        this.request = request;
        return;
    }

    public JSONObject getRequest()
    {
        return this.request;
    }

    public String getType() throws JSONException
    {
        return this.request.getString("type");
    }

    public String getChatMessage() throws JSONException
    {
        return this.request.getString("message");
    }

    public Object getType_v2()
    {
        return this.request.get("messageType");
    }
}
