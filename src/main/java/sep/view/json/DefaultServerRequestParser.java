package sep.view.json;

import sep.view.lib.RCoordinate;
import sep.view.clientcontroller.EConnectionLoss;

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

    public RCoordinate getCoordinate() throws JSONException
    {
        return new RCoordinate(this.request.getJSONObject("messageBody").getInt("x"), this.request.getJSONObject("messageBody").getInt("y"));
    }

    public String[] getCardsInHand() throws JSONException
    {
        return IntStream.range(0, this.request.getJSONObject("messageBody").getJSONArray("cardsInHand").length()).mapToObj(i -> this.request.getJSONObject("messageBody").getJSONArray("cardsInHand").getString(i)).toArray(String[]::new);
    }

    /** Only valid for Not Your Cards request. Not the same as getCardsInHand(). */
    public int getCardsInHandCountNYC() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("cardsInHand");
    }

    public String[] getForcedCards() throws JSONException
    {
        return IntStream.range(0, this.request.getJSONObject("messageBody").getJSONArray("cards").length()).mapToObj(i -> this.request.getJSONObject("messageBody").getJSONArray("cards").getString(i)).toArray(String[]::new);
    }

    public String getErrorMessage()
    {
        return this.request.getJSONObject("messageBody").getString("error");
    }

    public int getRegister()
    {
        return this.request.getJSONObject("messageBody").getInt("register");
    }

    public boolean getRegisterFilled()
    {
        return this.request.getJSONObject("messageBody").getBoolean("filled");
    }

    public String getRotation()
    {
        return this.request.getJSONObject("messageBody").getString("rotation");
    }

    public int getWinningPlayer(){ return this.request.getJSONObject("messageBody").getInt("clientID");}

    public int getEnergyCount() { return this.request.getJSONObject("messageBody").getInt("count");
    }

    public JSONArray getActiveCards() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getJSONArray("activeCards");
    }

    public String getCardName(){return this.request.getJSONObject("messageBody").getString("card");}

    public String getActiveCardFromIdx(final int idx) throws JSONException
    {
        return this.request.getJSONObject("messageBody").getJSONArray("activeCards").getJSONObject(idx).getString("card");
    }

    public int getPlayerIDFromActiveCardIdx(final int idx) throws JSONException
    {
        return this.request.getJSONObject("messageBody").getJSONArray("activeCards").getJSONObject(idx).getInt("clientID");
    }

    public String getNewCard() { return this.request.getJSONObject("messageBody").getString("newCard");
    }

    public int getNumber() { return this.request.getJSONObject("messageBody").getInt("number");
    }

    public String getCardsAsString() { return this.request.getJSONObject("messageBody").getJSONArray("cards").toString();

    }

    public String[] getDrawnDamageCards()
    {
        return IntStream.range(0, this.request.getJSONObject("messageBody").getJSONArray("cards").length()).mapToObj(i -> this.request.getJSONObject("messageBody").getJSONArray("cards").getString(i)).toArray(String[]::new);
    }

    public int getDamageCardsCountToDraw()
    {
        return this.request.getJSONObject("messageBody").getInt("count");
    }

    public String[] getAvailableDamagePilesToDraw()
    {
        return IntStream.range(0, this.request.getJSONObject("messageBody").getJSONArray("availablePiles").length()).mapToObj(i -> this.request.getJSONObject("messageBody").getJSONArray("availablePiles").getString(i)).toArray(String[]::new);
    }

    public String getAvailablePilesAsString() { return this.request.getJSONObject("messageBody").getJSONArray("availablePiles").toString();
    }

    public boolean getIsConnected()
    {
        return this.request.getJSONObject("messageBody").getBoolean("isConnected");
    }

    public EConnectionLoss getNetAction()
    {
        return EConnectionLoss.fromString(this.request.getJSONObject("messageBody").getString("action"));
    }

}
