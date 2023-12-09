package sep.server.json.lobby;

import sep.server.viewmodel.PlayerController;
import sep.server.json.AModel;
import sep.server.model.game.GameState;

import org.json.JSONObject;

public class SelectMapModel extends AModel
{
    public SelectMapModel(PlayerController pc)
    {
        super(pc.getClientInstance());
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject j = new JSONObject();
        j.put("messageType", "SelectMap");
        j.put("messageBody", new JSONObject().put("availableMaps", GameState.getAvailableCourses()));

        return j;
    }

}
