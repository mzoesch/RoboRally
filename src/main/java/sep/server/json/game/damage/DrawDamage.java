package sep.server.json.game.damage;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class DrawDamage extends AModel {

    private final int playerID;

    private final String[] cards;

    public DrawDamage(ClientInstance ci, int playerID, String[] cards) {
        super(ci);
        this.playerID = playerID;
        this.cards = cards;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("cards", this.cards);

        JSONObject j = new JSONObject();
        j.put("messageType", "DrawDamage");
        j.put("messageBody", body);

        return j;
    }
}
