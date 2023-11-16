package sep.server.json;

import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import java.io.IOException;

public class ChatMessageModel implements IJSONModel
{
    public static final int MAX_MESSAGE_LENGTH = 64;

    private final ClientInstance clientInstance;
    private final String caller;
    private final String message;

    public ChatMessageModel(ClientInstance clientInstance, String caller, String message)
    {
        super();

        this.clientInstance = clientInstance;
        this.caller = caller;
        this.message = message;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject json = new JSONObject();
        json.put("type", "chatMessage");
        json.put("caller", this.caller);
        json.put("message", this.message);

        return json;
    }

    @Override
    public void send()
    {
        try
        {
            this.clientInstance.getBufferedWriter().write(this.toJSON().toString());
            this.clientInstance.getBufferedWriter().newLine();
            this.clientInstance.getBufferedWriter().flush();

        }
        catch (IOException e)
        {
            System.err.println("[CLIENT] Failed to send server request.");
            System.err.println(e.getMessage());
            return;
        }

        return;
    }

}
