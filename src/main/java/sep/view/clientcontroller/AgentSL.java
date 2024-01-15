package sep.view.clientcontroller;

import org.json.JSONException;

import java.io.                     BufferedReader;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/** @deprecated  */
public final class AgentSL extends ServerListener
{
    private static final Logger l = LogManager.getLogger();

    public AgentSL(final BufferedReader br)
    {
        super(br);
        return;
    }

    // region Server response handlers

    @Override
    protected boolean onCorePlayerAttributesChanged() throws JSONException
    {
        /* Ignored on purpose. */
        return false;
    }

    @Override
    protected boolean onChatMsg() throws JSONException
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onLobbyPlayerStatus() throws JSONException
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onSelectMapRequest() throws JSONException
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onMapSelected() throws JSONException
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onGameStart() throws JSONException
    {
        return false;
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
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onForcedFinishProgramming() throws JSONException
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onPlayerProgrammingCardsReceived() throws JSONException
    {
        return false;
    }

    @Override
    protected boolean onProgrammingDeckShuffled() throws JSONException
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onProgrammingTimerStart() throws JSONException
    {
        /* Ignored on purpose. */
        return true;
    }

    @Override
    protected boolean onProgrammingTimerEnd() throws JSONException
    {
        /* Ignored on purpose. */
        return true;
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
        /* Ignored on purpose. */
        return true;
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

    // endregion Server response handlers

}
