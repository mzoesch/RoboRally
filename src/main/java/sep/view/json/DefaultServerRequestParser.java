package sep.view.json;

import org.json.JSONObject;
import org.json.JSONException;

/**
 * For easier access to the JSON object received from the server. Does not contain actual logic.
 * We may want to split this into multiple classes later if it gets too messy.
 */
public final class DefaultServerRequestParser
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

    public String getType_v2() throws JSONException
    {
        return this.request.getString("messageType");
    }

    public int getPlayerID() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("clientID");
    }

    public String getPlayerName() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("name");
    }

    public int getFigureID() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("figure");
    }

    public boolean isChatMsgPrivate() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getBoolean("isPrivate");
    }

    public String getChatMsg() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("message");
    }

    public int getChatMsgSourceID() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("from");
    }

}
