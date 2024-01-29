package sep.server.json.game.programmingphase;

import sep.server.json.         AModel;
import sep.server.viewmodel.    ClientInstance;

import org.json.    JSONObject;

public final class TimerStartedModel extends AModel
{
    public TimerStartedModel(final ClientInstance ci)
    {
        super(ci);
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        final JSONObject j = new JSONObject();
        j.put("messageType", "TimerStarted");
        return j;
    }

}
