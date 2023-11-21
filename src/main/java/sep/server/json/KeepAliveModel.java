package sep.server.json;

import org.json.JSONObject;

import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import org.json.JSONException;
import java.io.IOException;

public class KeepAliveModel implements IJSONModel
{
    private final ClientInstance ci;
    private JSONObject serverResponse;

    public KeepAliveModel(ClientInstance ci)
    {
        super();
        this.ci = ci;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject j = new JSONObject();
        j.put("messageType", "Alive");

        return j;
    }

    @Override
    public void send()
    {
//        System.out.printf(String.format("%s%n", this.toJSON().toString(4)));

        try
        {
            ci.getBufferedWriter().write(this.toJSON().toString());
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
