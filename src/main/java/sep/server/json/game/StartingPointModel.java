package sep.server.json.game;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class StartingPointModel extends AModel {

    private final int x;
    private final int y;
    private final int playerID;

    public StartingPointModel(ClientInstance ci, int x, int y, int playerID) {
        super(ci);
        this.x = x;
        this.y = y;
        this.playerID = playerID;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("x", this.x);
        body.put("y", this.y);
        body.put("cientID", this.playerID);

        JSONObject j = new JSONObject();
        j.put("messageType", "StartingPointTaken");
        j.put("messageBody", body);

        return j;
    }
}
