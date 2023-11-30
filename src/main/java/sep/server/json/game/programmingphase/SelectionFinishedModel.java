package sep.server.json.game.programmingphase;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;
import sep.view.json.AServerRequestModel;

public class SelectionFinishedModel extends AModel {

    private final int playerId;

    public SelectionFinishedModel(ClientInstance ci, int playerId) {
        super(ci);
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
