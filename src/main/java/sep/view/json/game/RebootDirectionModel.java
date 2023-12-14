package sep.view.json.game;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

/**
 * Model if the client has set one of their cards in their deck to one of the register slots.
 */
public final class RebootDirectionModel extends AServerRequestModel
{
    private final String direction;

    /**
     * @param register The register slot to put the card in.
     * @param cardName The name of the card to put in the register slot (Null if the register slot is to be cleared).
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
