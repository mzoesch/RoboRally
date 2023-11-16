package sep.server.viewmodel;

import sep.server.json.mainmenu.InitialClientConnectionModel;
import sep.server.json.DefaultClientRequestParser;
import sep.server.model.EServerInformation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.Objects;
import org.json.JSONObject;
import org.json.JSONException;
import java.net.SocketException;

/**
 * High-level manager object for a client connection to the server. A Client Instance is spawned at connection
 * creation and not destroyed until the connection is closed. A Client Instance object will always create a new
 * separate thread for itself to read and write to the client socket.
 *
 * <p> It implements high-level methods as, for example, sending and receiving messages from the client. It will
 * also handle the initial connection handshake to register a client in a session.
 */
public final class ClientInstance implements Runnable
{
    /** Escape character to close the connection to the server. In ASCII this is the dollar sign. */
    private static final int ESCAPE_CHARACTER = 36;

    private final Socket socket;
    private final InputStreamReader inputStreamReader;
    private final BufferedReader bufferedReader;
    private final OutputStreamWriter outputStreamWriter;
    private final BufferedWriter bufferedWriter;

    /** Must be crated to join a given session. */
    private PlayerController playerController;
    /** If the client is registered in a session. */
    private boolean bIsRegistered;

    public ClientInstance(Socket socket) throws IOException
    {
        super();

        this.socket = socket;
        this.inputStreamReader = new InputStreamReader(this.socket.getInputStream());
        this.outputStreamWriter = new OutputStreamWriter(this.socket.getOutputStream());
        this.bufferedReader = new BufferedReader(this.inputStreamReader);
        this.bufferedWriter = new BufferedWriter(this.outputStreamWriter);

        this.playerController = null;
        this.bIsRegistered = false;

        return;
    }

    private void handleDisconnect()
    {
        System.out.printf("[SERVER] Client %s disconnected.%n", this.socket.getInetAddress());

        if (this.bIsRegistered)
        {
            this.playerController.getSession().leaveSession(this.playerController);
        }

        try
        {
            this.inputStreamReader.close();
            this.bufferedReader.close();
            this.outputStreamWriter.close();
            this.bufferedWriter.close();
            this.socket.close();
        }
        catch (IOException e)
        {
            System.err.printf("Could not close the client connection in an orderly way.%n");
            System.err.printf("%s%n", e.getMessage());
            return;
        }

        return;
    }

    /**
     * Will try to register a client to a new or already existing session.
     *
     * @return             True if the client was successfully registered in a session, false otherwise.
     * @throws IOException If the client connection was closed unexpectedly.
     */
    private boolean registerClient() throws IOException
    {
        while (true)
        {
            int escapeCharacter;
            try
            {
                escapeCharacter = this.bufferedReader.read();
            }
            catch (SocketException e)
            {
                this.handleDisconnect();
                System.out.printf("[SERVER] Client %s disconnected unexpectedly.%n", this.socket.getInetAddress());
                return false;
            }
            // If the client closed the connection in an orderly way, the server will receive -1.
            if (escapeCharacter == -1)
            {
                this.handleDisconnect();
                return false;
            }

            InitialClientConnectionModel icc;
            try
            {
                // We need to do this because we already read the first character.
                icc = new InitialClientConnectionModel(new JSONObject(String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine())));
            }
            catch (JSONException e)
            {
                System.err.printf("[SERVER] Failed to parse JSON request from client %s. Ignoring.%n", this.socket.getInetAddress());
                System.err.printf("%s%n", e.getMessage());
                continue;
            }

            try
            {
                if (Objects.equals(icc.getConnectionMethod(), "createSession"))
                {
                    System.out.printf("[SERVER] Client %s requested to create a new session.%n", this.socket.getInetAddress());
                    String sessionID = EServerInformation.INSTANCE.createNewSession();
                    this.playerController = ServerInstance.createNewPlayerController(this, icc.getPlayerName(), EServerInformation.INSTANCE.getSessionByID(sessionID));
                    this.playerController.getSession().joinSession(this.playerController);
                    this.bIsRegistered = true;

                    InitialClientConnectionModel.sendPositive(this.bufferedWriter, sessionID);
                    System.out.printf("[SERVER] Client %s is now registered in session %s.%n", this.socket.getInetAddress(), sessionID);

                    return true;
                }

                if (Objects.equals(icc.getConnectionMethod(), "joinSession"))
                {
                    System.out.printf("[SERVER] Client %s requested to join %s session.%n", this.socket.getInetAddress(), icc.getSessionID());
                    // TODO More validation checks
                    if (!EServerInformation.INSTANCE.isSessionIDValid(icc.getSessionID()))
                    {
                        System.out.printf("[SERVER] Client %s requested to join an invalid session %s.%n", this.socket.getInetAddress(), icc.getSessionID());
                        InitialClientConnectionModel.sendNegative(this.bufferedWriter, "ID does not exist.");
                        return false;
                    }
                    if (Objects.requireNonNull(EServerInformation.INSTANCE.getSessionByID(icc.getSessionID())).isPlayerNameInSession(icc.getPlayerName()))
                    {
                        System.out.printf("[SERVER] Client %s requested to join a session with an invalid name.%n", this.socket.getInetAddress());
                        InitialClientConnectionModel.sendNegative(this.bufferedWriter, "Player name already exists.");
                        return false;
                    }
                    this.playerController = ServerInstance.createNewPlayerController(this, icc.getPlayerName(), EServerInformation.INSTANCE.getSessionByID(icc.getSessionID()));
                    this.playerController.getSession().joinSession(this.playerController);
                    this.bIsRegistered = true;

                    InitialClientConnectionModel.sendPositive(this.bufferedWriter, icc.getSessionID());
                    System.out.printf("[SERVER] Client %s is now registered in session %s.$n", this.socket.getInetAddress(), icc.getSessionID());

                    return true;
                }
            }
            catch (JSONException e)
            {
                System.err.printf("[SERVER] Failed to parse JSON request from client %s. Ignoring.%n", this.socket.getInetAddress());
                System.err.printf("%s%n", e.getMessage());
                continue;
            }

            System.out.printf("[SERVER] Received unknown JSON from client.%n");
            System.out.printf("%s%n", icc.getJSON().toString(1));

            continue;
        }
    }

    private boolean waitForPostLoginConfirmation()
    {
        // TODO Handle timeout
        try
        {
            // If the client closed the connection in an orderly way, the server will receive -1.
            int escapeCharacter = this.bufferedReader.read();
            if (escapeCharacter == -1)
            {
                this.handleDisconnect();
                return false;
            }

            // We need to do this because we already read the first character.
            InitialClientConnectionModel icc = new InitialClientConnectionModel(new JSONObject(String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine())));
            return Objects.equals(icc.getConnectionMethod(), "postLoginConfirmation");
        }
        catch (IOException e)
        {
            System.err.printf("[SERVER] Failed to read from client %s. Disconnecting the client.%n", this.socket.getInetAddress());
            System.err.printf("%s%n", e.getMessage());
            return false;
        }
        catch (JSONException e)
        {
            System.err.printf("[SERVER] Received invalid JSON from client %s. Disconnecting the client.%n", this.socket.getInetAddress());
            System.err.printf("%s%n", e.getMessage());
            return false;
        }
    }

    private boolean parseRequest(DefaultClientRequestParser dcrp) throws JSONException
    {
        if (Objects.equals(dcrp.getType(), "chatMessage"))
        {
            System.out.printf("[SERVER] Client %s sent chat message %s.%n", this.socket.getInetAddress(), dcrp.getChatMessage());
            this.playerController.getSession().broadcastChatMessage(this.playerController.getPlayerName(), dcrp.getChatMessage());
            return true;
        }

        // etc.

        return false;
    }

    private void defaultClientListener() throws IOException
    {
        while (true)
        {
            // If the client closed the connection in an orderly way, the server will receive -1.
            int escapeCharacter = this.bufferedReader.read();
            if (escapeCharacter == -1)
            {
                this.handleDisconnect();
                return;
            }

            if (escapeCharacter == ClientInstance.ESCAPE_CHARACTER)
            {
                System.out.printf("[SERVER] Received escape character from client %s. Disconnecting the client.%n", this.socket.getInetAddress());
                this.handleDisconnect();
                break;
            }

            DefaultClientRequestParser dcrp;
            try
            {
                // We need to do this because we already read the first character.
                dcrp = new DefaultClientRequestParser(new JSONObject(String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine())));
            }
            catch (JSONException e)
            {
                System.err.printf("[SERVER] Received invalid JSON from client %s. Ignoring.%n", this.socket.getInetAddress());
                System.err.println(e.getMessage());
                continue;
            }

            boolean bAccepted;
            try
            {
                bAccepted = this.parseRequest(dcrp);
            }
            catch (JSONException e)
            {
                System.err.printf("[SERVER] Received invalid JSON from client %s. Ignoring.%n", this.socket.getInetAddress());
                System.err.println(e.getMessage());
                continue;
            }

            if (!bAccepted)
            {
                System.err.printf("%s%n", dcrp.getRequest().toString(1));
            }

            continue;
        }

        return;
    }

    /** Life-cycle of a client connection. */
    @Override
    public void run()
    {
        try
        {
            boolean bSuccess = this.registerClient();

            if (!bSuccess)
            {
                this.handleDisconnect();
                return;
            }

            boolean bPostLoginConfirmation = this.waitForPostLoginConfirmation();
            if (!bPostLoginConfirmation)
            {
                this.handleDisconnect();
                return;
            }

            this.playerController.getSession().defaultBehaviourAfterPostLogin(this.playerController);

            this.defaultClientListener();

            return;
        }
        catch (IOException e)
        {
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            this.handleDisconnect();
            return;
        }
    }

    public BufferedWriter getBufferedWriter()
    {
        return bufferedWriter;
    }

}
