package sep.server.json.game.programmingphase;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class TimerStartedModel extends AModel {


    public TimerStartedModel(ClientInstance ci) {
        super(ci);
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "TimerStarted");

        return j;
    }
}
