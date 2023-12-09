package sep.server.json.lobby;

import sep.server.viewmodel.PlayerController;
import sep.server.json.AModel;

import org.json.JSONObject;

public class MapSelectedModel extends AModel
{
    private final String courseName;

    public MapSelectedModel(PlayerController PC, String courseName)
    {
        super(PC.getClientInstance());
        this.courseName = courseName;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject j = new JSONObject();
        j.put("messageType", "MapSelected");
        j.put("messageBody", new JSONObject().put("map", this.courseName));

        return j;
    }

}
