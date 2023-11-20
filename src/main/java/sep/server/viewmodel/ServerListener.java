package sep.server.viewmodel;

import sep.server.model.EServerInformation;

import java.io.IOException;
import java.net.Socket;

/**
 * We create a special object for listening to the server socket. This object is instantiated on the main thread.
 * Therefore, it will always block it. If the {@link #listen()} method terminates, the application will terminate.
 */
public final class ServerListener
{
    public ServerListener()
    {
        super();
        return;
    }

    /** Accepts clients. */
    public void listen() throws IOException
    {
        //noinspection InfiniteLoopStatement
        while (true)
        {
            System.out.printf("[SERVER] Listening on port %d for client connection . . .%n", EServerInformation.PORT);
            Socket client = EServerInformation.INSTANCE.getServerSocket().accept();
            System.out.printf("[SERVER] Accepted connection from %s.%n", client.getInetAddress().getHostAddress());

            ClientInstance clientInstance = new ClientInstance(client);
            EServerInformation.INSTANCE.getExecutorService().execute(clientInstance);

            continue;
        }
    }

}
