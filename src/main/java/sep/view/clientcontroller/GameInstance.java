package sep.view.clientcontroller;

import sep.view.json.mainmenu.InitialClientConnectionModel;

import javafx.application.Platform;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONException;
import sep.view.viewcontroller.ViewSupervisor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * High-level manager object for an instance of the running game. Spawned at game creation and not destroyed until
 * the game is killed. It implements important methods that affect the game as a whole, such as connecting
 * to the server or shutting down.
 */
public final class GameInstance
{
    private static final Logger l = LogManager.getLogger(GameInstance.class);

    public static final int MAX_PLAYER_NAME_LENGTH      = 16;
    private static final int MAX_CONNECTION_TRIES       = 3;

    private GameInstance()
    {
        super();

        if (EClientInformation.INSTANCE.getJFXInstance() == null)
        {
            EClientInformation.INSTANCE.setJFXInstance(this);

            if (EClientInformation.INSTANCE.isAgent())
            {
                l.info("Creating Game Instance for main thread (agent). Connecting to server.");
                GameInstance.runAgentLoop();
                return;
            }

            l.info("Creating Game Instance for main thread (JFX), constructing window.");
            ViewSupervisor.run();
            return;
        }

        l.info("Creating Game Instance for thread.");

        return;
    }

    private static void runAgentLoop()
    {
        int tries = 0;
        while (tries < GameInstance.MAX_CONNECTION_TRIES)
        {
            try
            {
                if (GameInstance.connectToServer())
                {
                    l.info("Agent successfully connected to server.");
                    break;
                }

                l.error("Some unknown error occurred while connecting to server.");

                if (tries > GameInstance.MAX_CONNECTION_TRIES - 2)
                {
                    break;
                }

                l.info("Retrying in 5 seconds. Failed {} times. Quitting after {} tries.", tries + 1, GameInstance.MAX_CONNECTION_TRIES);
                try
                {
                    Thread.sleep(5_000);
                }
                catch (final InterruptedException e)
                {
                    l.error("Interrupted while waiting to retry.");
                    l.error(e.getMessage());
                    return;
                }

                tries++;

                continue;
            }
            catch (final IOException e)
            {
                l.error("Interrupted while waiting to retry.");
                l.error(e.getMessage());
                l.info("Retrying in 5 seconds. Failed {} times. Quitting after {} tries.", tries + 1, GameInstance.MAX_CONNECTION_TRIES);

                try
                {
                    Thread.sleep(5_000);
                }
                catch (final InterruptedException e1)
                {
                    l.error("Failed to sleep.");
                    l.error(e1.getMessage());
                    return;
                }

                tries++;

                continue;
            }
        }

        if (!EClientInformation.INSTANCE.hasServerConnection())
        {
            l.fatal("Failed to connect to server.");
            l.fatal("Requesting shutdown.");
            GameInstance.kill();
            return;
        }

        try
        {
            if (!GameInstance.connectToSessionPostLogin())
            {
                l.fatal("Failed to connect to session.");
                l.fatal("Requesting shutdown.");
                GameInstance.kill();
                return;
            }
        }
        catch (final IOException | JSONException e)
        {
            l.fatal("Failed to connect to session.");
            l.fatal("Requesting shutdown.");
            GameInstance.kill();
            return;
        }

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
        l.info("Game Instance killed.");
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

    public static boolean connectToSessionPostLogin() throws IOException, JSONException
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
            l.fatal("Server protocol version mismatch.");
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
            l.fatal("Failed to retrieve player ID.");
            return false;
        }
        l.debug("Player ID received.");

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
            l.error("Failed to parse server response.");
            l.error(e.getMessage());
            return null;
        }

        return res;
    }

}
