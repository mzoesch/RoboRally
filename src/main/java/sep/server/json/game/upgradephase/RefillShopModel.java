package sep.server.json.game.upgradephase;

import sep.server.json.         AModel;
import sep.server.viewmodel.    ClientInstance;

import org.json.    JSONArray;
import org.json.    JSONObject;
import java.util.   ArrayList;

public final class RefillShopModel extends AModel
{
    private final ArrayList<String> cards;

    public RefillShopModel(final ClientInstance ci, final ArrayList<String> cards)
    {
        super(ci);

        this.cards = cards;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        final JSONObject j = new JSONObject();
        j.put(  "messageType",  "RefillShop"                                                );
        j.put(  "messageBody",  new JSONObject().put("cards", new JSONArray(this.cards))    );

        return j;
    }

}
