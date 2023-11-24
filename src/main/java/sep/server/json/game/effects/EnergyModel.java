package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class EnergyModel extends AModel {

    private final int playerID;
    private final int count;
    private final String source;

    public EnergyModel(ClientInstance ci, int playerID, int count, String source) {
        super(ci);
        this.playerID = playerID;
        this.count = count;
        this.source = source;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("count", this.count);
        body.put("source", this.source);

        JSONObject j = new JSONObject();
        j.put("messageType", "Energy");
        j.put("messageBody", body);

        return j;
    }
}
