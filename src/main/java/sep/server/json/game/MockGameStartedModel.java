package sep.server.json.game;

import sep.server.json.AModel;
import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import org.json.JSONArray;

public class MockGameStartedModel extends AModel
{
    public MockGameStartedModel(ClientInstance ci)
    {
        super(ci);
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject b = new JSONObject();
        b.put("gameMap",
                new JSONArray()
                    .put(
                        new JSONArray()
                    )
                    .put(
                        new JSONArray()
                    )
        );

        JSONObject j = new JSONObject();
        j.put("messageType", "GameStarted");
        j.put("messageBody", b);

        return j;
    }

}
