package sep.server.json.lobby;

import sep.server.viewmodel.PlayerController;
import sep.server.json.AModel;
import sep.server.model.game.GameState;

import org.json.JSONObject;

public class CourseSelectedModel extends AModel
{
    private final String courseName;

    public CourseSelectedModel(PlayerController PC, String courseName)
    {
        super(PC.getClientInstance());
        this.courseName = courseName;
        return;
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
