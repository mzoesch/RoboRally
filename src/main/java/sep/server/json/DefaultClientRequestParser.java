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

    public Object getType_v2() throws JSONException
    {
        return this.request.get("messageType");
    }

    public String getChatMessage_v2() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("message");
    }

    public int getReceiverID() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("to");
    }

    public String getPlayerName() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("name");
    }

    public int getFigureID()
    {
        return this.request.getJSONObject("messageBody").getInt("figure");
    }

    public boolean getIsReadyInLobby()
    {
        return this.request.getJSONObject("messageBody").getBoolean("ready");
    }

    public String getCourseName()
    {
        return this.request.getJSONObject("messageBody").getString("map");
    }
}
