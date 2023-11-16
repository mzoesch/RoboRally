package sep.server.model;

import sep.server.viewmodel.Session;

import java.util.concurrent.Executors;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.net.ServerSocket;
import java.util.ArrayList;

public enum EServerInformation
{
    INSTANCE;

    // TODO Move to env var
    public static final int PORT = 8080;

    private static final int MAX_CLIENTS = 32;

    private final ServerSocket serverSocket;
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

    public ServerSocket getServerSocket()
    {
        return serverSocket;
    }

    public ExecutorService getExecutorService()
    {
        return executorService;
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
