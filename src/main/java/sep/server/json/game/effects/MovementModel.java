package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class MovementModel extends AModel {

    private final int playerID;
    private final int x;
    private final int y;
    public MovementModel(ClientInstance ci, int playerID, int x, int y) {
        super(ci);
        this.playerID = playerID;
        this.x = x;
        this.y = y;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("x", this.x);
        body.put("y", this.y);

        JSONObject j = new JSONObject();
        j.put("messageType", "Movement");
        j.put("messageBody", body);

        return j;
    }
}
