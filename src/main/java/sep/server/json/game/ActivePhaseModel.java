package sep.server.json.game;

import org.json.JSONObject;
import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

public class ActivePhaseModel extends AModel{

        private final int phase;

        public ActivePhaseModel(ClientInstance ci, int phase) {
            super(ci);
            this.phase = phase;
        }

        @Override
        public JSONObject toJSON() {
            JSONObject body = new JSONObject();
            body.put("phase", this.phase);

            JSONObject j = new JSONObject();
            j.put("messageType", "ActivePhase");
            j.put("messageBody", body);

            return j;
        }
}


