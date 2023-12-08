package sep.server.json.game.damage;

import org.json.JSONArray;
import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class SelectedDamageModel extends AModel {

    private final String[] cards;

    public SelectedDamageModel(ClientInstance ci, String[] cards) {
        super(ci);
        this.cards = cards;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "SelectedDamage");
        j.put("messageBody", new JSONObject().put(("cards"), this.cards)); // muss nicht eigentlich JSONArray sein?

        return j;
    }
}
