package sep.view.json.game;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

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
