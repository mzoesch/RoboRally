package sep.server.json.common;

import sep.server.viewmodel.ClientInstance;
import sep.server.json.AModel;

import org.json.JSONObject;

public class ChatMsgModel extends AModel
{
    public static final int MAX_MESSAGE_LENGTH = 64;
    public static final int CHAT_MSG_BROADCAST = -1;
    public static final int SERVER_ID = 0;
    public static final String COMMAND_PREFIX = "/";
    public static final String ARG_SEPARATOR = ",";
    public static final String ARG_BEGIN = "{";
    public static final String ARG_END = "}";

    /**
     * Remote commands must be sent to the broadcasting channel and must have the following format:
     * <pre>{@link ChatMsgModel#COMMAND_PREFIX}COMMAND{@link ChatMsgModel#ARG_BEGIN}ARG1{@link ChatMsgModel#ARG_SEPARATOR}ARG2{@link ChatMsgModel#ARG_SEPARATOR}...{@link ChatMsgModel#ARG_END}</pre>
     */
    public static final String[] remoteCommands = new String[] { "DETACH" };

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
