package sep.server.model;

import sep.server.viewmodel.        Session;
import sep.server.viewmodel.        ClientInstance;
import sep.                         Types;
import sep.server.model.game.       GameState;

import java.net.                    ServerSocket;
import java.io.                     IOException;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.util.                   ArrayList;

/**
 * The Server Information enum is a singleton class that represents the server itself. The Server Instance is spawned
 * at application startup by the {@link sep.server.viewmodel.ServerListener ServerListener} and is not destroyed until
 * the application is killed. The Server Instance can be created only once and is shared across all threads. It
 * implements high-level logic for the server application itself, for example, connecting clients to existing
 * session or creating new ones if necessary.
 */
public enum EServerInformation
{
    INSTANCE;

    private static final Logger l = LogManager.getLogger(EServerInformation.class);

    public static final int KEEP_ALIVE_INTERVAL = 5_000;

    private int                         port;
    private ServerSocket                serverSocket;
    private final ArrayList<Session>    sessions;
    /** @deprecated */
    private int                         minRemotePlayerCountToStart;
    private int                         minHumanPlayerCountToStart;

    private EServerInformation()
    {
        this.port                           = Types.EPort.INVALID.i;
        this.serverSocket                   = null;
        this.sessions                       = new ArrayList<Session>();
        this.minRemotePlayerCountToStart    = GameState.DEFAULT_MIN_REMOTE_PLAYER_COUNT_TO_START;
        this.minHumanPlayerCountToStart     = GameState.DEFAULT_MIN_HUMAN_PLAYER_COUNT_TO_START;

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
                ci.disconnect();
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

    /** This method will not check if the session ID is already valid. */
    public Session createNewSession(final String sessionID)
    {
        final Session s = new Session(sessionID);
        this.sessions.add(s);
        return s;
    }

    public void removeSession(final Session session) throws RuntimeException
    {
        this.sessions.remove(session);

        try
        {
            session.onClose();
        }
        catch (final InterruptedException e)
        {
            l.fatal("Could not close session {}. Something fishy is going on. Shutting down.", session.getSessionID());
            l.fatal(e.getMessage());
            throw new RuntimeException(e);
        }

        l.info("Session [{}] closed successfully.", session.getSessionID());

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

    public void setMinHumanPlayerCount(final int min)
    {
        this.minHumanPlayerCountToStart = min;
        return;
    }

    public int getMinHumanPlayerCountToStart()
    {
        return this.minHumanPlayerCountToStart;
    }

    // region Getters and Setters

}
