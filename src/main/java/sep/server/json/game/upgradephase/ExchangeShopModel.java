package sep.server.json.game.upgradephase;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class ExchangeShopModel extends AModel {
    private final String cards;

    public ExchangeShopModel(ClientInstance ci, String cards) {
        super(ci);
        this.cards = cards;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageType", "ExchangeShop");
        j.put("messageBody", new JSONObject().put("cards", this.cards));
        return null;
    }
}
