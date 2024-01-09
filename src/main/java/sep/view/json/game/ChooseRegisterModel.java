package sep.view.json.game;

import org.json.JSONObject;
import sep.view.json.AServerRequestModel;

public class ChooseRegisterModel extends AServerRequestModel {

    private final int register;

    public ChooseRegisterModel(int register) {
        this.register = register;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject j = new JSONObject();
        j.put("messageTpye", "ChooseRegister");
        j.put("messageBody", new JSONObject().put("register", this.register));

        return j;
    }
}
