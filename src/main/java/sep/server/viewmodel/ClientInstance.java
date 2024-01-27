package sep.server.viewmodel;

import sep.server.json.common.      ChatMsgModel;
import sep.server.json.common.      KeepAliveModel;
import sep.server.json.common.      ErrorMsgModel;
import sep.server.json.             RDefaultClientRequestParser;
import sep.server.json.mainmenu.    InitialClientConnectionModel_v2;
import sep.server.model.            EServerInformation;

import java.util.                   Objects;
import java.util.                   HashMap;
import java.io.                     IOException;
import java.io.                     InputStreamReader;
import java.io.                     OutputStreamWriter;
import java.io.                     BufferedReader;
import java.io.                     BufferedWriter;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.net.                    SocketException;
import java.net.                    Socket;
import java.net.                    SocketTimeoutException;
import java.util.function.          Supplier;
import org.json.                    JSONObject;
import org.json.                    JSONException;
import java.util.concurrent.atomic. AtomicBoolean;

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

    private static final int ORDERLY_CLOSE                      = -1;
    private static final int SOCKET_OPERATION_TIMEOUT_INIT      = 1_000;
    /**
     * This is an additional timeout for socket operations. This is used to prevent the server from hanging
     * indefinitely if a client does not respond to a request. This is an additional hard timeout on top of the
     * keep-alive interval to prevent fishy behavior. If the keep-alive fails for whatever reason and the server
     * does not know about this, the server should always crash intentionally.
     */
    private static final int SOCKET_OPERATION_TIMOUT            = 15_000;

    private final HashMap<String, Supplier<Boolean>> clientReq =
    new HashMap<String, Supplier<Boolean>>() {{
        put(    "Alive",                ClientInstance.this::onAlive                        );
        put(    "PlayerValues",         ClientInstance.this::onCorePlayerAttributesChanged  );
        put(    "SendChat",             ClientInstance.this::onChatMsg                      );
        put(    "SetStatus",            ClientInstance.this::onLobbyStatus                  );
        put(    "MapSelected",          ClientInstance.this::onCourseSelect                 );
        put(    "PlayCard",             ClientInstance.this::onCardPlay                     );
        put(    "SelectedCard",         ClientInstance.this::onRegisterSlotUpdate           );
        put(    "SetStartingPoint",     ClientInstance.this::onStartPointSet                );
        put(    "PickDamage",           ClientInstance.this::onDamageCardSelect             );
        put(    "HelloServer",          ClientInstance.this::onAddAgentRequest              );
        put(    "RebootDirection",      ClientInstance.this::onRebootDirection              );
        put(    "BuyUpgrade",           ClientInstance.this::onBuyUpgrade                   );
        put(    "ChooseRegister",       ClientInstance.this::onChooseRegister               );
        put(    "DiscardSome",          ClientInstance.this::onDiscardSome                  );
    }};

    private Thread                          thread;
    private final Socket                    socket;
    private final BufferedReader            in;
    private final BufferedWriter            out;

    private boolean                         bRemoteAgent;
    /** Must be created to join a given session. */
    private PlayerController                playerController;
    /** If the client is registered in a session. */
    private boolean                         bRegistered;
    /** Used to keep track if a client responded to the keep-alive request form the server. */
    private final AtomicBoolean             bAlive              = new AtomicBoolean(false);
    /** If a client disconnects unexpectedly, we may try to disconnect them multiple times. This variable is used to keep track of that. */
    private final AtomicBoolean             bDisconnecting      = new AtomicBoolean(false);

    private RDefaultClientRequestParser     dcrp;

    public ClientInstance(final Socket socket) throws IOException
    {
        super();

        this.thread                 = null;
        this.socket                 = socket;
        this.socket                  .setSoTimeout(ClientInstance.SOCKET_OPERATION_TIMEOUT_INIT);

        this.in                     = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.out                    = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));

        this.bRemoteAgent           = false;
        this.playerController       = null;

        this.bRegistered            = false;
        this.bAlive                  .set(true);
        this.bDisconnecting          .set(false);

        this.dcrp                   = null;

        return;
    }

    public void disconnect()
    {
        if (this.bDisconnecting.get())
        {
            return;
        }
        this.bDisconnecting.set(true);

        l.debug("Client {} is disconnecting.", this.getAddr());

        if (this.bRegistered)
        {
            this.playerController.getSession().leaveSession(this.playerController);
        }

        this.thread.interrupt();

        try
        {
            this.socket.close();
        }
        catch (final IOException e)
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
     * @return              True if the client was successfully registered in a session, false otherwise.
     * @throws IOException  If the client connection was closed unexpectedly.
     */
    private boolean registerClient() throws IOException
    {
        final InitialClientConnectionModel_v2 icc = new InitialClientConnectionModel_v2(this);
        icc.sendProtocolVersion();

        icc.waitForProtocolVersionConfirmation();
        final boolean isValid   = icc.isClientProtocolVersionValid();
        if (!isValid)
        {
            l.error("Client {} protocol version mismatch. Disconnecting them.", this.getAddr());
            return false;
        }
        l.debug("Confirmed Client {}'s protocol version.", this.getAddr());

        /* TODO Check if session id is valid. */
        this.bRemoteAgent       = icc.isRemoteAgent();
        final Session s         = EServerInformation.INSTANCE.getNewOrExistingSessionID(icc.getSessionID());
        if (s.isFull())
        {
            new ErrorMsgModel(this, "Session is full.").send();
            l.warn("Client {} tried to join the full session [{}]. Disconnecting them.", this.getAddr(), s.getSessionID());
            return false;
        }
        if (s.hasStarted())
        {
            new ErrorMsgModel(this, "Session has already a game in progress.").send();
            l.warn("Client {} tried to join the session [{}] which has already a game in progress. Disconnecting them.", this.getAddr(), s.getSessionID());
            return false;
        }
        this.playerController   = EServerInstance.createNewPlayerController(this, s);
        icc.sendWelcome(this.playerController.getPlayerID());
        this.playerController.getSession().joinSession(this.playerController);
        this.bRegistered        = true;

        l.info("Client {} registered successfully in session [{}].", this.getAddr(), this.playerController.getSession().getSessionID());

        return true;
    }

    /**
     * Only used for the initial client registration as it block the calling thread.
     * The {@link #defaultClientListener()} method is used for receiving information from the client after that.
     */
    public String waitForResponse()
    {
        try
        {
            final int escapeCharacter = this.in.read();
            if (escapeCharacter == ClientInstance.ORDERLY_CLOSE)
            {
                this.disconnect();
                return null;
            }

            return String.format("%s%s", (char) escapeCharacter, this.in.readLine());
        }
        catch (final IOException e)
        {
            l.error("Failed to read from client {}. Disconnecting them.", this.getAddr());
            l.error(e.getMessage());
            return null;
        }
    }

    // region Client Request Handlers

    private synchronized boolean onAlive()
    {
        l.trace("Received keep-alive from client {}. Ok.", this.getAddr());
        this.setAlive(true);
        return true;
    }

    private synchronized boolean onCorePlayerAttributesChanged()
    {
        final String old = this.playerController.getName();

        /* TODO We have to do some validation here. */
        /* TODO Check if the client name is valid. It must not start with "[BOT]" or be empty etc. */

        this.playerController.setPlayerName(this.dcrp.getPlayerName());
        this.playerController.setFigure(this.dcrp.getFigureID());
        l.debug("Client {} selected figure {}.", this.getAddr(), this.playerController.getFigure());

        this.playerController.getSession().broadcastCorePlayerAttributes(this.playerController);
        if (!Objects.equals(old, this.playerController.getName()))
        {
            this.playerController.getSession().broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s changed their name to %s.", old, this.playerController.getName()));
            l.debug("Client {} changed their name from {} to {}.", this.getAddr(), old, this.playerController.getName());
        }

        return true;
    }

    private synchronized boolean onChatMsg()
    {
        l.debug("Client {} wants to send chat message [{}] to lobby {}.", this.getAddr(), this.dcrp.getChatMessage_v2(), this.getPlayerController().getSession().getSessionID());

        /* TODO Validate chat message. */

        this.playerController.getSession().parseAndExecuteChatMessage(this.playerController, this.dcrp.getChatMessage_v2(), this.dcrp.getReceiverID());

        return true;
    }

    private synchronized boolean onLobbyStatus()
    {
        l.debug("Client {} set ready status to {}.", this.getAddr(), this.dcrp.getIsReadyInLobby());
        this.playerController.getSession().onPlayerReadyStatusUpdate(this.playerController, this.dcrp.getIsReadyInLobby());
        return true;
    }

    private synchronized boolean onCourseSelect()
    {
        l.debug("Client {} selected course {}.", this.getAddr(), this.dcrp.getCourseName());
        this.playerController.getSession().onCourseSelect(this.playerController, this.dcrp.getCourseName());
        return true;
    }

    /** TODO What is the purpose of this req? */
    private synchronized boolean onCardPlay()
    {
        l.error("Received play Card from client: {}", this.dcrp.request().toString(0));
        return true;
    }

    private synchronized boolean onRegisterSlotUpdate()
    {
        l.debug("Client {} selected card [{}] to register {}.", this.getAddr(), this.dcrp.getBody().isNull("card") ? null : this.dcrp.getSelectedCardAsString(), this.dcrp.getSelectedCardRegister());
        this.playerController.setSelectedCardInRegister(this.dcrp.getBody().isNull("card") ? null : this.dcrp.getSelectedCardAsString(), this.dcrp.getSelectedCardRegister());
        return true;
    }

    private synchronized boolean onStartPointSet()
    {
        l.debug("Client {} set their starting point to {}", this.getAddr(), this.dcrp.getCoordinate().toString());
        this.playerController.getSession().getGameState().setStartPoint(this.playerController, this.dcrp.getCoordinate());
        return true;
    }

    private synchronized boolean onDamageCardSelect()
    {
        l.error("Received a picked damage card from client.");
        return true;
    }

    /** @deprecated */
    public synchronized boolean onAddAgentRequest()
    {
        // We may ignore all body arguments because we do not need them.
        // If there is something fishy going on, the initial client connection already would have failed.
        l.debug("Client {} wants to add an agent to lobby [{}].", this.getAddr(), this.getPlayerController().getSession().getSessionID());
        this.getPlayerController().getSession().addAgent();
        return true;
    }

    private synchronized boolean onRebootDirection()
    {
        l.debug("Client {} set their reboot direction to {}", this.getAddr(), this.dcrp.getDirection());
        this.playerController.getSession().getGameState().setRebootDirection(playerController, this.dcrp.getDirection());
        return true;
    }

    private synchronized boolean onBuyUpgrade()
    {
        if (!this.dcrp.hasBuyUpgradeCard())
        {
            l.debug("Client {} does not want to buy an upgrade card.", this.getPlayerController().getPlayerID());
            this.playerController.getSession().getGameState().getAuthGameMode().onUpgradeCardBought(this.playerController, null);
            return true;
        }

        l.debug("Client {} wants to buy the following Upgrade Card {}.", this.getPlayerController().getPlayerID(), this.dcrp.getBuyUpgradeCard());
        this.playerController.getSession().getGameState().getAuthGameMode().onUpgradeCardBought(this.playerController, this.dcrp.getBuyUpgradeCard());
        return true;
    }

    /*For the UpgradeCard AdminPriviledge*/
    private synchronized boolean onChooseRegister()
    {
        l.debug("Client {} choose the following Register {}", this.getPlayerController().getName(), this.dcrp.getChosenRegister());
        this.playerController.getSession().getGameState().setRegisterForAdminPriviledge(playerController, this.dcrp.getChosenRegister());
        return true;
    }

    /* For the UpgradeCard MemorySwap*/
    private synchronized boolean onDiscardSome()
    {
        l.debug("Client {} discard the following three Cards {}", this.getPlayerController().getName(), this.dcrp.getMemorySwapCard());
        this.playerController.getSession().getGameState().setMemorySwapCards(playerController, this.dcrp.getMemorySwapCard());
        return true;
    }

    // endregion Client Request Handlers

    private void parseRequest(final RDefaultClientRequestParser dcrp) throws JSONException
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
        this.socket.setSoTimeout(ClientInstance.SOCKET_OPERATION_TIMOUT);

        while (true)
        {
            final int escapeCharacter;
            try
            {
                // If the client closed the connection in an orderly way, the server will receive -1.
                escapeCharacter = this.in.read();
            }
            catch (final SocketException e)
            {
                l.warn("Client {}'s socket connection was closed unexpectedly.", this.getAddr());
                this.disconnect();
                return;
            }

            if (escapeCharacter == ClientInstance.ORDERLY_CLOSE)
            {
                l.debug("Client {} requested to close the server connection in an orderly way.", this.getAddr());
                this.disconnect();
                return;
            }

            final String r = String.format("%s%s", (char) escapeCharacter, this.in.readLine());
            l.trace("Received request from client {}. Parsing: {}", this.getAddr(), r);

            try
            {
                this.parseRequest(new RDefaultClientRequestParser(new JSONObject(r)));
            }
            catch (final JSONException e)
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
        this.bAlive.set(false);
        new KeepAliveModel(this).send();
        return;
    }

    public boolean sendRemoteRequest(final JSONObject j)
    {
        l.trace("Sending remote request to client {}: {}", this.getAddr(), j.toString(0));

        try
        {
            this.out.write(j.toString());
            this.out.newLine();
            this.out.flush();
        }
        catch (final IOException e)
        {
            l.error("Failed to send remote request to client {}.", this.getAddr());
            l.error(e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void run()
    {
        try
        {
            final boolean bSuccess = this.registerClient();

            if (!bSuccess)
            {
                this.disconnect();
                return;
            }

            this.playerController.getSession().onPostJoin(this.playerController);

            this.defaultClientListener();

            return;
        }
        catch (final SocketTimeoutException e)
        {
            if (EServerInstance.INSTANCE.getKeepAliveThread() == null)
            {
                l.fatal("Server failed. It was not notified about the keep-alive thread failure.");
                EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
                return;
            }

            l.fatal("Client {} exceeded a connection timeout even though the keep-alive was successful.", this.getAddr());
            EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);

            return;
        }
        catch (final IOException e)
        {
            l.error("Client {}'s socket connection was closed unexpectedly. Executing post disconnect logic.", this.getAddr());
            l.error(e.getMessage());
            this.disconnect();
            return;
        }
    }

    // region Getters and Setters

    public boolean isAlive()
    {
        return this.bAlive.get();
    }

    public void setAlive(final boolean bIsAlive)
    {
        this.bAlive.set(bIsAlive);
        return;
    }

    public PlayerController getPlayerController()
    {
        return this.playerController;
    }

    public String getAddr()
    {
        return String.format("[%s]", this.socket.getRemoteSocketAddress().toString());
    }

    public boolean isRemoteAgent()
    {
        return this.bRemoteAgent;
    }

    public void setThread(final Thread thread)
    {
        this.thread = thread;
        return;
    }

    // endregion Getters and Setters

}
