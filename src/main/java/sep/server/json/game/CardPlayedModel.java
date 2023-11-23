package sep.server.json.game;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class CardPlayedModel extends AModel {

    private final int playerID;
    private final String cardName;

    public CardPlayedModel(ClientInstance ci, int playerID, String cardName) {
        super(ci);
        this.playerID = playerID;
        this.cardName = cardName;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("card", this.cardName);

        JSONObject j = new JSONObject();
        j.put("messageType", "CardPlayed");
        j.put("messageBody", body);

        return j;
    }
}
