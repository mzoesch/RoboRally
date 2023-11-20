package sep.view.json.mainmenu;

import sep.view.clientcontroller.EClientInformation;

import org.json.JSONObject;
import org.json.JSONException;

public class InitialClientConnectionModel
{
    public static boolean checkServerProtocolVersion(JSONObject jsonObject) throws JSONException
    {
        if (!jsonObject.getString("messageType").equals("HelloClient"))
        {
            return false;
        }

        System.out.printf("[CLIENT] Server protocol version: %s%n", jsonObject.getJSONObject("messageBody").getString("protocol"));
        return jsonObject.getJSONObject("messageBody").getString("protocol").equals(String.format("Version %s", EClientInformation.PROTOCOL_VERSION));
    }

}
