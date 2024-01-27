package sep.server.json.game.upgradephase;

import sep.server.json.         AModel;
import sep.server.viewmodel.    ClientInstance;

import org.json.    JSONObject;
import org.json.    JSONArray;
import java.util.   ArrayList;

public final class ExchangeShopModel extends AModel
{
    private final ArrayList<String> cards;

    public ExchangeShopModel(final ClientInstance ci, final ArrayList<String> cards)
    {
        super(ci);

        this.cards = cards;

        return;
    }

    @Override
    public JSONObject toJSON()
    {
        final JSONObject j = new JSONObject();
        j.put(  "messageType",  "ExchangeShop"                                              );
        j.put(  "messageBody",  new JSONObject().put("cards", new JSONArray(this.cards))    );

        return j;
    }

}
