package sep.server.json.mainmenu;

import sep.server.viewmodel.ClientInstance;
import sep.server.model.EServerInformation;

import java.io.IOException;
import org.json.JSONObject;

/** Handles the initial client server handshake. */
public class InitialClientConnectionModelv2
{
    private final ClientInstance ci;

    public InitialClientConnectionModelv2(ClientInstance ci)
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

}
