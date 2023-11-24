package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class PlayerTurningModel extends AModel {

    private final int playerID;
    private final String rotation;

    public PlayerTurningModel(ClientInstance ci, int playerID, String rotation) {
        super(ci);
        this.playerID = playerID;
        this.rotation = rotation;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("rotation", this.rotation);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayerTurning");
        j.put("messageBody", body);

        return j;
    }
}
