package sep.view.json.game;

import org.json.JSONObject;
import sep.view.json.AServerRequestModel;

public class SelectedCardModel extends AServerRequestModel {

    private final String cardName;
    private final int register;

    public SelectedCardModel(String cardName, int register) {
        this.cardName = cardName;
        this.register = register;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("card", this.cardName);
        body.put("register", this.register);

        JSONObject j = new JSONObject();
        j.put("messageType", "SelectedCard");
        j.put("messageBody", body);

        return j;
    }
}
