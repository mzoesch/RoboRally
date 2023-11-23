package sep.view.json;

import sep.view.clientcontroller.GameInstance;

import org.json.JSONObject;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AServerRequestModel
{
    private static final Logger l = LogManager.getLogger(AServerRequestModel.class);

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
            l.error("Failed to send request to server");
            l.error(e.getMessage());
            return;
        }

        return;
    }
}
