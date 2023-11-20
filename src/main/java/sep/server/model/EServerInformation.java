package sep.server.model;

import sep.server.viewmodel.Session;
import sep.server.viewmodel.ClientInstance;

import java.util.concurrent.Executors;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 * The EServerInformation enum is a singleton class that represents the server itself. The EServerInstance is spawned
 * at application startup by the Server Listener and is not destroyed until the application is killed. The
 * EServerInstance can be created only once and is shared across all threads. It implements high-level logic for the
 * server application, for example, connecting clients to existing session or creating new ones.
 */
public enum EServerInformation
{
    INSTANCE;

    // TODO Move to env var
    public static final int PORT = 8080;
    public static final String PROTOCOL_VERSION = "0.1";

    private static final int MAX_CLIENTS = 32;
    public static final int KEEP_ALIVE_INTERVAL = 5_000;

    private final ServerSocket serverSocket;
    /** @deprecated */
    private final ExecutorService executorService;

    private final ArrayList<Session> sessions;

    private EServerInformation()
    {
        ServerSocket tServerSocket;
        try
        {
            tServerSocket = new ServerSocket(EServerInformation.PORT);
        }
        catch (IOException e)
        {
            tServerSocket = null;
            System.err.printf("[SERVER] Server socket failed in process.%n");
            System.err.printf("%s%n", e.getMessage());
            System.exit(1);
        }
        this.serverSocket = tServerSocket;

        this.executorService = Executors.newFixedThreadPool(EServerInformation.MAX_CLIENTS);

        this.sessions = new ArrayList<Session>();

        return;
    }

    public String createNewSession()
    {
        Session s = new Session();
        this.sessions.add(s);
        return s.getSessionID();
    }

    public void removeSession(Session session)
    {
        this.sessions.remove(session);
        System.out.printf("[SERVER] Session %s closed.%n", session.getSessionID());
        return;
    }

    public void sendKeepAlive()
    {
        ArrayList<ClientInstance> dead = new ArrayList<ClientInstance>();

        for (Session s : this.sessions)
        {
            s.sendKeepAlive(dead);
            continue;
        }

        System.out.printf("[SERVER] Sent keep-alive to all clients.%n");

        if (!dead.isEmpty())
        {
            System.out.printf("[SERVER] Removing %d dead client%s.%n", dead.size(), dead.size() == 1 ? "" : "s");
            for (ClientInstance ci : dead)
            {
                ci.handleDisconnect();
                continue;
            }

            return;
        }

        return;
    }

    public ServerSocket getServerSocket()
    {
        return serverSocket;
    }

    public Session[] getSessions()
    {
        return this.sessions.toArray(new Session[0]);
    }

    public Session getSessionByID(String sessionID)
    {
        for (Session s : this.sessions)
        {
            if (s.getSessionID().equals(sessionID))
            {
                return s;
            }

            continue;
        }

        return null;
    }

    public boolean isSessionIDValid(String sessionID)
    {
        for (Session s : this.sessions)
        {
            if (s.getSessionID().equals(sessionID))
            {
                return true;
            }

            continue;
        }

        return false;
    }

}
