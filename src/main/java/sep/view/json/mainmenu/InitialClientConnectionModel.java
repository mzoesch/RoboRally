package sep.view.json.mainmenu;

import sep.view.clientcontroller.EClientInformation;
import sep.view.clientcontroller.GameInstance;

import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InitialClientConnectionModel
{
    private static final Logger l = LogManager.getLogger(InitialClientConnectionModel.class);

    private InitialClientConnectionModel() throws IllegalStateException
    {
        super();
        throw new IllegalStateException("Utility class");
    }

    public static boolean checkServerProtocolVersion(JSONObject jsonObject) throws JSONException
    {
        if (!jsonObject.getString("messageType").equals("HelloClient"))
        {
            return false;
        }

        l.debug("Server Protocol Version: {}", jsonObject.getJSONObject("messageBody").getString("protocol"));
        l.debug("Client Protocol Version: {}", String.format("Version %s", sep.Types.Props.VERSION.toString()));
        return jsonObject.getJSONObject("messageBody").getString("protocol").equals(String.format("Version %s", sep.Types.Props.VERSION.toString()));
    }

    public static void sendProtocolVersionConfirmation() throws IOException
    {
        // TODO Validate group
        if (EClientInformation.INSTANCE.getPreferredSessionID().isEmpty() || EClientInformation.INSTANCE.getPreferredSessionID().isBlank())
        {
            EClientInformation.INSTANCE.setPreferredSessionID(UUID.randomUUID().toString().substring(0, 5));
        }

        JSONObject messageBody = new JSONObject();
        messageBody.put("group", EClientInformation.INSTANCE.getPreferredSessionID());
        messageBody.put("isAI", false);
        messageBody.put("protocol", String.format("Version %s", sep.Types.Props.VERSION.toString()));

        JSONObject j = new JSONObject();
        j.put("messageType", "HelloServer");
        j.put("messageBody", messageBody);

        GameInstance.sendServerRequest(j);

        return;
    }

    public static boolean checkPlayerID(JSONObject welcome) throws JSONException
    {
        if (!welcome.getString("messageType").equals("Welcome"))
        {
            return false;
        }

        EClientInformation.INSTANCE.setPlayerID(welcome.getJSONObject("messageBody").getInt("clientID"));

        return true;
    }

}
