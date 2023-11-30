package sep.server.json.game.activatingphase;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class ReplaceCardModel extends AModel {

    private final int register;
    private final int playerID;
    private final String newCard;

    public ReplaceCardModel(ClientInstance ci, int register, int playerID, String newCard) {
        super(ci);
        this.register = register;
        this.playerID = playerID;
        this.newCard = newCard;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("register", this.register);
        body.put("newCard", this.newCard);
        body.put("clientID", this.playerID);

        JSONObject j = new JSONObject();
        j.put("messageType", "ReplaceCard");
        j.put("messageBody", body);

        return j;
    }
}
