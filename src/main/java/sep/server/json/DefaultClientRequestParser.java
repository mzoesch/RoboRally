package sep.server.json;

import org.json.JSONObject;
import org.json.JSONException;

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

}
