package sep.view.json.mainmenu;

import org.json.JSONObject;
import sep.view.json.AResponseModel;

/** @deprecated */
public non-sealed class CreateSessionModel extends AResponseModel
{
    private final String playerName;

    public CreateSessionModel(String playerName)
    {
        super();
        this.playerName = playerName;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject request = new JSONObject();
        request.put("connectionMethod", "createSession");
        request.put("playerName", this.playerName);

        return request;
    }

    public String getSessionID()
    {
        if (this.response == null)
        {
            System.err.println("[CLIENT] Response not received.");
            return null;
        }

        return this.response.getString("lobbyID");
    }

}
