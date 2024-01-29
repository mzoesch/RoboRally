package sep.server.json.game.programmingphase;

import sep.server.json.         AModel;
import sep.server.viewmodel.    ClientInstance;

import org.json.    JSONArray;
import org.json.    JSONObject;

public final class CardsYouGotNowModel extends AModel
{
    private final String[] cards;

    public CardsYouGotNowModel(final ClientInstance ci, final String[] cards)
    {
        super(ci);

        this.cards = cards;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        final JSONObject body = new JSONObject();
        body.put(   "cards",    new JSONArray(this.cards)   );

        final JSONObject j = new JSONObject();
        j.put(  "messageType",      "CardsYouGotNow"        );
        j.put(  "messageBody",      body                    );

        return j;
    }

}
