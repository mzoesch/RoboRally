package sep.view.json;

import org.json.JSONObject;

// TODO Rename to IJSONSerializable
public interface IJSONModel
{
    public abstract JSONObject toJSON();
    public abstract void send();
}
