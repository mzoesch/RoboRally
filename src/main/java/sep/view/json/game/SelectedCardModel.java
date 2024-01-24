package sep.view.json.game;

import sep.view.json.AServerRequestModel;

import org.json.JSONObject;

/**
 * Model if the client has set one of their cards in their deck to one of the register slots.
 */
public final class SelectedCardModel extends AServerRequestModel
{
    private final String cardName;
    private final int register;

    /**
     * @param register The register slot to put the card in.
     * @param cardName The name of the card to put in the register slot (Null if the register slot is to be cleared).
     */
    public SelectedCardModel(int register, String cardName)
    {
        this.cardName = cardName;
        this.register = register;
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject body = new JSONObject();
        body.put("register", this.register + 1); /* The registers in the protocol are not zero-based. */
        body.put("card", this.cardName == null ? JSONObject.NULL : this.cardName);

        JSONObject j = new JSONObject();
        j.put("messageType", "SelectedCard");
        j.put("messageBody", body);

        return j;
    }

}
