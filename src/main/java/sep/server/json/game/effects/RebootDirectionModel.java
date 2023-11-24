package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class RebootDirectionModel extends AModel {

    private final String direction;

    public RebootDirectionModel(ClientInstance ci, String direction) {
        super(ci);
        this.direction = direction;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "RebootDirection");
        j.put("messageBody", new JSONObject().put("direction", this.direction));

        return j;
    }
}
