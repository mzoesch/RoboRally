package sep.view.json;

import org.json.JSONObject;

public class ChatMessageModel extends AServerRequestModel
{
    public static final int MAX_MESSAGE_LENGTH = 64;

    private final String caller;
    private final String message;

    public ChatMessageModel(String caller, String message)
    {
        super();

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

}
