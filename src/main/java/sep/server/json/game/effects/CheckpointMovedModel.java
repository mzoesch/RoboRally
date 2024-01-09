package sep.server.json.game.effects;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class CheckpointMovedModel extends AModel {

    private final int checkpoints;
    private  final int cx;
    private final int cy;

    public CheckpointMovedModel(ClientInstance ci, int checkpoints, int cx, int cy) {
        super(ci);
        this.checkpoints = checkpoints;
        this.cx = cx;
        this.cy = cy;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("checkpointID", this.checkpoints);
        body.put("x", this.cx);
        body.put("y", this.cy);

        JSONObject j = new JSONObject();
        j.put("messageType", "CheckpointMoved");
        j.put("messageBody", body);

        return j;
    }
}
