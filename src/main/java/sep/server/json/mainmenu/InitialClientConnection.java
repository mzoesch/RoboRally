package sep.server.json.mainmenu;

import org.json.JSONObject;
import java.io.BufferedWriter;
import org.json.JSONException;
import java.io.IOException;

public class InitialClientConnection
{
    private final JSONObject json;

    public InitialClientConnection(JSONObject json)
    {
        super();
        this.json = json;
        return;
    }

    private static void send(BufferedWriter bufferedWriter, JSONObject response)
    {
        try
        {
            bufferedWriter.write(response.toString());
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
        catch (IOException e)
        {
            System.err.printf("[SERVER] Failed to send response to client%n");
            System.err.printf("[SERVER] %s%n", e.getMessage());
            return;
        }

        return;
    }

    public static void sendPositive(BufferedWriter bufferedWriter, String lobbyID)
    {
        JSONObject response = new JSONObject();
        response.put("connectionState", true);
        response.put("lobbyID", lobbyID);

        InitialClientConnection.send(bufferedWriter, response);

        return;
    }

    public static void sendNegative(BufferedWriter bufferedWriter, String reason)
    {
        JSONObject response = new JSONObject();
        response.put("connectionState", false);
        response.put("errorMessage", reason);

        InitialClientConnection.send(bufferedWriter, response);

        return;
    }

    public JSONObject getJSON()
    {
        return this.json;
    }

    public String getConnectionMethod() throws JSONException
    {
        return this.json.getString("connectionMethod");
    }

    public String getSessionID()
    {
        return this.json.getString("sessionID");
    }

    public String getPlayerName()
    {
        return this.json.getString("playerName");
    }

}
