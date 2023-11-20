package sep.server.viewmodel;

import sep.server.model.EServerInformation;

import java.io.IOException;

/** Implements methods relevant to the server itself. */
public final class ServerInstance
{
    public static ServerInstance INSTANCE;
    private final ServerListener SERVER_LISTENER;

    private ServerInstance() throws IOException
    {
        super();

        System.out.printf("[SERVER] Starting server.%n");

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
                    System.err.printf("[SERVER] Keep-alive thread interrupted.%n");
                    System.err.printf("[SERVER] %s%n", e.getMessage());
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

    public static ServerListener getInstance()
    {
        return ServerInstance.INSTANCE.SERVER_LISTENER;
    }

    public static PlayerController createNewPlayerController(ClientInstance clientInstance, String playerName, Session session)
    {
        return new PlayerController(clientInstance, playerName, session);
    }

}
