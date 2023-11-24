package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class GameFinishedModel extends AModel {

    private final int playerID;

    public GameFinishedModel(ClientInstance ci, int playerID) {
        super(ci);
        this.playerID = playerID;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "GameFinished");
        j.put("messageBody", new JSONObject().put("clientID", this.playerID));

        return j;
    }
}
