package sep.view.json;

import sep.view.lib.                EFigure;
import sep.view.lib.                RCoordinate;
import sep.view.lib.                RRegisterCard;
import sep.view.clientcontroller.   EConnectionLoss;
import sep.view.lib.                EAnimation;

import java.util.stream.    IntStream;
import org.json.            JSONObject;
import org.json.            JSONException;
import org.json.            JSONArray;

/** For easier access to the JSON object received from the server. Does not contain actual logic. */
public record RDefaultServerRequestParser(JSONObject request)
{
    public RDefaultServerRequestParser(final JSONObject request)
    {
        this.request = request;
        return;
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

    public EFigure getFigure() throws JSONException
    {
        return EFigure.fromInt(this.request.getJSONObject("messageBody").getInt("figure"));
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

    /** Only valid for NotYourCards request. Not the same as {@link #getCardsInHand()}. */
    public int getCardsInHandCountNYC() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("cardsInHand");
    }

    public String[] getForcedCards() throws JSONException
    {
        return IntStream.range(0, this.request.getJSONObject("messageBody").getJSONArray("cards").length()).mapToObj(i -> this.request.getJSONObject("messageBody").getJSONArray("cards").getString(i)).toArray(String[]::new);
    }

    public String getErrorMessage() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("error");
    }

    public int getRegister() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("register");
    }

    public boolean getRegisterFilled() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getBoolean("filled");
    }

    public String getRotation() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("rotation");
    }

    public int getWinningPlayer() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("clientID");
    }

    public int getEnergyCount() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("count");
    }

    public JSONArray getActiveCards() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getJSONArray("activeCards");
    }

    public String getCardName() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("card");
    }

    public String getActiveCardFromIdx(final int idx) throws JSONException
    {
        if (this.request.getJSONObject("messageBody").getJSONArray("activeCards").getJSONObject(idx).has("card"))
        {
            return this.request.getJSONObject("messageBody").getJSONArray("activeCards").getJSONObject(idx).getString("card");
        }

        return null;
    }

    public int getPlayerIDFromActiveCardIdx(final int idx) throws JSONException
    {
        return this.request.getJSONObject("messageBody").getJSONArray("activeCards").getJSONObject(idx).getInt("clientID");
    }

    public String getNewCard() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getString("newCard");
    }

    public int getCheckpointNumber() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("number");
    }

    public int getCheckpointMovedID() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("checkpointID");
    }

    public String[] getDrawnDamageCards() throws JSONException
    {
        return IntStream.range(0, this.request.getJSONObject("messageBody").getJSONArray("cards").length()).mapToObj(i -> this.request.getJSONObject("messageBody").getJSONArray("cards").getString(i)).toArray(String[]::new);
    }

    public int getDamageCardsCountToDraw() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getInt("count");
    }

    public String[] getAvailableDamagePilesToDraw() throws JSONException
    {
        return IntStream.range(0, this.request.getJSONObject("messageBody").getJSONArray("availablePiles").length()).mapToObj(i -> this.request.getJSONObject("messageBody").getJSONArray("availablePiles").getString(i)).toArray(String[]::new);
    }

    public boolean getIsConnected() throws JSONException
    {
        return this.request.getJSONObject("messageBody").getBoolean("isConnected");
    }

    public EConnectionLoss getNetAction() throws JSONException
    {
        return EConnectionLoss.fromString(this.request.getJSONObject("messageBody").getString("action"));
    }

    public RRegisterCard[] getCurrentRegisterCards() throws JSONException
    {
        return IntStream.range(0, this.request.getJSONObject("messageBody").getJSONArray("activeCards").length()).mapToObj(i -> new RRegisterCard(this.request.getJSONObject("messageBody").getJSONArray("activeCards").getJSONObject(i).getInt("clientID"), this.request.getJSONObject("messageBody").getJSONArray("activeCards").getJSONObject(i).getString("card"))).toArray(RRegisterCard[]::new);
    }

    public EAnimation getAnimation() throws JSONException
    {
        return EAnimation.fromString(this.request.getJSONObject("messageBody").getString("type"));
    }

}
