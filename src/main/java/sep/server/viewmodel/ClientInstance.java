package sep.server.viewmodel;

import sep.server.json.common.ChatMsgModel;
import sep.server.json.mainmenu.InitialClientConnectionModel_v2;
import sep.server.json.DefaultClientRequestParser;
import sep.server.model.EServerInformation;
import sep.server.json.common.KeepAliveModel;

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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static final Logger l = LogManager.getLogger(ClientInstance.class);

    public Thread thread;
    private final Socket socket;
    private final InputStreamReader inputStreamReader;
    private final BufferedReader bufferedReader;
    private final OutputStreamWriter outputStreamWriter;
    private final BufferedWriter bufferedWriter;

    /** Must be created to join a given session. */
    private PlayerController playerController;
    /** If the client is registered in a session. */
    private boolean bIsRegistered;
    /** Used to keep track if a client responded to the keep-alive request form the server. */
    private boolean bIsAlive;
    /**
     * If a client disconnects unexpectedly, we may try to disconnect them multiple times.
     * This variable is used to keep track of that.
     */
    private boolean bDisconnecting;

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
        this.bIsAlive = true;
        this.bDisconnecting = false;

        return;
    }

    public void handleDisconnect()
    {
        /* Because this method may be called multiple times if the connection did not close in an orderly way. */
        if (this.bDisconnecting)
        {
            return;
        }
        this.bDisconnecting = true;

        l.debug("Client {} is disconnecting.", this.getAddr());

        if (this.bIsRegistered)
        {
            this.playerController.getSession().leaveSession(this.playerController);
        }

        this.thread.interrupt();

        try
        {
            this.socket.close();
        }
        catch (IOException e)
        {
            l.warn("Could not close client {} socket connection in an orderly way.", this.getAddr());
            l.warn(e.getMessage());
            return;
        }

        l.info("Client {} disconnected successfully.", this.getAddr());

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
        InitialClientConnectionModel_v2 icc = new InitialClientConnectionModel_v2(this);
        icc.sendProtocolVersion();

        icc.waitForProtocolVersionConfirmation();
        boolean isValid = icc.isClientProtocolVersionValid();
        if (!isValid)
        {
            return false;
        }

        // TODO Check if session id is valid.
        Session s = EServerInformation.INSTANCE.getNewOrExistingSessionID(icc.getSessionID());
        this.playerController = ServerInstance.createNewPlayerController(this, s);
        icc.sendWelcome(this.playerController.getPlayerID());
        this.playerController.getSession().joinSession(this.playerController);
        this.bIsRegistered = true;

        l.info("Client {} registered successfully.", this.getAddr());

        return true;
    }

    /**
     * Only use this method for the initial client registration. For receiving information form the client
     * after, use the {@link #defaultClientListener()} method.*/
    public String waitForResponse()
    {
        try
        {
            // If the client closed the connection in an orderly way, the server will receive -1.
            int escapeCharacter = this.bufferedReader.read();
            if (escapeCharacter == -1)
            {
                this.handleDisconnect();
                return null;
            }

            return String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine());
        }
        catch (IOException e)
        {
            l.error("Failed to read from client {}. Disconnecting them.", this.getAddr());
            l.error(e.getMessage());
            return null;
        }
        catch (JSONException e)
        {
            l.error("Received invalid JSON from client {}. Disconnecting them.", this.getAddr());
            l.error(e.getMessage());
            return null;
        }
    }

    private boolean parseRequest(DefaultClientRequestParser dcrp) throws JSONException
    {
        if (Objects.equals(dcrp.getType_v2(), "Alive"))
        {
            l.trace("Received keep-alive from client {}. Ok.", this.getAddr());
            this.setAlive(true);
            return true;
        }

        /* If core player information is being changed. */
        if (Objects.equals(dcrp.getType_v2(), "PlayerValues"))
        {
            final String oldName = this.playerController.getPlayerName();

            // TODO We have to do some validation here.
            this.playerController.setPlayerName(dcrp.getPlayerName());
            this.playerController.setFigure(dcrp.getFigureID());
            l.debug("Client {} selected figure {}.", this.getAddr(), this.playerController.getFigure());

            this.playerController.getSession().sendPlayerValuesToAllClients(this.playerController);
            if (!Objects.equals(oldName, this.playerController.getPlayerName()))
            {
                this.playerController.getSession().broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s changed their name to %s.", oldName, this.playerController.getPlayerName()));
                l.debug("Client {} changed their name from {} to {}.", this.getAddr(), oldName, this.playerController.getPlayerName());
            }

            return true;
        }

        /* If this client wants to send a chat message. */
        if (Objects.equals(dcrp.getType_v2(), "SendChat"))
        {
            l.debug("Client {} wants to send chat message [{}] to lobby {}.", this.getAddr(), dcrp.getChatMessage_v2(), this.getPlayerController().getSession().getSessionID());
            // TODO Validate chat message.
            this.playerController.getSession().handleChatMessage(this.playerController, dcrp.getChatMessage_v2(), dcrp.getReceiverID());
            return true;
        }

        /* If the client has set their ready status in the lobby. */
        if (Objects.equals(dcrp.getType_v2(), "SetStatus"))
        {
            l.debug("Client {} set ready status to {}.", this.getAddr(), dcrp.getIsReadyInLobby());
            this.playerController.getSession().handlePlayerReadyStatus(this.playerController, dcrp.getIsReadyInLobby());
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "MapSelected"))
        {
            l.debug("Client {} selected course {}.", this.getAddr(), dcrp.getCourseName());
            this.playerController.getSession().handleSelectCourseName(this.playerController, dcrp.getCourseName());
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "PlayCard"))
        {
            l.debug("Received play Card from client.");
            return true;
        }

        /* If the client has set one of their five registers. */
        if (Objects.equals(dcrp.getType_v2(), "SelectedCard"))
        {
            l.debug("Client {} selected card [{}] to register {}.", this.getAddr(), dcrp.getBody().isNull("card") ? null : dcrp.getSelectedCardAsString(), dcrp.getSelectedCardRegister());
            this.playerController.setSelectedCardInRegister(dcrp.getBody().isNull("card") ? null : dcrp.getSelectedCardAsString(), dcrp.getSelectedCardRegister());
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "SetStartingPoint"))
        {
            l.debug("Client {} set starting point to ({},{})", this.getAddr(), dcrp.getXCoordinate(), dcrp.getYCoordinate());
            this.playerController.getSession().getGameState().setStartingPoint(playerController, dcrp.getXCoordinate(), dcrp.getYCoordinate());
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "PickDamage")) {
            l.debug("Received a picked damage card from client.");
            return true;
        }

        return false;
    }

    private void defaultClientListener() throws IOException
    {
        while (true)
        {
            final int escapeCharacter;
            try
            {
                // If the client closed the connection in an orderly way, the server will receive -1.
                escapeCharacter = this.bufferedReader.read();
            }
            catch (SocketException e)
            {
                l.warn("Client {}'s socket connection was closed unexpectedly.", this.getAddr());
                this.handleDisconnect();
                return;
            }

            if (escapeCharacter == -1)
            {
                l.debug("Client {} requested to close the server connection in an orderly way.", this.getAddr());
                this.handleDisconnect();
                return;
            }

            final String s = String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine());
            l.trace("Received request from client {}. Parsing: {}", this.getAddr(), s);
            final boolean bAccepted;
            try
            {
                bAccepted = this.parseRequest(new DefaultClientRequestParser(new JSONObject(s)));
            }
            catch (JSONException e)
            {
                l.warn("Received invalid JSON from client {}. Ignoring.", this.getAddr());
                l.warn(e.getMessage());
                l.warn(s);
                continue;
            }

            if (bAccepted)
            {
                continue;
            }

            l.warn("Received unknown request from client {}. Ignoring.", this.getAddr());
            continue;
        }
    }

    public void sendKeepAlive()
    {
        this.bIsAlive = false;
        new KeepAliveModel(this).send();
        return;
    }

    public boolean sendRemoteRequest(JSONObject j)
    {
        l.trace("Sending remote request to client {}. {}", this.getAddr(), j.toString(0));

        try
        {
            this.bufferedWriter.write(j.toString());
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }
        catch (IOException e)
        {
            l.error("Failed to send remote request to client {}.", this.getAddr());
            l.error(e.getMessage());
            return false;
        }

        return true;
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

    // region Getters and Setters

    // TODO Do we have to synchronize this? Because of multithreading?
    public boolean isAlive()
    {
        return this.bIsAlive;
    }

    // TODO Do we have to synchronize this? Because of multithreading?
    public void setAlive(boolean bIsAlive)
    {
        this.bIsAlive = bIsAlive;
        return;
    }

    public PlayerController getPlayerController()
    {
        return this.playerController;
    }

    public String getAddr()
    {
        return this.socket.getRemoteSocketAddress().toString();
    }

    // endregion Getters and Setters

}
