package sep.view.json.common;

import sep.view.clientcontroller.EClientInformation;
import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

public class IdentificationModel extends AServerRequestModel
{
    private final String sessionID;
    private final boolean bIsAgent;

    public IdentificationModel(final String sessionID, boolean bIsAgent)
    {
        super();

        this.sessionID = sessionID;
        this.bIsAgent = bIsAgent;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        final JSONObject b = new JSONObject();
        b.put("group", this.sessionID);
        b.put("isAI", this.bIsAgent);
        b.put("protocol", String.format("Version %s", EClientInformation.PROTOCOL_VERSION));

        final JSONObject j = new JSONObject();
        j.put("messageType", "HelloServer");
        j.put("messageBody", b);

        return j;
    }

}
