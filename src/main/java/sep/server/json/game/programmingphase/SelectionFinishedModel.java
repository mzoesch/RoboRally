package sep.server.json.game.programmingphase;

import org.json.JSONObject;
import sep.view.json.AServerRequestModel;

public class SelectionFinishedModel extends AServerRequestModel {

    private final int playerId;

    public SelectionFinishedModel(int playerId) {
        this.playerId = playerId;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "SelectionFinished");
        j.put("messageBody", new JSONObject().put("clientID", this.playerId));

        return j;
    }
}
