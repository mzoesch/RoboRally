package sep.view.json;

import org.json.JSONObject;
import java.io.IOException;
import sep.view.clientcontroller.GameInstance;

public abstract class AServerRequestModel
{
    public AServerRequestModel()
    {
        super();
        return;
    }

    public abstract JSONObject toJSON();

    public void send()
    {
        try
        {
            GameInstance.sendServerRequest(this.toJSON());
        }
        catch (IOException e)
        {
            System.err.printf("[CLIENT] Failed to send request to server%n");
            System.err.printf("[CLIENT] %s%n", e.getMessage());
            return;
        }

        return;
    }
}
