package sep.server.model;

import sep.server.viewmodel.Session;
import sep.server.viewmodel.ClientInstance;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The EServerInformation enum is a singleton class that represents the server itself. The EServerInstance is spawned
 * at application startup by the Server Listener and is not destroyed until the application is killed. The
 * EServerInstance can be created only once and is shared across all threads. It implements high-level logic for the
 * server application, for example, connecting clients to existing session or creating new ones.
 */
public enum EServerInformation
{
    INSTANCE;

    private static final Logger l = LogManager.getLogger(EServerInformation.class);

    // TODO Move to env var
    public static final int PORT = 8080;
    public static final String PROTOCOL_VERSION = "0.1";
    public static final int KEEP_ALIVE_INTERVAL = 5_000;

    private final ServerSocket serverSocket;

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
            LogManager.getLogger(EServerInformation.class).fatal("Server socket failed in process.");
            LogManager.getLogger(EServerInformation.class).fatal(e.getMessage());
            System.exit(sep.EArgs.ERROR);
        }
        this.serverSocket = tServerSocket;

        this.sessions = new ArrayList<Session>();

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

        l.trace("Sent keep-alive to all clients.");

        if (!dead.isEmpty())
        {
            l.warn("Removing {} dead client{}.", dead.size(), dead.size() == 1 ? "" : "s");
            for (ClientInstance ci : dead)
            {
                ci.handleDisconnect();
                continue;
            }

            return;
        }

        return;
    }

    // region Getters and Setters

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

    public Session getNewOrExistingSessionID(String sessionID)
    {
        if (this.isSessionIDValid(sessionID))
        {
            return this.getSessionByID(sessionID);
        }

        return this.createNewSession(sessionID);
    }

    /** This method will not check if the session ID is valid. */
    public Session createNewSession(String sessionID)
    {
        Session s = new Session(sessionID);
        this.sessions.add(s);
        return s;
    }

    public void removeSession(Session session)
    {
        this.sessions.remove(session);
        l.info("Session {} closed.", session.getSessionID());
        return;
    }

    // region Getters and Setters

}
