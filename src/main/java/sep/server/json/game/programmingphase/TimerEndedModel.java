package sep.server.json.game.programmingphase;

import org.json.JSONArray;
import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class TimerEndedModel extends AModel {

    private final int[] playerID;

    public TimerEndedModel(ClientInstance ci, int[] playerID) {
        super(ci);
        this.playerID = playerID;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientIDs", new JSONArray(playerID));

        JSONObject j = new JSONObject();
        j.put("messageType", "TimerEnded");
        j.put("messageBody",body);

        return j;
    }
}
