package sep.view.json.game;

import org.json.JSONObject;
import sep.view.json.AServerRequestModel;

public class BuyUpgradeModel extends AServerRequestModel {

    private final boolean buy;
    private final String card;

    public BuyUpgradeModel(boolean buy, String card) {

        this.buy = buy;
        this.card = card;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("isBuying", this.buy);
        body.put("card", this.card);

        JSONObject j = new JSONObject();
        j.put("messageType", "BuyUpgrade");
        j.put("messageBody", body);

        return j;
    }
}
