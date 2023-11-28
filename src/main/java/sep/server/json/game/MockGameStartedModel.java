package sep.server.json.game;

import sep.server.json.AModel;
import sep.server.model.game.Tile;
import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;

public class MockGameStartedModel extends AModel
{
    final ArrayList<ArrayList<Tile>> course;
    public MockGameStartedModel(ClientInstance ci, ArrayList<ArrayList<Tile>> course)
    {
        super(ci);
        this.course = course;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        //TODO JSON-Object-Erstellung
        JSONObject body = new JSONObject();
        JSONArray courseJSON = new JSONArray();
        JSONArray arrayListX = new JSONArray();
        JSONArray arrayListY = new JSONArray();

        for(int i = 0; i < course.size(); i++){

            for(int k = 0; k < course.get(i).size(); k++){
                JSONArray tileInfo = course.get(i).get(k).toJSON();
                arrayListY.put(tileInfo);
            }
            courseJSON.put(arrayListX);
        }

        body.put("gameMap", courseJSON);
        JSONObject j = new JSONObject();
        j.put("messageType", "GameStarted");
        j.put("messageBody", body);

        return j;
    }
}
