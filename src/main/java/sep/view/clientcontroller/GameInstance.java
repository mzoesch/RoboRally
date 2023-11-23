package sep.view.clientcontroller;

import sep.view.json.mainmenu.InitialClientConnectionModel;

import javafx.application.Platform;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONException;
import sep.view.viewcontroller.ViewLauncher;

/**
 * High-level manager object for an instance of the running game. Spawned at game creation and not destroyed until
 * the game is killed. It implements important methods that affect the game as a whole, such as connecting
 * to the server or shutting down.
 */
public class GameInstance
{
    public static final int MAX_PLAYER_NAME_LENGTH = 16;

    private GameInstance()
    {
        super();

        if (EClientInformation.INSTANCE.getJFXInstance() == null)
        {
            System.out.printf("[CLIENT] Game Instance initialized on main thread (JFX) constructing window.%n");
            EClientInformation.INSTANCE.setJFXInstance(this);
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
        if (sep.EArgs.getMode() == sep.EArgs.DEFAULT && EClientInformation.INSTANCE.getWrap())
        {
            sep.EArgs.setMode(sep.EArgs.EXIT);
        }
        Platform.exit();
        return;
    }

    public static boolean connectToServer() throws IOException
    {
        if (EClientInformation.INSTANCE.hasServerConnection())
        {
            System.out.printf("[CLIENT] Client is already connected to a server.%n");
            return false;
        }

        return EClientInformation.INSTANCE.establishAServerConnection();
    }

    /** Called after the client loaded the lobby screen. */
    public static boolean connectToSessionPostLogin() throws IOException, JSONException
    {
        if (!EClientInformation.INSTANCE.hasServerConnection())
        {
            System.out.printf("[CLIENT] Client is not connected to a server.%n");
            return false;
        }

        JSONObject serverProtocolVersion = GameInstance.waitForServerResponse();
        if (serverProtocolVersion == null)
        {
            return false;
        }

        boolean bOk = InitialClientConnectionModel.checkServerProtocolVersion(serverProtocolVersion);
        if (!bOk)
        {
            System.out.printf("[CLIENT] Server protocol version mismatch.%n");
            return false;
        }

        InitialClientConnectionModel.sendProtocolVersionConfirmation();
        System.out.printf("[CLIENT] Server protocol version confirmed.%n");

        JSONObject welcome = GameInstance.waitForServerResponse();
        if (welcome == null)
        {
            return false;
        }
        bOk = InitialClientConnectionModel.checkPlayerID(welcome);
        if (!bOk)
        {
            System.out.printf("[CLIENT] Failed to retrieve player ID.%n");
            return false;
        }
        System.out.printf("[CLIENT] Player ID received (%d).%n", EClientInformation.INSTANCE.getPlayerID());

        EClientInformation.INSTANCE.listen();

        return true;
    }

    public static void handleServerDisconnect()
    {
        ServerListener.closeSocket(EClientInformation.INSTANCE.getBufferedWriter());
        EClientInformation.INSTANCE.resetServerConnectionAfterDisconnect();
        return;
    }

    public static void respondToKeepAlive() throws IOException
    {
        GameInstance.sendServerRequest(new JSONObject().put("messageType", "Alive"));
        return;
    }

    public static void sendServerRequest(JSONObject json) throws IOException
    {
        EClientInformation.INSTANCE.sendServerRequest(json);
        return;
    }

    /**
     * Will block the calling thread until a response from the server is received. Only use this method for the
     * initial connection to the server.
     *
     * @see EClientInformation#waitForServerResponse()
     */
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

    /** Called when wrapping. */
    public static void loadInnerClient()
    {
        System.out.printf("[WRAPPER] Launching client.%n");
        ViewLauncher.loadInnerClient();
        return;
    }

}
