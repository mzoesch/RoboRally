package sep.view.clientcontroller;

import sep.view.lib.                EGamePhase;
import sep.view.lib.                RPopUpMask;
import sep.view.lib.                EPopUp;
import sep.view.viewcontroller.     ViewSupervisor;
import sep.view.viewcontroller.     SceneController;

import org.json.                    JSONException;
import java.util.stream.            Collectors;
import java.util.stream.            IntStream;
import java.io.                     BufferedReader;
import java.util.                   Arrays;
import java.util.                   Objects;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/**
 * We create a special object for listening to the server socket on a separate
 * thread to avoid blocking the main thread of the application.
 * {@inheritDoc}
 */
public final class HumanSL extends ServerListener
{
    private static final Logger l = LogManager.getLogger(HumanSL.class);

    public HumanSL(final BufferedReader br)
    {
        super(br);
        return;
    }

    // region Server request handlers

    @Override
    protected boolean onCorePlayerAttributesChanged() throws JSONException
    {
        l.debug("Player {}'s core attributes have changed. Updating.", this.dsrp.getPlayerID());
        EGameState.addRemotePlayer(this.dsrp);
        ViewSupervisor.updatePlayerSelection();

        return true;
    }

    @Override
    protected boolean onChatMsg() throws JSONException
    {
        l.debug("New chat message received: [{}] from {}.", this.dsrp.getChatMsg(), this.dsrp.getChatMsgSourceID());
        ViewSupervisor.handleChatMessage(this.dsrp);
        return true;
    }

    @Override
    protected boolean onLobbyPlayerStatus() throws JSONException
    {
        l.debug("Received player status update. Client {} is ready: {}.", this.dsrp.getPlayerID(), this.dsrp.isLobbyPlayerStatusReady());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setReady(this.dsrp.isLobbyPlayerStatusReady());
        ViewSupervisor.updatePlayerStatus();
        return true;
    }

    @Override
    protected boolean onSelectMapRequest() throws JSONException
    {
        l.debug("Server requested client {} to choose a course. Available courses: {}.", EClientInformation.INSTANCE.getPlayerID(), String.join(", ", Arrays.asList(this.dsrp.getAvailableCourses())));
        EGameState.INSTANCE.setServerCourses(this.dsrp.getAvailableCourses());
        ViewSupervisor.updateAvailableCourses(true);
        return true;
    }

    @Override
    protected boolean onMapSelected() throws JSONException
    {
        l.debug("Current session course update: {}.", this.dsrp.getCourseName() == null || this.dsrp.getCourseName().isEmpty() ? "none" : this.dsrp.getCourseName());
        EGameState.INSTANCE.setCurrentServerCourse(this.dsrp.getCourseName());
        ViewSupervisor.updateCourseSelected();
        return true;
    }

    @Override
    protected boolean onGameStart() throws JSONException
    {
        l.debug("Game has started. Loading game scene . . .");
        ViewSupervisor.startGameLater(this.dsrp.getGameCourse());
        return true;
    }

    @Override
    protected boolean onPhaseChange() throws JSONException
    {
        l.debug("Game phase has changed. New phase: {}.", EGamePhase.fromInt(this.dsrp.getPhase()));

        EGameState.INSTANCE.setCurrentPhase(EGamePhase.fromInt(this.dsrp.getPhase()));

        if (!ViewSupervisor.hasLoadedGameScene())
        {
            l.warn("Game phase changed, but the game scene has not been loaded yet. Ignoring.");
            return false;
        }

        /* TODO Remove if upgrade phase is playable. */
        if (this.dsrp.getPhase() != 1)
        {
            ViewSupervisor.createPhaseUpdatePopUpLater(EGamePhase.fromInt(this.dsrp.getPhase()));
        }

        return true;
    }

    @Override
    protected boolean onPlayerTurnChange() throws JSONException
    {
        l.debug("It is now player {}'s turn.", this.dsrp.getPlayerID());
        ViewSupervisor.handleChatInfo(String.format("Player %s is now current Player.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));

        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.REGISTRATION)
        {
            if (this.dsrp.getPlayerID() == Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getPlayerID())
            {
                EGameState.INSTANCE.setCurrentPlayer(this.dsrp.getPlayerID(), true);
                ViewSupervisor.updateCourseView();

                return true;
            }

            EGameState.INSTANCE.setCurrentPlayer(this.dsrp.getPlayerID(), false);
            return true;
        }

        EGameState.INSTANCE.setCurrentPlayer(this.dsrp.getPlayerID(), false);

        return true;
    }

    @Override
    protected boolean onErrorMsg() throws JSONException
    {
        l.debug("The server run into an error. Message: {}.", this.dsrp.getErrorMessage());
        ViewSupervisor.createPopUpLater(new RPopUpMask(EPopUp.ERROR, this.dsrp.getErrorMessage()));
        return true;
    }

    /** TODO For what reason does this exists in the protocol??? */
    @Override
    protected boolean onCardPlayed() throws JSONException
    {
        l.debug("Player {} has played {}.", this.dsrp.getPlayerID(), this.dsrp.getCardName());
        ViewSupervisor.handleChatInfo(String.format("Player %s has played %s.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getCardName()));
        return true;
    }

    @Override
    protected boolean onStartingPointTaken() throws JSONException
    {
        /* Very sketchy, but we get somehow a race condition even though we use the run later methods of the Platform.  */
        try
        {
            // Get random number between 200 and 500
            final int sleepTime = (int) (Math.random() * 300) + 200;
            l.warn("Waiting {} ms for game to load scene.", sleepTime);
            Thread.sleep(sleepTime);
        }
        catch (final InterruptedException e)
        {
            l.fatal("Failed to wait for game to load scene.");
            throw new RuntimeException(e);
        }

        l.debug("Player {} took starting point {}.", this.dsrp.getPlayerID(), this.dsrp.getCoordinate().toString());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setStartingPosition(this.dsrp.getCoordinate());
        ViewSupervisor.handleChatInfo(String.format("Player %s has selected a starting Point.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));

        /* We must discard the hover effects. */
        if (this.dsrp.getPlayerID() == Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getPlayerID())
        {
            ViewSupervisor.updateCourseView();
        }
        else
        {
            ViewSupervisor.updatePlayerTransforms();
        }

        return true;
    }

    @Override
    protected boolean onRobotRotationUpdate() throws JSONException
    {
        l.debug("Player {} has rotated {}.", this.dsrp.getPlayerID(), this.dsrp.getRotation());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getRobotView().addRotationWithLerp(this.dsrp.getRotation());
        return true;
    }

    @Override
    protected boolean onRegisterSlotUpdate() throws JSONException
    {
        // TODO We want to update the UI-Footer with this method call. Check if the playerID is the local player.
        //      Currently we update regardless if the select card action was affirmed by the server.
        l.debug("Player {} has updated their register {}. Filled: {}.", this.dsrp.getPlayerID(), this.dsrp.getRegister(), this.dsrp.getRegisterFilled() ? "true" : "false");
        return true;
    }

    @Override
    protected boolean onPlayerFinishedProgramming() throws JSONException
    {
        l.debug("Player {} has finished programming.", this.dsrp.getPlayerID());
        EGameState.INSTANCE.setSelectionFinished(this.dsrp.getPlayerID());
        ViewSupervisor.handleChatInfo(String.format("Player %s has finished his card selection.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));
        return true;
    }

    @Override
    protected boolean onForcedFinishProgramming() throws JSONException
    {
        l.debug("Player {} has been forced to finish programming because they did not submit their selection in time. New cards: {}", this.dsrp.getPlayerID(), String.join(", ", Arrays.asList(this.dsrp.getForcedCards())));
        EGameState.INSTANCE.setSelectionFinished(this.dsrp.getPlayerID());
        EGameState.INSTANCE.clearGotRegisters();
        for (String c : this.dsrp.getForcedCards())
        {
            for (int i = 0; i < EGameState.INSTANCE.getRegisters().length; i++)
            {
                if (EGameState.INSTANCE.getRegisters()[i] == null)
                {
                    EGameState.INSTANCE.addRegister(i, c);
                    break;
                }

                continue;
            }

            continue;
        }
        ViewSupervisor.updateFooter();
        ViewSupervisor.handleChatInfo("You did not submit your cards in time. Empty registers are being filled up.");
        return true;
    }

    @Override
    protected boolean onPlayerProgrammingCardsReceived() throws JSONException
    {
        l.debug("Player {} has received their programming cards ({}).", this.dsrp.getPlayerID(), this.dsrp.getCardsInHandCountNYC());
        ViewSupervisor.handleChatInfo(String.format("Player %s has received %s cards in his hand.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getCardsInHandCountNYC()));
        return true;
    }

    @Override
    protected boolean onProgrammingDeckShuffled() throws JSONException
    {
        l.debug("The programming deck of player {} has been shuffled.", this.dsrp.getPlayerID());
        ViewSupervisor.handleChatInfo(String.format("The deck of player %s has been shuffled.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));
        return true;
    }

    @Override
    protected boolean onProgrammingTimerStart() throws JSONException
    {
        /* TODO Implement timer. */
        l.debug("Programming phase timer has started.");
        ViewSupervisor.handleChatInfo("The programming phase timer has started. Submit your cards in time!");
        return true;
    }

    @Override
    protected boolean onProgrammingTimerEnd() throws JSONException
    {
        /* TODO Implement timer. */
        l.debug("Programming phase timer has ended.");
        ViewSupervisor.handleChatInfo("The programming phase timer has ended.");
        return true;
    }

    @Override
    protected boolean onProgrammingCardsReceived() throws JSONException
    {
        l.debug("Received nine new programming cards from server: {}", String.join(", ", Arrays.asList(this.dsrp.getCardsInHand())));
        EGameState.INSTANCE.clearAllRegisters();
        for (String c : this.dsrp.getCardsInHand())
        {
            EGameState.INSTANCE.addGotRegister(c);
            continue;
        }
        ViewSupervisor.updateFooter();
        return true;
    }

    @Override
    protected boolean onCurrentRegisterCards() throws JSONException
    {
        final StringBuilder sb = new StringBuilder();
        sb.append(String.format("In register %s these cards were played: (", EGameState.INSTANCE.getCurrentRegister()));
        sb.append(IntStream.range(0, this.dsrp.getActiveCards().length()).mapToObj(i -> String.format("%s[Player %d played card %s]", i == 0 ? "" : ", ", this.dsrp.getPlayerIDFromActiveCardIdx(i), this.dsrp.getActiveCardFromIdx(i))).collect(Collectors.joining()));
        sb.append(").");
        l.debug(sb.toString());
        ViewSupervisor.handleChatInfo(sb.toString());
        EGameState.INSTANCE.setCurrentRegister(EGameState.INSTANCE.getCurrentRegister() + 1);
        EGameState.INSTANCE.addRCardsToRemotes(this.dsrp.getCurrentRegisterCards());
        ViewSupervisor.updatePlayerInformationArea();
        return true;
    }

    @Override
    protected boolean onCurrentRegisterCardReplacement() throws JSONException
    {
        if (this.dsrp == null)
        {
            return false;
        }

        l.debug("Player {} has received a new card {} as a replacement for their current register phase card.", this.dsrp.getPlayerID(), this.dsrp.getNewCard());
        if (this.dsrp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
        {
            String info = String.format("You received the following card %s as a replacement.", this.dsrp.getNewCard());
            ViewSupervisor.handleChatInfo(info);
            EGameState.INSTANCE.addRegister(this.dsrp.getRegister(), this.dsrp.getNewCard());
            return true;
        }

        if ((EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())) != null) {
            String info = String.format("Player %s received following card %s as replacement.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getNewCard());
            ViewSupervisor.handleChatInfo(info);
        }

        return true;
    }

    @Override
    protected boolean onAnimationPlay() throws JSONException
    {
        l.debug("Server requested client {} to play an animation: {}.", EClientInformation.INSTANCE.getPlayerID(), this.dsrp.getAnimation().toString());
        ViewSupervisor.playAnimation(this.dsrp.getAnimation());
        return true;
    }

    @Override
    protected boolean onCheckpointReached() throws JSONException
    {
        l.debug("Player {} has reached {} checkpoints.", this.dsrp.getPlayerID(), this.dsrp.getNumber());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setCheckPointsReached(this.dsrp.getNumber());
        ViewSupervisor.handleChatInfo(String.format("Player %s has reached %s checkpoints.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getNumber()));
        return true;
    }

    @Override
    protected boolean onEnergyTokenChanged() throws JSONException
    {
        l.debug("Player {}'s energy amount has been updated to {}.", this.dsrp.getPlayerID(), this.dsrp.getEnergyCount());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setEnergy(this.dsrp.getEnergyCount());
        ViewSupervisor.updatePlayerView();
        return true;
    }

    @Override
    protected boolean onGameEnd() throws JSONException
    {
        l.debug("Game has ended. The winner is player {}.", this.dsrp.getWinningPlayer());
        EGameState.INSTANCE.determineWinningPlayer(this.dsrp.getWinningPlayer());
        ViewSupervisor.getSceneController().renderNewScreen(SceneController.END_SCENE_ID, SceneController.PATH_TO_END_SCENE, true);
        return true;
    }

    @Override
    protected boolean onPlayerPositionUpdate() throws JSONException
    {
        l.debug("Player {} has moved to {}.", this.dsrp.getPlayerID(), this.dsrp.getCoordinate().toString());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getRobotView().lerpTo(this.dsrp.getCoordinate());
        return true;
    }

    @Override
    protected boolean onPlayerReboot() throws JSONException
    {
        l.debug("Player {} has been rebooted.", this.dsrp.getPlayerID());

        if (this.dsrp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
        {
            l.debug("Local player has been rebooted. Showing reboot dialog.");
            ViewSupervisor.handleChatInfo("You were rebooted. Choose your reboot direction.");
            ViewSupervisor.createRebootDialogLater();
        }
        else
        {
            ViewSupervisor.handleChatInfo(String.format("Player %s was rebooted.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));
        }

        return true;
    }

    @Override
    protected boolean onClientConnectionUpdate() throws JSONException
    {
        l.debug("Client {}'s net connection status was updated. Client is connected: {}; Taking action: {}.", this.dsrp.getPlayerID(), this.dsrp.getIsConnected(), this.dsrp.getNetAction().toString());
        if (Objects.requireNonNull(this.dsrp.getNetAction()) == EConnectionLoss.REMOVE)
        {
            EGameState.INSTANCE.removeRemotePlayer(this.dsrp.getPlayerID());
            return true;
        }

        l.error("Received net action {}, but the client could not understand it. Ignoring.", this.dsrp.getNetAction().toString());
        return false;
    }

    @Override
    protected boolean onPickDamageType() throws JSONException
    {
        final String s = String.format("You have to pick %s damage cards. Available piles are: (%s).", this.dsrp.getDamageCardsCountToDraw(), String.join(", ", Arrays.asList(this.dsrp.getAvailableDamagePilesToDraw())));
        l.debug("Client has to pick damageCards. Showing selection dialog");
        ViewSupervisor.handleChatInfo(s);
        ViewSupervisor.createDamageCardSelectionDialogLater(this.dsrp.getAvailableDamagePilesToDraw(),this.dsrp.getDamageCardsCountToDraw());
        return true;
    }

    @Override
    protected boolean onDrawDamage() throws JSONException
    {
        final String i = String.format("Player %s has drawn the following damage cards: %s.", EClientInformation.INSTANCE.getPlayerID(), String.join(", ", Arrays.asList(this.dsrp.getDrawnDamageCards())));
        l.debug(i);
        ViewSupervisor.handleChatInfo(i);
        ViewSupervisor.createDrawDamagePopUpLater(String.join(", ", Arrays.asList(this.dsrp.getDrawnDamageCards())));
        return true;
    }

    @Override
    protected boolean onExchangeShop() throws JSONException
    {
        l.debug("Received exchange shop from server.");
        return true;
    }

    @Override
    protected boolean onRefillShop() throws JSONException
    {
        l.debug("Received refill shop from server.");
        return true;
    }

    @Override
    protected boolean onUpgradeBought() throws JSONException
    {
        l.debug("Received upgrade bought from server.");
        return true;
    }

    @Override
    protected boolean onCheckpointMoved() throws JSONException
    {
        l.debug("Received checkpoint moved from server.");
        return true;
    }

    @Override
    protected boolean onDiscardSome() throws JSONException
    {
        l.debug("Received discard some cards from server.");
        return true;
    }

    @Override
    protected boolean onRegisterChosen() throws JSONException
    {
        l.debug("Received register chosen from server.");
        return true;
    }

    // endregion Server request handlers

}
