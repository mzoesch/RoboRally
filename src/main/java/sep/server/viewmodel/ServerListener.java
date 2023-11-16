package sep.server.viewmodel;

import sep.server.model.EServerInformation;

import java.io.IOException;
import java.net.Socket;

public final class ServerListener
{
    public ServerListener()
    {
        super();
        return;
    }

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
