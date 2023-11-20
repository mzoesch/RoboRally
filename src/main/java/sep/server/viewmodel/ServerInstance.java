package sep.server.viewmodel;

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

        this.SERVER_LISTENER = new ServerListener();
        this.SERVER_LISTENER.listen();

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
