package sep.view.json.game;

import org.json.JSONArray;
import org.json.JSONObject;
import sep.view.json.AServerRequestModel;

public class DiscardSomeModel extends AServerRequestModel {

    private final String[] cards;

    public DiscardSomeModel(String[] cards) {
        this.cards = cards;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();

        JSONArray cardsArray = new JSONArray();
        for (String card : cards) {
            cardsArray.put(card);
        }

        body.put("cards", cardsArray);

        JSONObject j = new JSONObject();
        j.put("messageType", "DiscardSome");
        j.put("messageBody", body);

        return j;
    }
}
