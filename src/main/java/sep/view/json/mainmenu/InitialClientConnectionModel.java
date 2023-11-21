package sep.view.json.mainmenu;

import sep.view.clientcontroller.EClientInformation;
import sep.view.clientcontroller.GameInstance;

import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;

public final class InitialClientConnectionModel
{
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

        System.out.printf("[CLIENT] Server Protocol %s. Client Protocol %s.%n", jsonObject.getJSONObject("messageBody").getString("protocol"), String.format("Version %s", EClientInformation.PROTOCOL_VERSION));
        return jsonObject.getJSONObject("messageBody").getString("protocol").equals(String.format("Version %s", EClientInformation.PROTOCOL_VERSION));
    }

    public static void sendProtocolVersionConfirmation() throws IOException
    {
        JSONObject messageBody = new JSONObject();
        messageBody.put("group", "???");
        messageBody.put("isAI", false);
        messageBody.put("protocol", String.format("Version %s", EClientInformation.PROTOCOL_VERSION));

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
