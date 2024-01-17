package sep.view.json.game;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class DiscardSomeModel extends AModel {

    private final String cards;

    public DiscardSomeModel(ClientInstance ci, String cards) {
        super(ci);
        this.cards = cards;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "DiscardSome");
        j.put("messageBody", new JSONObject().put("cards", this.cards));

        return j;
    }
}
