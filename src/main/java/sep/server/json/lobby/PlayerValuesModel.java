package sep.server.json.lobby;

import sep.server.viewmodel.PlayerController;

import org.json.JSONObject;
import java.io.IOException;
import sep.server.json.IJSONModel;

public class PlayerValuesModel implements IJSONModel
{
    private final PlayerController pc;
    private final int playerID;
    private final String playerName;
    private final int figureID;

    public PlayerValuesModel(PlayerController pc, int playerID, String playerName, int figureID)
    {
        super();

        this.pc = pc;
        this.playerID = playerID;
        this.playerName = playerName;
        this.figureID = figureID;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("name", this.playerName);
        body.put("figure", this.figureID);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerAdded");
        j.put("messageBody", body);

        return j;
    }

    @Override
    public void send()
    {
        // TODO Make this duplicate in the Client Instance.
        try
        {
            this.pc.getClientInstance().getBufferedWriter().write(this.toJSON().toString());
            this.pc.getClientInstance().getBufferedWriter().newLine();
            this.pc.getClientInstance().getBufferedWriter().flush();
        }
        catch (IOException e)
        {
            System.err.printf("[SERVER] Failed to send response to client%n");
            System.err.printf("[SERVER] %s%n", e.getMessage());
            return;
        }

        return;
    }
}
