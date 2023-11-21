package sep.view.json;

import org.json.JSONObject;

public class ChatMsgModel extends AServerRequestModel
{
    public static final String COMMAND_PREFIX = "/";
    public static final int CHAT_MSG_BROADCAST = -1;
    public static final int SERVER_ID = 0;
    public static final int CLIENT_ID = -1;
    public static final String SERVER_NAME = "SERVER";
    public static final String CLIENT_NAME = "CLIENT";

    private final String msg;
    private final int receiver;

    public ChatMsgModel(String msg, int receiver)
    {
        super();
        this.msg = msg;
        this.receiver = receiver;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("message", this.msg);
        body.put("to", this.receiver);

        JSONObject j = new JSONObject();
        j.put("messageType", "SendChat");
        j.put("messageBody", body);

        return j;
    }

}
