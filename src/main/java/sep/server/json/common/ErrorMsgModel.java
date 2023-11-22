package sep.server.json.common;

import org.json.JSONObject;
import sep.server.json.IJSONModel;
import sep.server.viewmodel.ClientInstance;

import java.io.IOException;

public class ErrorMsgModel implements IJSONModel {
    private final String msg;
    private final ClientInstance ci;

    public ErrorMsgModel(ClientInstance ci, String msg) {
        this.ci = ci;
        this.msg = msg;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject body = new JSONObject();
        body.put("error", this.msg);

        JSONObject j = new JSONObject();
        j.put("messageType", "Error");
        j.put("messageBody", body);

        return j;
    }

    @Override
    public void send()
    {
//        System.out.printf(String.format("%s%n", this.toJSON().toString(4)));

        try
        {
            ci.getBufferedWriter().write(this.toJSON().toString());
            ci.getBufferedWriter().newLine();
            ci.getBufferedWriter().flush();
        }
        catch (IOException e)
        {
            System.err.printf("[SERVER] Failed to send response to client%n");
            System.err.printf("[SERVER] %s%n", e.getMessage());
            return;
        }

        return;
    }
}
