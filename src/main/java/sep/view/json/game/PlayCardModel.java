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
        JSONObject body = new JSONObject();
        body.put("card", this.cardName);

        JSONObject j = new JSONObject();
        j.put("messageType", "PlayCard");
        j.put("messsageBody", body);

        return j;
    }
}
