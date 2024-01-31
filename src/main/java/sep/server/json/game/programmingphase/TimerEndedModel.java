package sep.server.json.game.programmingphase;

import sep.server.json.         AModel;
import sep.server.viewmodel.    ClientInstance;

import org.json.        JSONArray;
import org.json.        JSONObject;

public final class TimerEndedModel extends AModel
{
    private final int[] playerID;

    public TimerEndedModel(final ClientInstance ci, final int[] playerID)
    {
        super(ci);

        this.playerID = playerID;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        final JSONObject body = new JSONObject();
        body.put(   "clientIDs",    new JSONArray(this.playerID)    );

        final JSONObject j = new JSONObject();
        j.put(      "messageType",  "TimerEnded"                    );
        j.put(      "messageBody",  body                            );

        return j;
    }

}
