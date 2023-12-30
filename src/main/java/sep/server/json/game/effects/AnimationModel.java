package sep.server.json.game.effects;

import sep.server.json.         AModel;
import sep.server.viewmodel.    ClientInstance;
import sep.                     Types;

import org.json.                JSONObject;

public class AnimationModel extends AModel
{
    private final Types.Animation anim;

    public AnimationModel(final ClientInstance ci, final Types.Animation anim)
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
