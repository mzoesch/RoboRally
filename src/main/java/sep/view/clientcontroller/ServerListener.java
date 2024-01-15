package sep.view.clientcontroller;

import sep.view.json.               RDefaultServerRequestParser;
import sep.view.viewcontroller.     ViewSupervisor;
import sep.view.viewcontroller.     SceneController;
import sep.view.lib.                RPopUpMask;
import sep.view.lib.                EPopUp;

import java.io.                     IOException;
import java.io.                     BufferedReader;
import java.util.                   HashMap;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.util.function.          Supplier;
import org.json.                    JSONException;
import org.json.                    JSONObject;

/** Default way to handle server requests after the initial client registration handshake. */
public sealed abstract class ServerListener implements Runnable permits AgentSL, HumanSL, AgentSL_v2
{
    private static final Logger l = LogManager.getLogger(ServerListener.class);

    private static final int ORDERLY_CLOSE                      = -1;

    private final HashMap<String, Supplier<Boolean>> serverReq  =
    new HashMap<String, Supplier<Boolean>>()
    {{
        put(    "Alive",                ServerListener.this::onAlive                            );
        put(    "PlayerAdded",          ServerListener.this::onCorePlayerAttributesChanged      );
        put(    "ReceivedChat",         ServerListener.this::onChatMsg                          );
        put(    "PlayerStatus",         ServerListener.this::onLobbyPlayerStatus                );
        put(    "SelectMap",            ServerListener.this::onSelectMapRequest                 );
        put(    "MapSelected",          ServerListener.this::onMapSelected                      );
        put(    "GameStarted",          ServerListener.this::onGameStart                        );
        put(    "ActivePhase",          ServerListener.this::onPhaseChange                      );
        put(    "CurrentPlayer",        ServerListener.this::onPlayerTurnChange                 );
        put(    "Error",                ServerListener.this::onErrorMsg                         );
        put(    "CardPlayed",           ServerListener.this::onCardPlayed                       );
        put(    "StartingPointTaken",   ServerListener.this::onStartingPointTaken               );
        put(    "PlayerTurning",        ServerListener.this::onRobotRotationUpdate              );
        put(    "CardSelected",         ServerListener.this::onRegisterSlotUpdate               );
        put(    "SelectionFinished",    ServerListener.this::onPlayerFinishedProgramming        );
        put(    "CardsYouGotNow",       ServerListener.this::onForcedFinishProgramming          );
        put(    "NotYourCards",         ServerListener.this::onPlayerProgrammingCardsReceived   );
        put(    "ShuffleCoding",        ServerListener.this::onProgrammingDeckShuffled          );
        put(    "TimerStarted",         ServerListener.this::onProgrammingTimerStart            );
        put(    "TimerEnded",           ServerListener.this::onProgrammingTimerEnd              );
        put(    "YourCards",            ServerListener.this::onProgrammingCardsReceived         );
        put(    "CurrentCards",         ServerListener.this::onCurrentRegisterCards             );
        put(    "ReplaceCard",          ServerListener.this::onCurrentRegisterCardReplacement   );
        put(    "Animation",            ServerListener.this::onAnimationPlay                    );
        put(    "CheckPointReached",    ServerListener.this::onCheckpointReached                );
        put(    "Energy",               ServerListener.this::onEnergyTokenChanged               );
        put(    "GameFinished",         ServerListener.this::onGameEnd                          );
        put(    "Movement",             ServerListener.this::onPlayerPositionUpdate             );
        put(    "Reboot",               ServerListener.this::onPlayerReboot                     );
        put(    "ConnectionUpdate",     ServerListener.this::onClientConnectionUpdate           );
        put(    "PickDamage",           ServerListener.this::onPickDamageType                   );
        put(    "DrawDamage",           ServerListener.this::onDrawDamage                       );
        put(    "ExchangeShop",         ServerListener.this::onExchangeShop                     );
        put(    "RefillShop",           ServerListener.this::onRefillShop                       );
        put(    "UpgradeBought",        ServerListener.this::onUpgradeBought                    );
        put(    "CheckpointMoved",      ServerListener.this::onCheckpointMoved                  );
        put(    "DiscardSome",          ServerListener.this::onDiscardSome                      );
        put(    "RegisterChosen",       ServerListener.this::onRegisterChosen                   );
    }}
    ;

    private final BufferedReader            br;

    protected RDefaultServerRequestParser   dsrp;

    public ServerListener(final BufferedReader br)
    {
        super();

        this.br     = br;
        this.dsrp   = null;

        return;
    }

    public static void closeSocket()
    {
        EClientInformation.INSTANCE.closeSocket();
        EClientInformation.INSTANCE.resetServerConnectionAfterDisconnect();

        /* We are never connected to a server in the lobby scene. */
        if (ViewSupervisor.getSceneController().getCurrentScreen().id().equals(SceneController.MAIN_MENU_ID))
        {
            return;
        }

        l.info("Client disconnected from server.");

        return;
    }

    @Override
    public void run() throws IllegalStateException
    {
        try
        {
            while (true)
            {
                final int escapeCharacter = this.br.read();
                if (escapeCharacter == ServerListener.ORDERLY_CLOSE)
                {
                    l.debug("Server request to close the server connection in an orderly way.");
                    GameInstance.handleServerDisconnect();
                    ViewSupervisor.getSceneController().renderExistingScreen(SceneController.MAIN_MENU_ID);
                    ViewSupervisor.createPopUpLater(new RPopUpMask(EPopUp.ERROR, "Server closed the connection."));
                    return;
                }

                final String r = String.format("%s%s", (char) escapeCharacter, this.br.readLine());
                l.trace("Received request from server: Parsing: {}", r);

                try
                {
                    this.parseRequest(new RDefaultServerRequestParser(new JSONObject(r)));
                }
                catch (final JSONException e)
                {
                    l.warn("Failed to parse JSON request from server. Ignoring.");
                    l.warn(e.getMessage());
                    l.warn(r);

                    continue;
                }

                continue;
            }
        }
        catch (final IOException e)
        {
            l.fatal("Failed to read from server.");
            l.fatal(e.getMessage());
            GameInstance.handleServerDisconnect();
            ViewSupervisor.getSceneController().renderExistingScreen(SceneController.MAIN_MENU_ID);
            ViewSupervisor.createPopUpLater(new RPopUpMask(EPopUp.ERROR, "Server closed the connection."));

            return;
        }
    }

    // region Server request handlers

    private boolean onAlive() throws JSONException
    {
        l.trace("Woken up by keep-alive. Responding. Ok.");

        try
        {
            GameInstance.respondToKeepAlive();
        }
        catch (final IOException e)
        {
            GameInstance.handleServerDisconnect();
            return false;
        }

        return true;
    }

    protected abstract boolean      onCorePlayerAttributesChanged()          throws JSONException;
    protected abstract boolean      onChatMsg()                              throws JSONException;
    protected abstract boolean      onLobbyPlayerStatus()                    throws JSONException;
    protected abstract boolean      onSelectMapRequest()                     throws JSONException;
    protected abstract boolean      onMapSelected()                          throws JSONException;
    protected abstract boolean      onGameStart()                            throws JSONException;
    protected abstract boolean      onPhaseChange()                          throws JSONException;
    protected abstract boolean      onPlayerTurnChange()                     throws JSONException;
    protected abstract boolean      onErrorMsg()                             throws JSONException;
    protected abstract boolean      onCardPlayed()                           throws JSONException;
    protected abstract boolean      onStartingPointTaken()                   throws JSONException;
    protected abstract boolean      onRobotRotationUpdate()                  throws JSONException;
    protected abstract boolean      onRegisterSlotUpdate()                   throws JSONException;
    protected abstract boolean      onPlayerFinishedProgramming()            throws JSONException;
    protected abstract boolean      onForcedFinishProgramming()              throws JSONException;
    protected abstract boolean      onPlayerProgrammingCardsReceived()       throws JSONException;
    protected abstract boolean      onProgrammingDeckShuffled()              throws JSONException;
    protected abstract boolean      onProgrammingTimerStart()                throws JSONException;
    protected abstract boolean      onProgrammingTimerEnd()                  throws JSONException;
    protected abstract boolean      onProgrammingCardsReceived()             throws JSONException;
    protected abstract boolean      onCurrentRegisterCards()                 throws JSONException;
    protected abstract boolean      onCurrentRegisterCardReplacement()       throws JSONException;
    protected abstract boolean      onAnimationPlay()                        throws JSONException;
    protected abstract boolean      onCheckpointReached()                    throws JSONException;
    protected abstract boolean      onEnergyTokenChanged()                   throws JSONException;
    protected abstract boolean      onGameEnd()                              throws JSONException;
    protected abstract boolean      onPlayerPositionUpdate()                 throws JSONException;
    protected abstract boolean      onPlayerReboot()                         throws JSONException;
    protected abstract boolean      onClientConnectionUpdate()               throws JSONException;
    protected abstract boolean      onPickDamageType()                       throws JSONException;
    protected abstract boolean      onDrawDamage()                           throws JSONException;
    protected abstract boolean      onExchangeShop()                         throws JSONException;
    protected abstract boolean      onRefillShop()                           throws JSONException;
    protected abstract boolean      onUpgradeBought()                        throws JSONException;
    protected abstract boolean      onCheckpointMoved()                      throws JSONException;
    protected abstract boolean      onDiscardSome()                          throws JSONException;
    protected abstract boolean      onRegisterChosen()                       throws JSONException;

    // endregion Server request handlers

    private void parseRequest(final RDefaultServerRequestParser dsrp) throws JSONException, IllegalStateException
    {
        this.dsrp = dsrp;

        if (this.serverReq.containsKey(this.dsrp.getType_v2()))
        {
            if (this.serverReq.get(this.dsrp.getType_v2()).get())
            {
                this.dsrp = null;
                return;
            }

            this.dsrp = null;
            throw new JSONException("Hit a wall while trying to understand the server request.");
        }

        l.warn("Received unknown request from server. Ignoring.");
        l.warn(this.dsrp.request().toString(0));

        this.dsrp = null;

        return;
    }

}
