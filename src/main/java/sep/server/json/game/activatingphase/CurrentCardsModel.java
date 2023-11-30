package sep.server.json.game.activatingphase;

import org.json.JSONArray;
import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class CurrentCardsModel extends AModel {

    private final CardInfo[] activeCards;

    public CurrentCardsModel(ClientInstance ci, CardInfo[] activeCards) {
        super(ci);
        this.activeCards = activeCards;
    }

    @Override
    public JSONObject toJSON() {
        JSONArray cards = new JSONArray();
        for (CardInfo cardInfo : activeCards) {
            JSONObject cardObject = new JSONObject();
            cardObject.put("clientID", cardInfo.getClientID());
            cardObject.put("card", cardInfo.getCard());
            cards.put(cardObject);
        }

        JSONObject j = new JSONObject();
        j.put("messageType", "CurrentCards");
        j.put("messageBody", new JSONObject().put("activeCards", cards));

        return j;
    }
}
