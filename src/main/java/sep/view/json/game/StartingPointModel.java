package sep.view.json.game;

import org.json.JSONObject;
import sep.view.json.AServerRequestModel;

public class StartingPointModel extends AServerRequestModel {

    private final int x;
    private final int y;

    public StartingPointModel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("x", this.x);
        body.put("y", this.y);

        JSONObject j = new JSONObject();
        j.put("messageType", " SetStartingPoint");
        j.put("messageBody", body);

        return j;
    }
}
