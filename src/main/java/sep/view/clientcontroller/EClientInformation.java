package sep.view.clientcontroller;

import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.io.BufferedWriter;
import java.util.concurrent.Executors;
import java.io.OutputStreamWriter;

/**
 * Singleton object that holds all relevant information about the client's connection to the server and the game
 * instance. This object is shared across all threads. It is used to communicate with the server and to store
 * information that is persistent throughout the game's lifetime.
 */
public enum EClientInformation
{
    INSTANCE;

    // TODO To env var
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;
    public static final String PROTOCOL_VERSION = "0.1";

    private Socket socket;
    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private final StringBuilder stdServerErrPipeline;
    private ServerListener serverListener;
    private ExecutorService executorService;

    public GameInstance JFX_INSTANCE;

    private String connectedLobbyID;
    private String playerName;

    private EClientInformation()
    {
        this.JFX_INSTANCE = null;

        this.socket = null;
        this.inputStreamReader = null;
        this.outputStreamWriter = null;
        this.bufferedReader = null;
        this.bufferedWriter = null;
        this.stdServerErrPipeline = new StringBuilder();
        this.serverListener = null;
        this.executorService = null;

        this.connectedLobbyID = null;
        this.playerName = null;

        return;
    }

    public boolean establishAServerConnection() throws IOException
    {
        if (this.socket != null || this.bufferedReader != null || this.bufferedWriter != null)
        {
            System.err.println("[CLIENT] Socket already initialized.");
            return false;
        }

        this.socket = new Socket(EClientInformation.SERVER_IP, EClientInformation.SERVER_PORT);
        this.inputStreamReader = new InputStreamReader(this.socket.getInputStream());
        this.outputStreamWriter = new OutputStreamWriter(this.socket.getOutputStream());
        this.bufferedReader = new BufferedReader(this.inputStreamReader);
        this.bufferedWriter = new BufferedWriter(this.outputStreamWriter);

        return true;
    }

    /** Will block the main thread until a response from the server is received. Only use this method for the initial
     * connection to the server. After that, use the {@link #listen()} method.
     */
    public String waitForServerResponse() throws IOException
    {
        if (this.bufferedReader == null)
        {
            System.err.println("[CLIENT] Socket not initialized.");
            return null;
        }

        return this.bufferedReader.readLine();
    }

    /**
     * Will create a new thread that will listen for server responses to not block the main thread.
     * @see sep.view.clientcontroller.ServerListener
     * */
    public void listen()
    {
        if (this.serverListener != null)
        {
            System.err.println("[CLIENT] ServerListener already initialized.");
            return;
        }

        this.executorService = Executors.newFixedThreadPool(1);
        this.serverListener = new ServerListener(this.socket, this.inputStreamReader, this.bufferedReader);
        this.executorService.execute(this.serverListener);

        return;
    }

    public Socket getSocket()
    {
        return this.socket;
    }

    public BufferedReader getBufferedReader()
    {
        return bufferedReader;
    }

    public BufferedWriter getBufferedWriter()
    {
        return bufferedWriter;
    }

    public StringBuilder getStdServerErrPipeline()
    {
        return stdServerErrPipeline;
    }

    public void setConnectedSessionID(String connectedLobbyID)
    {
        this.connectedLobbyID = connectedLobbyID;
        return;
    }

    public String getConnectedLobbyID()
    {
        return this.connectedLobbyID;
    }

    public void setServerListener(ServerListener serverListener)
    {
        this.serverListener = serverListener;
        return;
    }

    public void sendServerRequest(JSONObject json)
    {
        if (this.bufferedWriter == null)
        {
            System.err.println("[CLIENT] Socket not initialized.");
            return;
        }

        try
        {
            this.bufferedWriter.write(json.toString());
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }
        catch (IOException e)
        {
            System.err.println("[CLIENT] Failed to send request to server.");
            System.err.println(e.getMessage());
            return;
        }

        return;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
        return;
    }

    public String getPlayerName()
    {
        return this.playerName;
    }

    public void resetServerConnectionAfterDisconnect()
    {
        this.socket = null;
        this.inputStreamReader = null;
        this.outputStreamWriter = null;
        this.bufferedReader = null;
        this.bufferedWriter = null;
        this.stdServerErrPipeline.setLength(0);

        this.connectedLobbyID = null;
        this.playerName = null;

        return;
    }

}
