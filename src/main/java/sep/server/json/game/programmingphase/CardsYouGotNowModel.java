package sep.server.json.game.programmingphase;

import org.json.JSONArray;
import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class CardsYouGotNowModel extends AModel {

    private final String[] cards;

    public CardsYouGotNowModel(ClientInstance ci, String[] cards) {
        super(ci);
        this.cards = cards;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("cards", new JSONArray(cards));

        JSONObject j = new JSONObject();
        j.put("messageType", "CardsYouGotNow");
        j.put("messageBody",body);
        return j;
    }
}
