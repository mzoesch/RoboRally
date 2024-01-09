package sep.server.json.game.upgradephase;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class UpgradeBoughtModel extends AModel {

    private final int clientID;
    private final String card;

    public UpgradeBoughtModel(ClientInstance ci, int clientID, String card) {
        super(ci);
        this.clientID = clientID;
        this.card = card;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.clientID);
        body.put("card", this.card);

        JSONObject j = new JSONObject();
        j.put("messageType", "UpgradeBought");
        j.put("messageBody", body);

        return j;
    }
}
