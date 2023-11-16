package sep.view.json;

import org.json.JSONObject;

public interface IJSONModel
{
    public abstract JSONObject toJSON();
    public abstract void send();
}
