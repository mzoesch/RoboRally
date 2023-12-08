package sep.server.json.mainmenu;

import sep.server.viewmodel.ClientInstance;
import sep.server.model.EServerInformation;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Handles the initial client server handshake. */
public class InitialClientConnectionModel_v2
{
    private static final Logger l = LogManager.getLogger(InitialClientConnectionModel_v2.class);

    private final ClientInstance ci;
    private JSONObject response;

    public InitialClientConnectionModel_v2(ClientInstance ci)
    {
        super();
        this.ci = ci;
        return;
    }

    public void sendProtocolVersion()
    {
        JSONObject j = new JSONObject();
        j.put("messageType", "HelloClient");
        j.put("messageBody", new JSONObject().put("protocol", String.format("Version %s", EServerInformation.PROTOCOL_VERSION)));

        try
        {
            ci.getBufferedWriter().write(j.toString());
            ci.getBufferedWriter().newLine();
            ci.getBufferedWriter().flush();
        }
        catch (IOException e)
        {
            l.fatal("Failed to send response to client.");
            l.fatal(e.getMessage());
            return;
        }

        return;
    }

    public void waitForProtocolVersionConfirmation() throws JSONException
    {
        String response = this.ci.waitForResponse();
        if (response == null)
        {
            l.error("Failed to receive response from client");
            return;
        }

        this.response = new JSONObject(response);
        return;
    }

    public void sendWelcome(int playerID)
    {
        JSONObject j = new JSONObject();
        j.put("messageType", "Welcome");
        j.put("messageBody", new JSONObject().put("clientID", playerID));

        try
        {
            ci.getBufferedWriter().write(j.toString());
            ci.getBufferedWriter().newLine();
            ci.getBufferedWriter().flush();
        }
        catch (IOException e)
        {
            l.fatal("Failed to send response to client.");
            l.fatal(e.getMessage());
            return;
        }

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

            JSONObject messageBody = this.response.getJSONObject("messageBody");

            // Maybe we should check for legacy protocol versions in the future?
            if (!messageBody.getString("protocol").equals(String.format("Version %s", EServerInformation.PROTOCOL_VERSION)))
            {
                l.fatal("Client protocol version is not compatible with server protocol version.");
                return false;
            }

            return true;
        }
        catch (JSONException e)
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
        catch (JSONException e)
        {
            l.error("Failed to parse response from client");
            l.error(e.getMessage());
            return null;
        }
    }

}

