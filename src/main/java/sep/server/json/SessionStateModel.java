package sep.server.json;

import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import java.io.IOException;

/** @deprecated */
public class SessionStateModel extends AModel
{
    private final ClientInstance clientInstance;
    private final String[] playerNames;
    private final String hostPlayerName;

    public SessionStateModel(ClientInstance clientInstance, String[] playerNames, String hostPlayerName)
    {
        super(clientInstance);

        this.clientInstance = clientInstance;
        this.playerNames = playerNames;
        this.hostPlayerName = hostPlayerName;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject request = new JSONObject();
        request.put("type", "sessionState");
        request.put("playerNames", this.playerNames);
        request.put("hostPlayerName", this.hostPlayerName);

        return request;
    }

    @Override
    public void send()
    {
        try
        {
            this.clientInstance.getBufferedWriter().write(this.toJSON().toString());
            this.clientInstance.getBufferedWriter().newLine();
            this.clientInstance.getBufferedWriter().flush();

            return;
        }
        catch (IOException e)
        {
            System.err.println("[CLIENT] Failed to send request to client.");
            System.err.println(e.getMessage());
            return;
        }
    }

}
