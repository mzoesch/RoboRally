package sep.server.viewmodel;

import sep.server.model.            EServerInformation;
import sep.server.model.            Agent;

import java.io.                     IOException;
import java.util.                   UUID;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/** Implements methods relevant to the server itself. */
public enum EServerInstance
{
    INSTANCE;

    public enum EServerCodes
    {
        OK,
        FATAL,
        ;
    }

    private static final Logger l = LogManager.getLogger(EServerInstance.class);

    private static final int SUFFIX_LENGTH = 4;

    private ServerListener      serverListener;
    private Thread              keepAliveThread;

    private EServerInstance()
    {
        this.serverListener     = null;
        this.keepAliveThread    = null;
        return;
    }

    public static void run() throws IOException
    {
        l.info("Starting server.");

        EServerInformation.INSTANCE.startServer();
        EServerInstance.INSTANCE.keepAlive();
        EServerInstance.INSTANCE.serverListener = new ServerListener();
        EServerInstance.INSTANCE.serverListener.listen(); /* Will block the main thread. */

        return;
    }

    public void kill(final EServerCodes code)
    {
        l.info("Killing server.");

        if (this.keepAliveThread != null)
        {
            this.keepAliveThread.interrupt();
            try
            {
                this.keepAliveThread.join();
            }
            catch (final InterruptedException e)
            {
                l.fatal("Failed to join keep-alive thread.");
            }
            this.keepAliveThread = null;
        }

        /* Can we exit the server differently? So that we can get back to the wrapper? */
        System.exit(code.ordinal());

        return;
    }

    /**
     * Will create a separate thread for itself that will send a keep-alive message
     * to all registered remote clients every five seconds.
     */
    private void keepAlive()
    {
        this.keepAliveThread = EServerInstance.createKeepAliveThread();
        this.keepAliveThread.start();

        return;
    }

    // region Getters and Setters

    public static ServerListener getInstance()
    {
        return EServerInstance.INSTANCE.serverListener;
    }

    public static PlayerController createNewPlayerController(final ClientInstance ci, final Session s)
    {
        return new PlayerController(ci, EServerInstance.createAnonymousPlayerName(), EServerInstance.createCtrlID(), s);
    }

    public static Agent createNewAgent(final Session s)
    {
        return new Agent(EServerInstance.createRandomAgentName(s), EServerInstance.createCtrlID(), s);
    }

    private static int createCtrlID()
    {
        /* TODO Check if already taken. Even though the chance of this happening is basically zero. But just in case :). */
        return Math.abs(UUID.randomUUID().hashCode());
    }

    private static String createAnonymousPlayerName()
    {
        return String.format("Anonymous Player %s", UUID.randomUUID().toString().substring(0, EServerInstance.SUFFIX_LENGTH));
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

    private static Thread createKeepAliveThread()
    {
        return new Thread(() ->
            {
                while (true)
                {
                    try
                    {
                        //noinspection BusyWait
                        Thread.sleep(EServerInformation.KEEP_ALIVE_INTERVAL);
                    }
                    catch (final InterruptedException e)
                    {
                        l.warn("Keep-alive thread interrupted. If this was during shutdown, this is can be ignored.");
                        l.warn(e.getMessage());
                        break;
                    }

                    EServerInformation.INSTANCE.sendKeepAlive();

                    continue;
                }

                return;
            });
    }

    public Thread getKeepAliveThread()
    {
        return this.keepAliveThread;
    }

    // endregion Getters and Setters

}
