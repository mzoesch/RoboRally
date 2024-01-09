package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class RegisterChosenModel extends AModel {

    private final int clinetID;

    private final int register;

    public RegisterChosenModel(ClientInstance ci, int clinetID, int register) {
        super(ci);
        this.clinetID = clinetID;
        this.register = register;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("clientID", this.clinetID);
        body.put("register", this.register);

        JSONObject j = new JSONObject();
        j.put("messageType", "RegisterChosen");
        j.put("messageBody", body);

        return j;
    }
}
