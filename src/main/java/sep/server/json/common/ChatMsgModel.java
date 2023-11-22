package sep.server.json.common;

import sep.server.viewmodel.ClientInstance;
import sep.server.json.AModel;

import org.json.JSONObject;

public class ChatMsgModel extends AModel
{
    public static final int MAX_MESSAGE_LENGTH = 64;
    public static final int CHAT_MSG_BROADCAST = -1;
    public static final int SERVER_ID = 0;

    private final int caller;
    private final String message;
    private final boolean bIsPrivate;

    public ChatMsgModel(ClientInstance ci, int caller, String message, boolean bIsPrivate)
    {
        super(ci);

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

}
