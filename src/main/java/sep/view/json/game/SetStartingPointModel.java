package sep.view.json.game;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

public class SetStartingPointModel extends AServerRequestModel
{
    private final int x;
    private final int y;

    public SetStartingPointModel(int x, int y)
    {
        this.x = x;
        this.y = y;

        return;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("x", this.x);
        body.put("y", this.y);

        JSONObject j = new JSONObject();
        j.put("messageType", "SetStartingPoint");
        j.put("messageBody", body);

        return j;
    }

}
