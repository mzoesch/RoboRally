package sep.view.clientcontroller;

import sep.view.json.mainmenu.      InitialClientConnectionModel;
import sep.view.lib.                OutErr;
import sep.                         Types;

import javafx.application.          Platform;
import java.io.                     IOException;
import org.json.                    JSONObject;
import org.json.                    JSONException;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/**
 * High-level manager object for an instance of the running game. Spawned at game creation and not destroyed until
 * the game is killed. It implements important methods that affect the game as a whole, such as connecting
 * to the server or shutting down.
 */
public abstract sealed class GameInstance permits GI_Agent, GI_Human
{
    private static final Logger l = LogManager.getLogger(GameInstance.class);

    public static final int MAX_PLAYER_NAME_LENGTH = 16;

    public static final int     EXIT_OK         = 0;
    public static final int     EXIT_FATAL      = 1;

    protected GameInstance()
    {
        super();

        if (EClientInformation.INSTANCE.getJFXInstance() == null)
        {
            EClientInformation.INSTANCE.setJFXInstance(this);
            return;
        }

        l.fatal("Creating Game Instance for thread even though one already exists.");
        GameInstance.kill();

        return;
    }

    public abstract void run();

    public static void kill()
    {
        GameInstance.kill(GameInstance.EXIT_OK);
        return;
    }

    public static void kill(final int exitCode)
    {
        GameInstance.handleServerDisconnect();
        l.info("Game Instance killed.");

        if (EClientInformation.INSTANCE.isAgent())
        {
            l.info("Shutting down agent.");
            System.exit(exitCode);
            return;
        }

        EClientInformation.INSTANCE.setExitCode(exitCode);
        Platform.exit();

        return;
    }

    public static boolean connectToServer() throws IOException
    {
        if (EClientInformation.INSTANCE.hasServerConnection())
        {
            l.error("Client is already connected to a server.");
            return false;
        }

        return EClientInformation.INSTANCE.establishAServerConnection();
    }

    public static boolean connectToSessionPostLogin(final OutErr outErr) throws IOException, JSONException
    {
        if (!EClientInformation.INSTANCE.hasServerConnection())
        {
            l.error("Client is not connected to a server.");
            return false;
        }

        final JSONObject serverProtocolVersion = GameInstance.waitForServerResponse();
        if (serverProtocolVersion == null)
        {
            return false;
        }

        boolean bOk = InitialClientConnectionModel.checkServerProtocolVersion(serverProtocolVersion);
        if (!bOk)
        {
            l.error("Server protocol version mismatch. Excepted Version {} but found {}.", Types.EProps.VERSION.toString(), InitialClientConnectionModel.getServerProtocolVersion());
            outErr.set(String.format("Server protocol version mismatch. Expected Version %s but found %s.", Types.EProps.VERSION.toString(), InitialClientConnectionModel.getServerProtocolVersion()));
            return false;
        }

        InitialClientConnectionModel.sendProtocolVersionConfirmation();
        l.debug("Server protocol version confirmed.");

        final JSONObject welcome = GameInstance.waitForServerResponse();
        if (welcome == null)
        {
            return false;
        }
        bOk = InitialClientConnectionModel.checkPlayerID(welcome);
        if (!bOk)
        {
            if (InitialClientConnectionModel.isError(welcome))
            {
                l.error("Server sent an error message. Message: {}", InitialClientConnectionModel.getErrorMessage(welcome));

                if (outErr != null)
                {
                    outErr.set(InitialClientConnectionModel.getErrorMessage(welcome));
                }

                return false;
            }

            l.fatal("Failed to retrieve player ID.");

            return false;
        }
        l.debug("Player ID [{}] received.", EClientInformation.INSTANCE.getPlayerID());

        l.info("Client successfully connected to server in session {}.", EClientInformation.INSTANCE.getPreferredSessionID());

        l.info("Starting server listener ({}).", EClientInformation.INSTANCE.isAgent() ? "Agent mode" : "JFX mode");
        EClientInformation.INSTANCE.listen(EClientInformation.INSTANCE.isAgent());

        return true;
    }

    public static void handleServerDisconnect()
    {
        ServerListener.closeSocket();
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
     * Will block the calling thread until a response from the server is received.
     * Only used for the initial client connection.
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
            l.error("Failed to parse server response.");
            l.error(e.getMessage());
            return null;
        }

        return res;
    }

    public static GameInstance createGameInstance()
    {
        return EClientInformation.INSTANCE.isAgent() ? new GI_Agent() : new GI_Human();
    }

}
