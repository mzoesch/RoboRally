package sep.view.json;

import sep.view.lib.Coordinate;

import org.json.JSONObject;
import org.json.JSONException;
import java.util.stream.IntStream;
import org.json.JSONArray;

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

    public boolean isLobbyPlayerStatusReady() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getBoolean("ready");
    }

    public String[] getAvailableCourses() throws JSONException
    {
        return IntStream.range(0, this.request.getJSONObject("messageBody").getJSONArray("availableMaps").length()).mapToObj(i -> this.request.getJSONObject("messageBody").getJSONArray("availableMaps").getString(i)).toArray(String[]::new);
    }

    public String getCourseName() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("map");
    }

    public JSONArray getGameCourse() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getJSONArray("gameMap");
    }

    public int getPhase() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("phase");
    }

    public Coordinate getCoordinate() throws JSONException
    {
        return new Coordinate(this.request.getJSONObject("messageBody").getInt("x"), this.request.getJSONObject("messageBody").getInt("y"));
    }

}
