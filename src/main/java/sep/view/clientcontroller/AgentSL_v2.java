package sep.view.clientcontroller;

import sep.view.lib.                EGamePhase;
import sep.view.viewcontroller.     ViewSupervisor;
import sep.view.viewcontroller.     SceneController;

import org.json.                    JSONException;
import java.util.stream.            Collectors;
import java.util.stream.            IntStream;
import java.io.                     BufferedReader;
import java.io.                     InputStreamReader;
import java.util.                   Arrays;
import java.util.                   Objects;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.net.                    Socket;

/**
 * We create a special object for listening to the server socket on a separate
 * thread to avoid blocking the main thread of the application.
 * {@inheritDoc}
 */
public final class AgentSL_v2 extends ServerListener
{
    private static final Logger l = LogManager.getLogger(HumanSL.class);

    public AgentSL_v2(final BufferedReader br)
    {
        super(br);

        this.serverCourse = null;

        return;
    }

    // region Server request handlers

    @Override
    protected boolean onCorePlayerAttributesChanged() throws JSONException
    {
        return true;
    }

    @Override
    protected boolean onChatMsg() throws JSONException
    {
        return true;
    }

    @Override
    protected boolean onLobbyPlayerStatus() throws JSONException
    {
        return true;
    }

    @Override
    protected boolean onSelectMapRequest() throws JSONException
    {
        return true;
    }

    @Override
    protected boolean onMapSelected() throws JSONException
    {
        return true;
    }

    @Override
    protected boolean onGameStart() throws JSONException
    {
        return true;
    }

    @Override
    protected boolean onPhaseChange() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onPlayerTurnChange() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onErrorMsg() throws JSONException
    {
        l.error("Server sent an error message. Message: {}", this.dsrp.getErrorMessage());
        return false;
    }

    @Override
    protected boolean onCardPlayed() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onStartingPointTaken() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onRobotRotationUpdate() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onRegisterSlotUpdate() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onPlayerFinishedProgramming() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onForcedFinishProgramming() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onPlayerProgrammingCardsReceived() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onProgrammingDeckShuffled() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onProgrammingTimerStart() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onProgrammingTimerEnd() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onProgrammingCardsReceived() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onCurrentRegisterCards() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onCurrentRegisterCardReplacement() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onAnimationPlay() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onCheckpointReached() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onEnergyTokenChanged() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onGameEnd() throws JSONException
    {
        return true;
    }

    @Override
    protected boolean onPlayerPositionUpdate() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onPlayerReboot() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onClientConnectionUpdate() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onPickDamageType() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onDrawDamage() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onExchangeShop() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onRefillShop() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onUpgradeBought() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onCheckpointMoved() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onDiscardSome() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onRegisterChosen() throws JSONException
    {
        return false;
    }

    // endregion Server request handlers

}
