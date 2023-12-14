package sep.view.json.game;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

/**
 * Model for the selection of the RebootDirection from the Client
 */
public final class RebootDirectionModel extends AServerRequestModel
{
    private final String direction;

    /**
     * @param direction The direction in which the roboter shall be rebooted
     */
    public RebootDirectionModel(String direction)
    {
        this.direction = direction;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("direction", this.direction);

        JSONObject j = new JSONObject();
        j.put("messageType", "RebootDirection");
        j.put("messageBody", body);

        return j;
    }

}
