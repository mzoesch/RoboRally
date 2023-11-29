package sep.server.json.game;

import sep.server.json.AModel;
import sep.server.model.game.Course;
import sep.server.model.game.Tile;
import sep.server.viewmodel.ClientInstance;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;

public class GameStartedModel extends AModel
{
    final ArrayList<ArrayList<Tile>> course;
    public GameStartedModel(ClientInstance ci, ArrayList<ArrayList<Tile>> course)
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
                System.out.println(tileInfo);
                //arrayListY.put(tileInfo);
                //arrayListY.put(i);
                //arrayListY.put(k);

            }
            arrayListX.put(arrayListY);
            //System.out.println(arrayListY);
            arrayListY.clear();
        }
        //System.out.println(arrayListX);
        courseJSON.put(arrayListX);
        body.put("gameMap", courseJSON);
        JSONObject j = new JSONObject();
        j.put("messageType", "GameStarted");
        j.put("messageBody", body);

        return j;
    }

    public static void main(final String[] args){
        Course testCourse = new Course("DizzyHighway");
        GameStartedModel testModel = new GameStartedModel(null, testCourse.getCourse());
        JSONObject testJSON = testModel.toJSON();
        System.out.println(testJSON);
    }
}
