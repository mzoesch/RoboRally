package sep.server.json;

import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import java.io.IOException;

public class ChatMsgModel implements IJSONModel
{
    public static final int MAX_MESSAGE_LENGTH = 64;
    public static final int CHAT_MSG_BROADCAST = -1;
    public static final int SERVER_ID = 0;

    private final ClientInstance clientInstance;
    private final int caller;
    private final String message;
    private final boolean bIsPrivate;

    public ChatMsgModel(ClientInstance clientInstance, int caller, String message, boolean bIsPrivate)
    {
        super();

        this.clientInstance = clientInstance;
        this.caller = caller;
        this.message = message;
        this.bIsPrivate = bIsPrivate;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("message", this.message);
        body.put("from", this.caller);
        body.put("isPrivate", this.bIsPrivate);

        JSONObject j = new JSONObject();
        j.put("messageType", "ReceivedChat");
        j.put("messageBody", body);

        return j;
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
