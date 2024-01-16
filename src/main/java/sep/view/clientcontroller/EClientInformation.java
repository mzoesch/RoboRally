package sep.view.clientcontroller;

import sep.                         EArgs;
import sep.view.json.common.        IdentificationModel;
import sep.view.json.               ChatMsgModel;
import sep.view.viewcontroller.     ViewSupervisor;
import sep.view.viewcontroller.     SceneController;
import sep.view.lib.                EFigure;

import org.json.                    JSONObject;
import java.util.concurrent.        ExecutorService;
import java.io.                     BufferedReader;
import java.io.                     InputStreamReader;
import java.io.                     BufferedWriter;
import java.io.                     IOException;
import java.io.                     OutputStreamWriter;
import java.util.concurrent.        Executors;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.net.                    ConnectException;
import java.net.                    UnknownHostException;
import java.net.                    Socket;
import java.util.concurrent.atomic. AtomicBoolean;

/**
 * Singleton object that holds all relevant information about the client's connection to the server and the game
 * instance. This object is shared across all threads. It is used to communicate with the server and to store
 * information that is persistent throughout the game's lifetime.
 */
public enum EClientInformation
{
    INSTANCE;

    private static final Logger l = LogManager.getLogger(EClientInformation.class);

    public static final String AGENT_PREFIX = "[BOT]";

    private static final int DISCONNECT_ATOMIC_RESET_DELAY = 500;

    private String                  serverIP;
    private int                     serverPort;

    boolean                         bMockView;

    private Socket                  socket;
    private InputStreamReader       inputStreamReader; /* TODO We may remove the stream readers and writers. */
    private OutputStreamWriter      outputStreamWriter;
    private BufferedReader          bufferedReader;
    private BufferedWriter          bufferedWriter;
    private final StringBuilder     stdServerErrPipeline;
    private ServerListener          serverListener;
    private ExecutorService         executorService;
    private final AtomicBoolean     bDisconnectHandled      = new AtomicBoolean(false);

    /** The main thread. */
    private GameInstance            JFXInstance;

    private String                  preferredSessionID;
    /** Cannot be changed for the duration of a session connection. */
    private int                     playerID;

    private boolean                 bIsAgent;
    private String                  prefAgentName;
    private boolean                 bAllowLegacyAgents;

    private EClientInformation()
    {
        this.JFXInstance = null;

        this.serverIP               = EArgs.PREF_SERVER_IP;
        this.serverPort             = sep.Types.EPort.DEFAULT.i;

        this.bMockView              = false;

        this.socket                 = null;
        this.inputStreamReader      = null;
        this.outputStreamWriter     = null;
        this.bufferedReader         = null;
        this.bufferedWriter         = null;
        this.stdServerErrPipeline   = new StringBuilder();
        this.serverListener         = null;
        this.executorService        = null;

        this.playerID               = -1;
        this.preferredSessionID     = "";

        this.bIsAgent               = false;
        this.prefAgentName          = "";
        this.bAllowLegacyAgents     = false;

        return;
    }

    /** Establishing a server connection via the TCP. */
    public boolean establishAServerConnection() throws IOException
    {
        if (this.socket != null || this.bufferedReader != null || this.bufferedWriter != null)
        {
            l.error("Socket already initialized.");
            return false;
        }

        l.info(String.format("Connecting to server [%s:%d].", EClientInformation.INSTANCE.serverIP, EClientInformation.INSTANCE.serverPort));

        try
        {
            this.socket = new Socket(EClientInformation.INSTANCE.serverIP, EClientInformation.INSTANCE.serverPort);
        }
        catch (final ConnectException e)
        {
            l.error("Failed to connect to server.");
            l.error(e.getMessage());

            this.stdServerErrPipeline.setLength(0);
            this.stdServerErrPipeline.append(String.format("Failed to connect to server. %s", e.getMessage()));

            return false;
        }
        catch (final UnknownHostException e)
        {
            l.error("Failed to connect to server.");
            l.error(e.getMessage());

            this.stdServerErrPipeline.setLength(0);
            this.stdServerErrPipeline.append("Failed to connect to server. Unknown host.");

            return false;
        }

        this.inputStreamReader      = new InputStreamReader(this.socket.getInputStream());
        this.outputStreamWriter     = new OutputStreamWriter(this.socket.getOutputStream());
        this.bufferedReader         = new BufferedReader(this.inputStreamReader);
        this.bufferedWriter         = new BufferedWriter(this.outputStreamWriter);

        return true;
    }

    /**
     * Will block the calling thread until a response from the server is received. Only use this method for the
     * initial connection to the server. After that, use the {@link #listen(boolean)} method.
     */
    public String waitForServerResponse() throws IOException
    {
        if (this.bufferedReader == null)
        {
            l.error("Socket not initialized.");
            return null;
        }

        return this.bufferedReader.readLine();
    }

    /**
     * Will create a new thread that will listen for server responses to not block the main thread.
     *
     * @see sep.view.clientcontroller.ServerListener
     */
    public void listen(final boolean bBlock)
    {
        if (this.serverListener != null)
        {
            l.error("ServerListener already initialized.");
            return;
        }

        if (bBlock)
        {
            this.serverListener = new AgentSL_v2(this.bufferedReader);
            l.debug("Now listening for standard server responses.");
            this.serverListener.run();

            return;
        }

        this.executorService    = Executors.newFixedThreadPool(1);
        this.serverListener     = new HumanSL(this.bufferedReader);
        this.executorService.execute(this.serverListener);

        l.debug("Now listening for standard server responses.");

        return;
    }

    public void sendServerRequest(final JSONObject j)
    {
        if (this.bufferedWriter == null)
        {
            l.error("Socket not initialized.");
            return;
        }

        l.trace(String.format("Sending request to server: %s", j.toString(0)));

        try
        {
            this.bufferedWriter.write(j.toString(0));
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }
        catch (final IOException e)
        {
            l.error("Failed to send request to server.");
            l.error(e.getMessage());
            return;
        }

        return;
    }

    public void sendAddAgentRequest()
    {
        this.sendServerRequest(new IdentificationModel(this.preferredSessionID, true).toJSON());
        return;
    }

    /** @deprecated */
    public void sendRemoveAgentRequest(final int playerID)
    {
        // TODO WARNING
        //              Okay,
        //              so this is super super sketchy because there is no current way to request a client
        //              disconnection from the server, we send a chat message request directly to the server.
        //              But we must find a better way to implement this.
        //              We must ask how we should implement this.

        new ChatMsgModel(String.format("%sDETACH{%d}", ChatMsgModel.COMMAND_PREFIX, playerID), ChatMsgModel.CHAT_MSG_BROADCAST).send();
        return;
    }

    // region Getters and Setters

    public StringBuilder getStdServerErrPipeline()
    {
        return stdServerErrPipeline;
    }

    public void setPlayerID(final int playerID)
    {
        this.playerID = playerID;
        return;
    }

    public int getPlayerID()
    {
        return this.playerID;
    }

    public void resetServerConnectionAfterDisconnect()
    {
        this.socket                 = null;
        this.inputStreamReader      = null;
        this.outputStreamWriter     = null;
        this.bufferedReader         = null;
        this.bufferedWriter         = null;
        this.stdServerErrPipeline.setLength(0);
        this.serverListener         = null;
        this.executorService        = null;

        this.playerID               = -1;
        this.preferredSessionID     = "";

        return;
    }

    public boolean hasServerConnection()
    {
        return this.socket != null && this.bufferedReader != null && this.bufferedWriter != null;
    }

    public void setPreferredSessionID(final String id)
    {
        this.preferredSessionID = id;
        return;
    }

    public String getPreferredSessionID()
    {
        return this.preferredSessionID;
    }

    public void setJFXInstance(final GameInstance JFX_INSTANCE)
    {
        this.JFXInstance = JFX_INSTANCE;
        return;
    }

    public GameInstance getJFXInstance()
    {
        return this.JFXInstance;
    }

    public void setServerIP(final String SERVER_IP)
    {
        this.serverIP = SERVER_IP;
        return;
    }

    public void setServerPort(final int port)
    {
        this.serverPort = port;
        return;
    }

    public void setMockView(final boolean bMockView)
    {
        this.bMockView = bMockView;
        return;
    }

    public boolean isMockView()
    {
        return this.bMockView;
    }

    public void closeSocket()
    {
        if (this.socket == null)
        {
            if (EClientInformation.INSTANCE.isAgent())
            {
                l.warn("Tried to close socket, but socket was not initialized.");
                return;
            }

            /* We are never connected to a server in the lobby scene. */
            if (ViewSupervisor.getSceneController().getCurrentScreen().id().equals(SceneController.MAIN_MENU_ID))
            {
                return;
            }

            l.warn("Tried to close socket, but socket was not initialized.");

            return;
        }

        try
        {
            this.socket.close();
        }
        catch (final IOException e)
        {
            l.error("Failed to close socket.");
            l.error(e.getMessage());
            return;
        }

        return;
    }

    public void setIsAgent(final boolean bIsAgent)
    {
        this.bIsAgent = bIsAgent;
        return;
    }

    public boolean isAgent()
    {
        return this.bIsAgent;
    }

    public void setPrefAgentName(final String prefAgentName)
    {
        this.prefAgentName = prefAgentName;
        return;
    }

    public String getPrefAgentName() throws RuntimeException
    {
        if (this.prefAgentName == null || this.prefAgentName.isEmpty())
        {
            throw new RuntimeException("Agent name not set.");
        }

        return this.prefAgentName;
    }

    public void setAllowLegacyAgents(final boolean bAllowLegacyAgents)
    {
        this.bAllowLegacyAgents = bAllowLegacyAgents;
        return;
    }

    public boolean getAllowLegacyAgents()
    {
        return this.bAllowLegacyAgents;
    }

    public EFigure getPrefAgentRobot()
    {
        return EFigure.SPIN;
    }

    public void setDisconnectHandled(final boolean bHandled)
    {
        l.debug(String.format("Setting disconnect atomic flag to %b.", bHandled));
        this.bDisconnectHandled.set(bHandled);

        if (!bHandled)
        {
            return;
        }

        /* Kinda sketchy but works for now. */
        new Thread(() ->
        {
            try
            {
                Thread.sleep(EClientInformation.DISCONNECT_ATOMIC_RESET_DELAY);
            }
            catch (final InterruptedException e)
            {
                l.fatal("Failed to reset disconnect atomic flag.");
                l.fatal(e.getMessage());
                GameInstance.kill();
                return;
            }

            l.debug("Resetting disconnect atomic flag.");
            this.bDisconnectHandled.set(false);
        })
        .start();

        return;
    }

    public boolean getDisconnectHandled()
    {
        return this.bDisconnectHandled.get();
    }

    // endregion Getters and Setters

}
