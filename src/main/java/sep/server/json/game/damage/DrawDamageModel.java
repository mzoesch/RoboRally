package sep.server.json.game.damage;

import sep.server.json.         AModel;
import sep.server.viewmodel.    ClientInstance;

import org.json.                JSONObject;

public final class DrawDamageModel extends AModel
{
    private final int       playerID;
    private final String[]  cards;

    public DrawDamageModel(final ClientInstance ci, final int playerID, final String[] cards)
    {
        super(ci);

        this.playerID = playerID;
        this.cards = cards;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        final JSONObject body = new JSONObject();
        body.put(   "clientID",     this.playerID   );
        body.put(   "cards",        this.cards      );

        final JSONObject j = new JSONObject();
        j.put(  "messageType",  "DrawDamage"    );
        j.put(  "messageBody",  body            );

        return j;
    }

}
