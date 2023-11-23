package sep.server.json.game.programmingphase;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class CardSelectedModel extends AModel {

    private final int playerID;
    private final int register;
    private final boolean inRegister;

    public CardSelectedModel(ClientInstance ci, int playerID, int register, boolean inRegister) {
        super(ci);
        this.playerID = playerID;
        this.register = register;
        this.inRegister = inRegister;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.playerID);
        body.put("register", this.register);
        body.put("filled", this.inRegister);

        JSONObject j = new JSONObject();
        j.put("messageType", "CardSelected");
        j.put("messageBody", body);

        return j;
    }
}
