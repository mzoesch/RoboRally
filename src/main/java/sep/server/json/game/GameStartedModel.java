package sep.server.json.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sep.server.json.AModel;
import sep.server.model.game.Course;
import sep.server.model.game.GameState;
import sep.server.model.game.Tile;
import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;

public class GameStartedModel extends AModel
{
    private static final Logger l = LogManager.getLogger(GameState.class);
    ArrayList<ArrayList<Tile>> course;
    public GameStartedModel(ClientInstance ci, ArrayList<ArrayList<Tile>> course)
    {
        super(ci);
        this.course = course;
        return;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        JSONObject courseJson = new JSONObject();
        JSONArray arrayListX = new JSONArray();
        JSONArray arrayListY = new JSONArray();

        for(int i = 0; i < course.size(); i++){

            for(int k = 0; k < course.get(i).size(); k++){
                JSONArray tileInfo = course.get(i).get(k).toJSON();
                arrayListY.put(tileInfo);

            }
            arrayListX.put(arrayListY);
;
            arrayListY = new JSONArray();
        }
        body.put("gameMap", arrayListX);
        courseJson.put("messageType", "GameStarted");
        courseJson.put("messageBody", body);
        l.debug("Game mode successfully finished building the course.");
        l.trace(courseJson);
        return courseJson;
    }
}
