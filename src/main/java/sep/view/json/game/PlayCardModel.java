package sep.view.json.game;

import org.json.JSONObject;
import sep.view.json.AServerRequestModel;

public class PlayCardModel extends AServerRequestModel {

    private final String cardName;

    public PlayCardModel(String cardName) {
        this.cardName = cardName;
    }


    @Override
    public JSONObject toJSON() {

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayCard");
        j.put("messageBody", new JSONObject().put("card", this.cardName));

        return j;
    }
}
