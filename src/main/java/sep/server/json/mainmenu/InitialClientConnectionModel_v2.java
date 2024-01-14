package sep.server.json.mainmenu;

import sep.                         Types;
import sep.server.viewmodel.        ClientInstance;

import org.json.                    JSONException;
import org.json.                    JSONObject;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/** Handles the initial client server handshake. */
public class InitialClientConnectionModel_v2
{
    private static final Logger l = LogManager.getLogger(InitialClientConnectionModel_v2.class);

    private final ClientInstance    ci;
    private JSONObject              response;

    public InitialClientConnectionModel_v2(final ClientInstance ci)
    {
        super();
        this.ci = ci;
        return;
    }

    public void sendProtocolVersion()
    {
        final JSONObject j = new JSONObject();
        j.put(  "messageType",  "HelloClient"                                                                                   );
        j.put(  "messageBody",  new JSONObject().put("protocol", String.format("Version %s", Types.EProps.VERSION.toString()))  );

        if (this.ci.sendRemoteRequest(j))
        {
            l.debug("Sent protocol version to client.");
            return;
        }

        l.fatal("Failed to send protocol version to client.");
        this.ci.disconnect();

        return;
    }

    public void waitForProtocolVersionConfirmation() throws JSONException
    {
        final String response = this.ci.waitForResponse();
        if (response == null)
        {
            l.error("Failed to receive response from client");
            return;
        }

        this.response = new JSONObject(response);
        return;
    }

    public void sendWelcome(final int playerID)
    {
        final JSONObject j = new JSONObject();
        j.put(  "messageType",  "Welcome"                                   );
        j.put(  "messageBody",  new JSONObject().put("clientID", playerID)  );

        if (this.ci.sendRemoteRequest(j))
        {
            l.debug("Sent welcome to client.");
            return;
        }

        l.fatal("Failed to send welcome to client.");
        this.ci.disconnect();

        return;
    }

    public boolean isClientProtocolVersionValid()
    {
        if (this.response == null)
        {
            return false;
        }

        try
        {
            if (!this.response.getString("messageType").equals("HelloServer"))
            {
                return false;
            }

            final JSONObject body = this.response.getJSONObject("messageBody");

            // Maybe we should check for legacy protocol versions in the future?
            if (!body.getString("protocol").equals(String.format("Version %s", Types.EProps.VERSION.toString())))
            {
                l.fatal("Client protocol version is not compatible with server protocol version.");
                return false;
            }

            return true;
        }
        catch (final JSONException e)
        {
            l.error("Failed to parse response from client");
            l.error(e.getMessage());
            return false;
        }

    }

    public boolean isRemoteAgent()
    {
        if (this.response == null)
        {
            return false;
        }

        try
        {
            return this.response.getJSONObject("messageBody").getBoolean("isAI");
        }
        catch (final JSONException e)
        {
            l.error("Failed to parse response from client");
            l.error(e.getMessage());
            return false;
        }
    }

    public String getSessionID()
    {
        if (this.response == null)
        {
            return null;
        }

        try
        {
            return this.response.getJSONObject("messageBody").getString("group");
        }
        catch (final JSONException e)
        {
            l.error("Failed to parse response from client");
            l.error(e.getMessage());
            return null;
        }
    }

}
