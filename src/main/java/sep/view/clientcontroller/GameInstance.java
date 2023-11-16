package sep.view.clientcontroller;

import sep.view.json.mainmenu.CreateSessionModel;
import sep.view.json.mainmenu.PostLoginConfirmationModel;
import sep.view.json.mainmenu.JoinSessionModel;

import javafx.application.Platform;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import sep.view.viewcontroller.ViewLauncher;

public class GameInstance
{
    public static final int MAX_PLAYER_NAME_LENGTH = 16;
    public static final int SESSION_ID_LENGTH = 5;

    private GameInstance()
    {
        super();

        if (EClientInformation.INSTANCE.JFX_INSTANCE == null)
        {
            System.out.printf("[CLIENT] Game Instance initialized on main thread (JFX) constructing window.%n");
            ViewLauncher.run();
            return;
        }

        System.out.printf("[CLIENT] Creating Game Instance for thread.%n");

        return;
    }

    public static void run()
    {
        //noinspection InstantiationOfUtilityClass
        new GameInstance();
        return;
    }

    public static void kill()
    {
        GameInstance.handleServerDisconnect();
        Platform.exit();
        return;
    }

    public static boolean connectToNewSession(String playerName) throws IOException
    {
        System.out.printf("[CLIENT] Trying to connect client to new session.%n");

        if (EClientInformation.INSTANCE.establishAServerConnection())
        {
            System.out.printf("[CLIENT] Successfully connected to server.%n");

            CreateSessionModel model = new CreateSessionModel(playerName);
            model.send();

            model.waitForResponse();
            if (model.getResponse() == null)
            {
                return false;
            }

            if (model.isConnectionStateInvalid())
            {
                GameInstance.handleServerDisconnect();
                EClientInformation.INSTANCE.getStdServerErrPipeline().setLength(0);
                EClientInformation.INSTANCE.getStdServerErrPipeline().append(model.getErrorMessage());

                return false;
            }

            EClientInformation.INSTANCE.setConnectedSessionID(model.getSessionID());
            EClientInformation.INSTANCE.setPlayerName(playerName);

            EClientInformation.INSTANCE.listen();

            System.out.printf("[CLIENT] Successfully connected to new session (%s).%n", model.getSessionID());

            return true;
        }

        System.out.printf("[CLIENT] Failed to connect to server.%n");

        return false;
    }

    public static boolean connectToExistingSession(String playerName, String sessionID) throws IOException
    {
        System.out.printf("[CLIENT] Trying to connect client to existing session.%n");

        if (EClientInformation.INSTANCE.establishAServerConnection())
        {
            System.out.printf("[CLIENT] Successfully connected to server.%n");

            JoinSessionModel model = new JoinSessionModel(playerName, sessionID);
            model.send();

            model.waitForResponse();
            if (model.getResponse() == null)
            {
                return false;
            }

            if (model.isConnectionStateInvalid())
            {
                GameInstance.handleServerDisconnect();
                EClientInformation.INSTANCE.getStdServerErrPipeline().setLength(0);
                EClientInformation.INSTANCE.getStdServerErrPipeline().append(model.getErrorMessage());

                return false;
            }

            EClientInformation.INSTANCE.setConnectedSessionID(model.getSessionID());
            EClientInformation.INSTANCE.setPlayerName(playerName);

            EClientInformation.INSTANCE.listen();

            System.out.printf("[CLIENT] Successfully connected to existing session (%s).%n", sessionID);

            return true;
        }

        System.out.printf("[CLIENT] Failed to connect to server.%n");

        return false;
    }

    /** Called after the client loaded into the lobby. */
    public static void connectToSessionPostLogin() throws IOException
    {
        PostLoginConfirmationModel.sendPositive(EClientInformation.INSTANCE.getBufferedWriter());
        return;
    }

    public static void handleServerDisconnect()
    {
        ServerListener.closeSocket(EClientInformation.INSTANCE.getBufferedWriter());
        EClientInformation.INSTANCE.resetServerConnectionAfterDisconnect();
        return;
    }

    public static void sendServerRequest(JSONObject json) throws IOException
    {
        EClientInformation.INSTANCE.sendServerRequest(json);
        return;
    }

    public static JSONObject waitForServerResponse() throws IOException
    {
        String response = EClientInformation.INSTANCE.waitForServerResponse();
        if (response == null)
        {
            return null;
        }

        JSONObject res;
        try
        {
            res = new JSONObject(response);
        }
        catch (JSONException e)
        {
            System.err.printf("[CLIENT] Failed to parse server response.%n");
            System.err.printf("[CLIENT] %s%n", e.getMessage());
            return null;
        }

        return res;
    }

}
