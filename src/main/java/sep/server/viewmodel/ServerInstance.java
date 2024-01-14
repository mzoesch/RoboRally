package sep.server.viewmodel;

import sep.server.model.            EServerInformation;
import sep.server.model.            Agent;

import java.io.                     IOException;
import java.util.                   UUID;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/** Implements methods relevant to the server itself. */
public enum ServerInstance
{
    private static final Logger l = LogManager.getLogger(ServerInstance.class);

    private static final int SUFFIX_LENGTH = 4;

    public static ServerInstance INSTANCE;
    private final ServerListener SERVER_LISTENER;

    private ServerInstance()
    {
        this.serverListener     = null;
        this.keepAliveThread    = null;
        return;
    }

    public static void run() throws IOException
    {
        l.info("Starting server.");

        EServerInformation.INSTANCE.startServer();
        ServerInstance.keepAlive();
        this.SERVER_LISTENER = new ServerListener();
        this.SERVER_LISTENER.listen(); /* Will block the main thread. */
        ServerInstance.INSTANCE.keepAlive();
        ServerInstance.INSTANCE.serverListener = new ServerListener();
        ServerInstance.INSTANCE.serverListener.listen(); /* Will block the main thread. */

        return;
    }


        return;
    }

    /**
     * Will create a separate thread for itself that will send a keep-alive message
     * to all registered remote clients every five seconds.
     */
    private static void keepAlive()
    {
        new Thread(
        () ->
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
        })
        .start();

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

    public static PlayerController createNewPlayerController(final ClientInstance ci, final Session s)
    {
        return new PlayerController(ci, ServerInstance.createAnonymousPlayerName(), ServerInstance.createCtrlID(), s);
    }

    public static Agent createNewAgent(final Session s)
    {
        return new Agent(ServerInstance.createRandomAgentName(s), ServerInstance.createCtrlID(), s);
    }

    private static int createCtrlID()
    {
        /* TODO Check if already taken. Even though the chance of this happening is basically zero. But just in case :). */
        return Math.abs(UUID.randomUUID().hashCode());
    }

    private static String createAnonymousPlayerName()
    {
        return String.format("Anonymous Player %s", UUID.randomUUID().toString().substring(0, ServerInstance.SUFFIX_LENGTH));
    }

    private static String createRandomAgentName(final Session s)
    {
        final int rIdx = (int) (Math.random() * Agent.AGENT_NAMES.length);
        int idx = (rIdx + 1) % Agent.AGENT_NAMES.length;
        while (true)
        {
            if (idx == rIdx)
            {
                l.warn("Whoops, something weird happened. There are no more agent names available. This should never have happened.");
                break;
            }

            if (s.isAgentNameTaken(Agent.AGENT_NAMES[idx]))
            {
                idx = (idx + 1) % Agent.AGENT_NAMES.length;
                continue;
            }

            break;
        }

        return String.format("%s %s", Agent.AGENT_PREFIX, Agent.AGENT_NAMES[idx]);
    }

    public static String createRandomAgentName()
    {
        return String.format("%s %s", Agent.AGENT_PREFIX, Agent.AGENT_NAMES[(int) (Math.random() * Agent.AGENT_NAMES.length)]);
    }

    // endregion Getters and Setters

}
