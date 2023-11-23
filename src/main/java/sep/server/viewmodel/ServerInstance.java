package sep.server.viewmodel;

import sep.server.model.EServerInformation;

import java.io.IOException;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Implements methods relevant to the server itself. */
public final class ServerInstance
{
    private static final Logger l = LogManager.getLogger(ServerInstance.class);

    public static ServerInstance INSTANCE;
    private final ServerListener SERVER_LISTENER;

    private ServerInstance() throws IOException
    {
        super();

        l.info("Starting server.");

        ServerInstance.INSTANCE = this;
        ServerInstance.keepAlive();
        this.SERVER_LISTENER = new ServerListener();
        this.SERVER_LISTENER.listen(); /* Will block the main thread. */

        return;
    }

    /**
     * Will create a separate thread for itself that will send a keep-alive message
     * to all registered clients every 5 seconds.
     */
    private static void keepAlive()
    {
        new Thread(() ->
        {
            while (true)
            {
                try
                {
                    //noinspection BusyWait
                    Thread.sleep(EServerInformation.KEEP_ALIVE_INTERVAL);
                }
                catch (InterruptedException e)
                {
                    l.error("Keep-alive thread interrupted.");
                    l.error(e.getMessage());
                    return;
                }

                EServerInformation.INSTANCE.sendKeepAlive();

                continue;
            }
        }).start();

        return;
    }

    public static void run() throws IOException
    {
        new ServerInstance();
        return;
    }

    // region Getters and Setters

    public static ServerListener getInstance()
    {
        return ServerInstance.INSTANCE.SERVER_LISTENER;
    }

    public static PlayerController createNewPlayerController(ClientInstance clientInstance, Session session)
    {
        return new PlayerController(clientInstance, ServerInstance.createAnonymousPlayerName(), ServerInstance.createPlayerID(), session);
    }

    private static int createPlayerID()
    {
        return Math.abs(UUID.randomUUID().hashCode());
    }

    private static String createAnonymousPlayerName()
    {
        int SUFFIX_LENGTH = 4;
        return String.format("Anonymous Player %s", UUID.randomUUID().toString().substring(0, SUFFIX_LENGTH));
    }

    // endregion Getters and Setters

}
