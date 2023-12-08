package sep.server.json;

import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AModel implements IJSONSerializable
{
    private static final Logger l = LogManager.getLogger(AModel.class);

    private final ClientInstance ci;

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
        this.ci.sendRemoteRequest(this.toJSON());
        return;
    }

}
