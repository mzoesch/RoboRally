package sep.server.json.game.programmingphase;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class NotYourCardsModel extends AModel {

    private final int playerID;
    private final int numberOfCards;

    public NotYourCardsModel(ClientInstance ci, int playerID, int numberOfCards) {
        super(ci);
        this.playerID = playerID;
        this.numberOfCards = numberOfCards;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("cardsInHand", this.numberOfCards);

        JSONObject j = new JSONObject();
        j.put("messageType", "NotYourCards");
        j.put("messageBody", body);

        return j;
    }
}
