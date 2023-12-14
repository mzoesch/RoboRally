package sep.server.model;

import sep.server.viewmodel.Session;
import sep.server.viewmodel.ClientInstance;
import sep.EPort;
import sep.EArgs;
import sep.server.model.game.GameState;

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

    public static final String PROTOCOL_VERSION = "1.0";
    public static final int KEEP_ALIVE_INTERVAL = 5_000;

    private int port;
    private ServerSocket serverSocket;
    private final ArrayList<Session> sessions;
    private int minRemotePlayerCountToStart;

    private EServerInformation()
    {
        this.port = EPort.INVALID.i;
        this.serverSocket = null;
        this.sessions = new ArrayList<Session>();
        this.minRemotePlayerCountToStart = GameState.DEFAULT_MIN_REMOTE_PLAYER_COUNT_TO_START;

        return;
    }

    public void startServer() throws IOException
    {
        if (this.serverSocket != null)
        {
            l.error("Server already started.");
            return;
        }

        this.serverSocket = new ServerSocket(this.port);
        l.info("Server started on port {}.", this.port);
        return;
    }

    public void sendKeepAlive()
    {
        final ArrayList<ClientInstance> dead = new ArrayList<ClientInstance>();

        for (final Session s : this.sessions)
        {
            s.sendKeepAlive(dead);
            continue;
        }

        if (!this.sessions.isEmpty())
        {
            l.trace("Sent keep-alive to all clients.");
        }

        if (!dead.isEmpty())
        {
            l.warn("Removing {} dead client{}.", dead.size(), dead.size() == 1 ? "" : "s");
            for (final ClientInstance ci : dead)
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

    public Session getSessionByID(final String sessionID)
    {
        for (final Session s : this.sessions)
        {
            if (s.getSessionID().equals(sessionID))
            {
                return s;
            }

            continue;
        }

        return null;
    }

    public boolean isSessionIDValid(final String sessionID)
    {
        for (final Session s : this.sessions)
        {
            if (s.getSessionID().equals(sessionID))
            {
                return true;
            }

            continue;
        }

        return false;
    }

    public Session getNewOrExistingSessionID(final String sessionID)
    {
        if (this.isSessionIDValid(sessionID))
        {
            return this.getSessionByID(sessionID);
        }

        return this.createNewSession(sessionID);
    }

    /** This method will not check if the session ID is valid. */
    public Session createNewSession(final String sessionID)
    {
        final Session s = new Session(sessionID);
        this.sessions.add(s);
        return s;
    }

    public void removeSession(final Session session)
    {
        this.sessions.remove(session);
        l.info("Session {} closed.", session.getSessionID());
        return;
    }

    public int getPort()
    {
        return this.port;
    }

    public void setPort(final int port)
    {
        this.port = port;
        return;
    }

    public void setMinRemotePlayerCountToStart(final int min)
    {
        this.minRemotePlayerCountToStart = min;
        return;
    }

    public int getMinRemotePlayerCountToStart()
    {
        return this.minRemotePlayerCountToStart;
    }

    // region Getters and Setters

}
