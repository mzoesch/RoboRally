package sep.server.json.game.programmingphase;

import org.json.JSONArray;
import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

import java.lang.reflect.Array;

public class YourCardsModel extends AModel {

    private final String[] cards;

    public YourCardsModel(ClientInstance ci, String[] cards) {
        super(ci);
        this.cards = cards;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "YourCards");
        j.put("messageBody", new JSONObject().put("cardsInHand", this.cards));

        return j;
    }
}
