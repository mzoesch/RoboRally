package sep.view.json;

import org.json.JSONObject;
import org.json.JSONException;

public class DefaultServerRequestParser
{
    private final JSONObject request;

    public DefaultServerRequestParser(JSONObject request)
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

    public String getCaller() throws JSONException
    {
        return this.request.getString("caller");
    }

    public String getChatMessage() throws JSONException
    {
        return this.request.getString("message");
    }

    public String[] getPlayerNames() throws JSONException
    {
        return this.request.getJSONArray("playerNames").toList().toArray(new String[0]);
    }

    public String getHostPlayerName() throws JSONException
    {
        return this.request.getString("hostPlayerName");
    }

}
