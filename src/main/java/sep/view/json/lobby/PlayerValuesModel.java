package sep.view.json.lobby;

import sep.view.clientcontroller.GameInstance;

import org.json.JSONObject;
import sep.view.json.IJSONModel;

import java.io.IOException;

public class PlayerValuesModel implements IJSONModel
{
    private final String PLAYER_NAME;
    private final int FIGURE_ID;

    public PlayerValuesModel(String playerName, int figureId)
    {
        this.PLAYER_NAME = playerName;
        this.FIGURE_ID = figureId;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("name", this.PLAYER_NAME);
        body.put("figure", this.FIGURE_ID);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerValues");
        j.put("messageBody", body);

        return j;
    }

    @Override
    public void send()
    {
        try
        {
            GameInstance.sendServerRequest(this.toJSON());
        }
        catch (IOException e)
        {
            System.err.println("[CLIENT] Could not send player values to server.");
            System.err.printf("[CLIENT] %s%n", e.getMessage());
        }

        return;
    }

}
