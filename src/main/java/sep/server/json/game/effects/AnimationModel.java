package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class AnimationModel extends AModel {

    private final String type;

    public AnimationModel(ClientInstance ci, String type) {
        super(ci);
        this.type = type;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "Animation");
        j.put("messageBody", new JSONObject().put("type", this.type));

        return j;
    }
}
