package sep.view.json.mainmenu;

import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.IOException;

public class PostLoginConfirmationModel
{
    public static void sendPositive(BufferedWriter bufferedWriter) throws IOException
    {
        JSONObject response = new JSONObject();
        response.put("connectionMethod", "postLoginConfirmation");

        bufferedWriter.write(response.toString());
        bufferedWriter.newLine();
        bufferedWriter.flush();

        return;
    }
}
