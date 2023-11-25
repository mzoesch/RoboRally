package sep.view.json.lobby;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

public class CourseSelectedModel extends AServerRequestModel
{
    private final String COURSE_NAME;

    public CourseSelectedModel(String courseName)
    {
        this.COURSE_NAME = courseName;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject j = new JSONObject();
        j.put("messageType", "MapSelected");
        j.put("messageBody", new JSONObject().put("map", this.COURSE_NAME));

        return j;
    }

}
