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

    // TODO ??? In our current protocol (v0.1), this is not handled in any way. So how should we close connections???
    /** Escape character to close the connection to the server. In ASCII this is the dollar sign. */
    private static final int ESCAPE_CHARACTER = 36;

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

        l.info("Client {} is disconnecting.", this.socket.getInetAddress());

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
            l.warn("Could not close client {} socket connection in an orderly way.", this.socket.getInetAddress());
            l.warn(e.getMessage());
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

        l.info("Client {} registered successfully.", this.socket.getInetAddress());

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
            l.error("Failed to read from client {}. Disconnecting them.", this.socket.getInetAddress());
            l.error(e.getMessage());
            return null;
        }
        catch (JSONException e)
        {
            l.error("Received invalid JSON from client {}. Disconnecting them.", this.socket.getInetAddress());
            l.error(e.getMessage());
            return null;
        }
    }

    private boolean parseRequest(DefaultClientRequestParser dcrp) throws JSONException
    {
        if (Objects.equals(dcrp.getType_v2(), "Alive"))
        {
            l.trace("Ok keep-alive from client {}.", this.socket.getInetAddress());
            this.setAlive(true);
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "PlayerValues"))
        {
            String oldName = this.playerController.getPlayerName();

            // TODO We have to do some validation here.
            this.playerController.setPlayerName(dcrp.getPlayerName());
            this.playerController.setFigure(dcrp.getFigureID());
            l.debug("Player {} selected figure {}.", this.playerController.getPlayerName(), this.playerController.getFigure());

            this.playerController.getSession().sendPlayerValuesToAllClients(this.playerController);
            if (!Objects.equals(oldName, this.playerController.getPlayerName()))
            {
                this.playerController.getSession().broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s changed their name to %s.", oldName, this.playerController.getPlayerName()));
            }

            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "SendChat"))
        {
            // TODO Validate chat message.
            this.playerController.getSession().handleChatMessage(this.playerController, dcrp.getChatMessage_v2(), dcrp.getReceiverID());
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "SetStatus"))
        {
            this.playerController.getSession().handlePlayerReadyStatus(this.playerController, dcrp.getIsReadyInLobby());
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "MapSelected"))
        {
            this.playerController.getSession().handleSelectCourseName(this.playerController, dcrp.getCourseName());
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "PlayCard")) {
            l.debug("Received play Card from client.");
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "SelectedCard")) {
            l.debug("Received selected Card from client.");
            String selectedCard = dcrp.getSelectedCard();
            int selectedRegister = dcrp.getSelectedCardRegister();
            this.playerController.setSelectedCardInRegister(selectedCard,selectedRegister);
            return true;
        }

        if (Objects.equals(dcrp.getType_v2(), "SetStartingPoint")) {
            l.debug("Received starting point from client.");
            int x = dcrp.getXCoordinate();
            int y = dcrp.getYCoordinate();
            this.playerController.getSession().getGameState().setStartingPoint(playerController,x,y);
            return true;
        }

        return false;
    }

    private void defaultClientListener() throws IOException
    {
        while (true)
        {
            int escapeCharacter;
            try
            {
                // If the client closed the connection in an orderly way, the server will receive -1.
                escapeCharacter = this.bufferedReader.read();
            }
            catch (SocketException e)
            {
                this.handleDisconnect();
                l.warn("Client {} disconnected unexpectedly.", this.socket.getInetAddress());
                return;
            }

            if (escapeCharacter == -1)
            {
                this.handleDisconnect();
                return;
            }

            boolean bAccepted;
            try
            {
                bAccepted = this.parseRequest(new DefaultClientRequestParser(new JSONObject(String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine()))));
            }
            catch (JSONException e)
            {
                l.warn("Received invalid JSON from client {}. Ignoring.", this.socket.getInetAddress());
                l.warn(e.getMessage());
                continue;
            }

            if (bAccepted)
            {
                continue;
            }

            l.warn("Received unknown request from client {}. Ignoring.", this.socket.getInetAddress());
            continue;
        }
    }

    public void sendKeepAlive()
    {
        this.bIsAlive = false;
        new KeepAliveModel(this).send();
        return;
    }

    public void sendMockJSON(JSONObject mockJSON)
    {
        try
        {
            this.bufferedWriter.write(mockJSON.toString());
            this.bufferedWriter.newLine();
            this.bufferedWriter.flush();
        }
        catch (IOException e)
        {
            l.error("Failed to send mock JSON to client {}.", this.socket.getInetAddress());
            l.error(e.getMessage());
            return;
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

    public BufferedWriter getBufferedWriter()
    {
        return bufferedWriter;
    }

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

    // endregion Getters and Setters

}
