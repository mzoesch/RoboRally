package sep.server.viewmodel;

import java.io.IOException;

public final class ServerInstance
{
    private static ServerListener SERVER_LISTENER;

    private ServerInstance() throws IOException
    {
        super();

        System.out.printf("[SERVER] Starting server.%n");

        ServerInstance.SERVER_LISTENER = new ServerListener();
        ServerInstance.SERVER_LISTENER.listen();

        return;
    }

    public static void run() throws IOException
    {
        //noinspection InstantiationOfUtilityClass
        new ServerInstance();
        return;
    }

    public static PlayerController createNewPlayerController(ClientInstance clientInstance, String playerName, Session session)
    {
        return new PlayerController(clientInstance, playerName, session);
    }

}
