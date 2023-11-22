package sep.server.json;

import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import java.io.IOException;

public abstract class AModel implements IJSONSerializable
{
    ClientInstance ci;

    public AModel(ClientInstance ci)
    {
        super();
        this.ci = ci;
        return;
    }

    @Override
    public abstract JSONObject toJSON();

    public void send()
    {
        try
        {
            ci.getBufferedWriter().write(this.toJSON().toString());
            ci.getBufferedWriter().newLine();
            ci.getBufferedWriter().flush();
            return;
        }
        catch (IOException e)
        {
            System.err.printf("[SERVER] Failed to send response to client%n");
            System.err.printf("[SERVER] %s%n", e.getMessage());
            return;
        }
    }

}
