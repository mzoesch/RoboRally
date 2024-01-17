package sep.view.json.mainmenu;

import sep.                         Types;
import sep.view.clientcontroller.   EClientInformation;
import sep.view.clientcontroller.   GameInstance;

import java.util.                   Objects;
// import java.util.                   UUID;
import org.json.                    JSONObject;
import org.json.                    JSONException;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     IOException;

public final class InitialClientConnectionModel
{
    private static final Logger l = LogManager.getLogger(InitialClientConnectionModel.class);

    private InitialClientConnectionModel() throws IllegalStateException
    {
        super();
        throw new IllegalStateException("Utility class.");
    }

    public static boolean checkServerProtocolVersion(final JSONObject obj) throws JSONException
    {
        if (!obj.getString("messageType").equals("HelloClient"))
        {
            return false;
        }

        l.debug(    "Server Protocol Version: {}",  obj.getJSONObject("messageBody").getString("protocol")          );
        l.debug(    "Client Protocol Version: {}",  String.format("Version %s", Types.EProps.VERSION.toString())    );

        return obj.getJSONObject("messageBody").getString("protocol").equals(String.format("Version %s", Types.EProps.VERSION.toString()));
    }

    public static void sendProtocolVersionConfirmation() throws IOException
    {
        /* TODO Validate group. */
        if (EClientInformation.INSTANCE.getPreferredSessionID().isEmpty() || EClientInformation.INSTANCE.getPreferredSessionID().isBlank())
        {
            /* UUID.randomUUID().toString().substring(0, 5) */ // Legacy. We may want to reuse this later on.
            EClientInformation.INSTANCE.setPreferredSessionID(Types.EConfigurations.isDev() ? "A" : Types.EProps.DESCRIPTION.toString());
        }

        final JSONObject b      = new JSONObject();
        b.put(  "group",        EClientInformation.INSTANCE.getPreferredSessionID()             );
        b.put(  "isAI",         EClientInformation.INSTANCE.isAgent()                           );
        b.put(  "protocol",     String.format("Version %s", Types.EProps.VERSION.toString())    );

        final JSONObject j      = new JSONObject();
        j.put(  "messageType",  "HelloServer"   );
        j.put(  "messageBody",  b               );

        GameInstance.sendServerRequest(j);

        return;
    }

    public static boolean checkPlayerID(final JSONObject welcome) throws JSONException
    {
        if (!welcome.getString("messageType").equals("Welcome"))
        {
            return false;
        }

        EClientInformation.INSTANCE.setPlayerID(welcome.getJSONObject("messageBody").getInt("clientID"));

        return true;
    }

    public static boolean isError(final JSONObject j)
    {
        try
        {
            return Objects.equals(j.getString("messageType"), "Error");
        }
        catch (final JSONException e)
        {
            return false;
        }
    }

    public static String getErrorMessage(final JSONObject j)
    {
        return j.getJSONObject("messageBody").getString("error");
    }
}
