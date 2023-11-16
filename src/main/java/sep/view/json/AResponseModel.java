package sep.view.json;

import sep.view.clientcontroller.GameInstance;

import org.json.JSONObject;
import java.io.IOException;

public abstract class AResponseModel
{
    protected JSONObject response;

    public AResponseModel()
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


    public void waitForResponse()
    {
        if (this.response != null)
        {
            System.err.println("[CLIENT] Response already received.");
            return;
        }

        try
        {
            this.response = GameInstance.waitForServerResponse();
        }
        catch (IOException e)
        {
            System.err.printf("[CLIENT] Failed to receive server response%n");
            System.err.printf("[CLIENT] %s%n", e.getMessage());
            return;
        }

        return;
    }

    public boolean isConnectionStateInvalid()
    {
        if (this.response == null)
        {
            System.err.println("[CLIENT] Response not received.");
            return true;
        }

        return !this.response.getBoolean("connectionState");
    }

    public String getErrorMessage()
    {
        if (this.response == null)
        {
            System.err.println("[CLIENT] Response not received.");
            return null;
        }

        return this.response.getString("errorMessage");
    }

    public JSONObject getResponse()
    {
        return this.response;
    }

}
