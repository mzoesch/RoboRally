package sep.server.viewmodel;

import sep.server.json.common.ChatMsgModel;
import sep.server.json.mainmenu.InitialClientConnectionModel_v2;
import sep.server.json.RDefaultClientRequestParser;
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
import java.util.function.Supplier;
import java.util.HashMap;

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

    private final HashMap<String, Supplier<Boolean>> clientReq =
    new HashMap<String, Supplier<Boolean>>()
    {{
        put("Alive", ClientInstance.this::onAlive);
        put("PlayerValues", ClientInstance.this::onCorePlayerAttributesChanged);
        put("SendChat", ClientInstance.this::onChatMsg);
        put("SetStatus", ClientInstance.this::onLobbyStatus);
        put("MapSelected", ClientInstance.this::onCourseSelect);
        put("PlayCard", ClientInstance.this::onCardPlay);
        put("SelectedCard", ClientInstance.this::onRegisterSlotUpdate);
        put("SetStartingPoint", ClientInstance.this::onStartingPointSet);
        put("PickDamage", ClientInstance.this::onDamageCardSelect);
        put("HelloServer", ClientInstance.this::onAddAgentRequest);
        put("RebootDirection", ClientInstance.this::onRebootDirection);
    }};

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

    private RDefaultClientRequestParser dcrp;

    public ClientInstance(final Socket socket) throws IOException
    {
        super();

        this.thread = null;
        this.socket = socket;
        this.inputStreamReader = new InputStreamReader(this.socket.getInputStream());
        this.outputStreamWriter = new OutputStreamWriter(this.socket.getOutputStream());
        this.bufferedReader = new BufferedReader(this.inputStreamReader);
        this.bufferedWriter = new BufferedWriter(this.outputStreamWriter);

        this.playerController = null;
        this.bIsRegistered = false;
        this.bIsAlive = true;
        this.bDisconnecting = false;

        this.dcrp = null;

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
     * after, use the {@link #defaultClientListener()} method.
     */
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

    // region Client Request Handlers

    private boolean onAlive()
    {
        l.trace("Received keep-alive from client {}. Ok.", this.getAddr());
        this.setAlive(true);
        return true;
    }

    private boolean onCorePlayerAttributesChanged()
    {
        final String oName = this.playerController.getName();

        /* TODO We have to do some validation here. */
        /* TODO Check if the client name is valid. It must not start with "[BOT]" or be empty etc. */

        this.playerController.setPlayerName(this.dcrp.getPlayerName());
        this.playerController.setFigure(this.dcrp.getFigureID());
        l.debug("Client {} selected figure {}.", this.getAddr(), this.playerController.getFigure());

        this.playerController.getSession().sendPlayerValuesToAllClients(this.playerController);
        if (!Objects.equals(oName, this.playerController.getName()))
        {
            this.playerController.getSession().broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s changed their name to %s.", oName, this.playerController.getName()));
            l.debug("Client {} changed their name from {} to {}.", this.getAddr(), oName, this.playerController.getName());
        }

        return true;
    }

    private boolean onChatMsg()
    {
        l.debug("Client {} wants to send chat message [{}] to lobby {}.", this.getAddr(), dcrp.getChatMessage_v2(), this.getPlayerController().getSession().getSessionID());

        // TODO Validate chat message.

        this.playerController.getSession().handleChatMessage(this.playerController, dcrp.getChatMessage_v2(), dcrp.getReceiverID());

        return true;
    }

    private boolean onLobbyStatus()
    {
        l.debug("Client {} set ready status to {}.", this.getAddr(), this.dcrp.getIsReadyInLobby());
        this.playerController.getSession().handlePlayerReadyStatus(this.playerController, this.dcrp.getIsReadyInLobby());
        return true;
    }

    private boolean onCourseSelect()
    {
        l.debug("Client {} selected course {}.", this.getAddr(), this.dcrp.getCourseName());
        this.playerController.getSession().handleSelectCourseName(this.playerController, this.dcrp.getCourseName());
        return true;
    }

    /** TODO What is the purpose of this req? */
    private boolean onCardPlay()
    {
        l.debug("Received play Card from client.");
        return true;
    }

    private boolean onRegisterSlotUpdate()
    {
        l.debug("Client {} selected card [{}] to register {}.", this.getAddr(), this.dcrp.getBody().isNull("card") ? null : this.dcrp.getSelectedCardAsString(), this.dcrp.getSelectedCardRegister());
        this.playerController.setSelectedCardInRegister(this.dcrp.getBody().isNull("card") ? null : this.dcrp.getSelectedCardAsString(), this.dcrp.getSelectedCardRegister());
        return true;
    }

    private boolean onStartingPointSet()
    {
        l.debug("Client {} set their starting point to ({},{})", this.getAddr(), this.dcrp.getXCoordinate(), this.dcrp.getYCoordinate());
        this.playerController.getSession().getGameState().setStartingPoint(playerController, this.dcrp.getXCoordinate(), this.dcrp.getYCoordinate());
        return true;
    }

    private boolean onDamageCardSelect()
    {
        l.debug("Received a picked damage card from client.");
        return true;
    }

    public boolean onAddAgentRequest()
    {
        // We may ignore all body arguments because we do not need them.
        // If there is something fishy going on, the initial client connection already would have failed.
        l.debug("Client {} wants to add an agent to lobby {}.", this.getAddr(), this.getPlayerController().getSession().getSessionID());
        this.getPlayerController().getSession().addAgent();
        return true;
    }

    private boolean onRebootDirection() {
        l.debug("Client {} set their reboot direction to {}", this.getAddr(), this.dcrp.getDirection());
        this.playerController.getSession().getGameState().setRebootDirection(playerController, this.dcrp.getDirection());
        return true;
    }

    // endregion Client Request Handlers

    private void parseRequest(RDefaultClientRequestParser dcrp) throws JSONException
    {
        this.dcrp = dcrp;

        if (this.clientReq.containsKey(this.dcrp.getType_v2()))
        {
            if (this.clientReq.get(this.dcrp.getType_v2()).get())
            {
                this.dcrp = null;
                return;
            }

            this.dcrp = null;
            throw new JSONException("Hit a wall while trying to understand the server request.");
        }

        l.warn("Received unknown request from server. Ignoring.");
        l.warn(this.dcrp.request().toString(0));

        this.dcrp = null;

        return;
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

            final String r = String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine());
            l.trace("Received request from client {}. Parsing: {}", this.getAddr(), r);

            try
            {
                this.parseRequest(new RDefaultClientRequestParser(new JSONObject(r)));
            }
            catch (JSONException e)
            {
                l.warn("Received invalid JSON from client {}. Ignoring.", this.getAddr());
                l.warn(e.getMessage());
                l.warn(r);
                continue;
            }

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
        if (!j.toString(0).contains("\"messageType\":\"Alive\""))
        {
            l.trace("Sending remote request to client {}. {}", this.getAddr(), j.toString(0));
        }

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

            this.playerController.getSession().onPostJoin(this.playerController);

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
