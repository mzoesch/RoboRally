package sep.server.json.mainmenu;

import sep.server.viewmodel.ClientInstance;
import sep.server.model.EServerInformation;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

/** Handles the initial client server handshake. */
public class InitialClientConnectionModel_v2
{
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

        System.out.printf(String.format("%s%n", j.toString(4)));

        try
        {
            ci.getBufferedWriter().write(j.toString());
            ci.getBufferedWriter().newLine();
            ci.getBufferedWriter().flush();
        }
        catch (IOException e)
        {
            System.err.printf("[SERVER] Failed to send response to client%n");
            System.err.printf("[SERVER] %s%n", e.getMessage());
            return;
        }

        return;
    }

    public void waitForProtocolVersionConfirmation() throws JSONException
    {
        String response = this.ci.waitForResponse();
        if (response == null)
        {
            System.err.printf("[SERVER] Failed to receive response from client%n");
            return;
        }

        this.response = new JSONObject(response);
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

            JSONObject messageBody = this.response.getJSONObject("messageBody");

            // Maybe we should check for legacy protocol versions in the future?
            if (!messageBody.getString("protocol").equals(String.format("Version %s", EServerInformation.PROTOCOL_VERSION)))
            {
                System.err.println("[SERVER] Client protocol version is not compatible with server protocol version.");
                return false;
            }

            return true;
        }
        catch (JSONException e)
        {
            System.err.printf("[SERVER] Failed to parse response from client%n");
            System.err.printf("[SERVER] %s%n", e.getMessage());
            return false;
        }

    }
}
