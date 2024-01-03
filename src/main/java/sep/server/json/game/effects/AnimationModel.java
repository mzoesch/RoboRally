package sep.server.json.game.effects;

import sep.server.model.game.   EAnimation;
import sep.server.json.         AModel;
import sep.server.viewmodel.    ClientInstance;

import org.json.                JSONObject;

public final class AnimationModel extends AModel
{
    private final EAnimation anim;

    public AnimationModel(final ClientInstance ci, final EAnimation anim)
    {
        super(ci);
        this.anim = anim;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        final JSONObject j = new JSONObject();
        j.put(  "messageType",  "Animation"                                         );
        j.put(  "messageBody",  new JSONObject().put("type", this.anim.toString())  );

        return j;
    }

}
