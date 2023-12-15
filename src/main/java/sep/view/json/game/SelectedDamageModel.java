package sep.view.json.game;

import org.json.JSONArray;
import org.json.JSONObject;
import sep.view.json.AServerRequestModel;

import java.util.ArrayList;

public class SelectedDamageModel extends AServerRequestModel {
    private final ArrayList<String> selectedDamageCards;

    public SelectedDamageModel(ArrayList<String> selectedDamageCards){
        this.selectedDamageCards = selectedDamageCards;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONArray cards = new JSONArray();
        for(String s : selectedDamageCards){
            cards.put(s);
        }
        JSONObject body = new JSONObject();
        body.put("cards", cards);

        JSONObject j = new JSONObject();
        j.put("messageType", "SelectedDamage");
        j.put("messageBody", body);

        return j;
    }

}
