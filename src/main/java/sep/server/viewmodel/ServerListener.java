package sep.server.viewmodel;

import sep.server.model.EServerInformation;

import java.io.IOException;
import java.net.Socket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * We create a special object for listening to the server socket. This object is instantiated on the main thread.
 * Therefore, it will always block it. If the {@link #listen()} method terminates, the application will terminate.
 */
public final class ServerListener
{
    private static final Logger l = LogManager.getLogger(ServerListener.class);

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
            l.debug("Listening on port {} for client connection . . .", EServerInformation.PORT);
            Socket client = EServerInformation.INSTANCE.getServerSocket().accept();
            l.debug("Accepted connection from {}.", client.getInetAddress().getHostAddress());

            // TODO This is a little bit weird, the Client Instance should extend Thread not implement it. Fix.
            ClientInstance ci = new ClientInstance(client);
            Thread t = new Thread(ci);
            ci.thread = t;
            t.start();

            continue;
        }
    }

}
