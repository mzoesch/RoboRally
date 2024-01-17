package sep.server.json;

import sep.server.model.game.tiles. Coordinate;

import org.json.                    JSONObject;
import org.json.                    JSONException;

/**
 * For easier access to the JSON object received from the client. Does not contain actual logic.
 * We may want to split this into multiple classes later if it gets too messy.
 */
public record RDefaultClientRequestParser(JSONObject request)
{
    public RDefaultClientRequestParser(final JSONObject request)
    {
        this.request = request;
        return;
    }

    public String getType_v2() throws JSONException
    {
        return this.request.getString("messageType");
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

    public int getFigureID() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("figure");
    }

    public boolean getIsReadyInLobby() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getBoolean("ready");
    }

    public String getCourseName() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("map");
    }

    /** @deprecated  */
    public int getXCoordinate() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("x");
    }

    /** @deprecated  */
    public int getYCoordinate() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("y");
    }

    public Coordinate getCoordinate() throws JSONException
    {
        return new Coordinate(this.request.getJSONObject("messageBody").getInt("x"), this.request.getJSONObject("messageBody").getInt("y"));
    }

    public String getSelectedCardAsString() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("card");
    }

    public int getSelectedCardRegister() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("register");
    }

    public JSONObject getBody() throws JSONException
    {
        return this.request.getJSONObject("messageBody");
    }

    public String getDirection() throws JSONException {
        return this.request.getJSONObject("messageBody").getString("direction");
    }

}
