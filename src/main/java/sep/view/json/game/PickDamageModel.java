package sep.view.json.game;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class PickDamageModel extends AModel {

    private final int count;

    private final String[] availablePiles;

    public PickDamageModel(ClientInstance ci, int count, String[] availablePiles) {
        super(ci);
        this.count = count;
        this.availablePiles = availablePiles;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("count", this.count);
        body.put("availablePiles", this.availablePiles);

        JSONObject j = new JSONObject();
        j.put("messageTpye", "PickDamage");
        j.put("messageBody", body);

        return j;
    }
}
