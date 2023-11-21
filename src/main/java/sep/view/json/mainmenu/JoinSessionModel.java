package sep.view.json.mainmenu;

import org.json.JSONObject;
import sep.view.json.AResponseModel;

/** @deprecated  */
public non-sealed class JoinSessionModel extends AResponseModel
{
    private final String playerName;
    private final String sessionID;

    public JoinSessionModel(String playerName, String sessionID)
    {
        super();
        this.playerName = playerName;
        this.sessionID = sessionID;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject request = new JSONObject();
        request.put("connectionMethod", "joinSession");
        request.put("playerName", this.playerName);
        request.put("sessionID", this.sessionID);

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
