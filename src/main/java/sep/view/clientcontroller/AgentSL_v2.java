package sep.view.clientcontroller;

import sep.view.json.lobby.         PlayerValuesModel;
import sep.view.json.lobby.         SetStatusModel;

import org.json.                    JSONException;
import org.json.                    JSONObject;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     BufferedReader;
import java.util.                   Objects;

/**
 * We create a special object for listening to the server socket on a separate
 * thread to avoid blocking the main thread of the application.
 * {@inheritDoc}
 */
public final class AgentSL_v2 extends ServerListener
{
    private static final Logger l = LogManager.getLogger(AgentSL_v2.class);

    private JSONObject serverCourse;

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
        l.debug("Player {}'s core attributes have changed. Updating.", this.dsrp.getPlayerID());
        EGameState.addRemotePlayer(this.dsrp);
        return true;
    }

    @Override
    protected boolean onChatMsg()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onLobbyPlayerStatus()
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onSelectMapRequest() throws IllegalStateException
    {
        throw new IllegalStateException("AgentSL_v2.onSelectMapRequest() was called. The server should never ask an agent to select a map.");
    }

    @Override
    protected boolean onMapSelected() throws RuntimeException
    {
        if (this.dsrp.getCourseName() == null || this.dsrp.getCourseName().isEmpty())
        {
            return true;
        }

        l.info("Current session course updated to {}. Sending name and robot to server.", this.dsrp.getCourseName());
        EGameState.INSTANCE.setCurrentServerCourse(this.dsrp.getCourseName());
        new PlayerValuesModel(EClientInformation.INSTANCE.getPrefAgentName(), EClientInformation.INSTANCE.getPrefAgentRobot()).send();

        if (Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).isReady())
        {
            return true;
        }

        l.info("Sending Agent ready request to server.");
        new SetStatusModel(true).send();

        return true;
    }

    @Override
    protected boolean onGameStart() throws JSONException
    {
        l.debug("Game start received.");
        this.serverCourse = this.dsrp.request();
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
        l.info("Game ended. Killing the game instance.");
        GameInstance.kill();
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
