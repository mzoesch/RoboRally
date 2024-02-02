package sep.view.clientcontroller;

import sep.view.lib.                EGamePhase;
import sep.view.lib.                RCheckpointMask;
import sep.view.lib.                RPopUpMask;
import sep.view.lib.                EPopUp;
import sep.view.viewcontroller.     ViewSupervisor;
import sep.view.viewcontroller.     SceneController;

import org.json.                    JSONException;
import java.util.                   ArrayList;
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
        l.debug("Game phase has changed to: {}.", EGamePhase.fromInt(this.dsrp.getPhase()));

        EGameState.INSTANCE.setMemorySwapPlayed(false);
        EGameState.INSTANCE.setSpamBlockerPlayed(false);

        /* Because we can play this card both in the programming phase and the activation phase. */
        if (EGamePhase.fromInt(this.dsrp.getPhase()) == EGamePhase.PROGRAMMING)
        {
            EGameState.INSTANCE.setAdminPrivilegePlayed(false);
            EGameState.INSTANCE.setCurrentRegister(0);
        }

        EGameState.INSTANCE.setCurrentPhase(EGamePhase.fromInt(this.dsrp.getPhase()));
        EGameState.INSTANCE.setProgrammingTimerRunning(false);

        if (!ViewSupervisor.hasLoadedGameScene())
        {
            l.warn("Game phase changed, but the game scene has not been loaded yet. Ignoring.");
            return false;
        }

        ViewSupervisor.createPhaseUpdatePopUpLater(EGamePhase.fromInt(this.dsrp.getPhase()));

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

        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.UPGRADE)
        {
            if (this.dsrp.getPlayerID() == Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getPlayerID())
            {
                EGameState.INSTANCE.setCurrentPlayer(this.dsrp.getPlayerID(), true);
                ViewSupervisor.updateCourseView();
                ViewSupervisor.createShopDialogLater();

                return true;
            }

            EGameState.INSTANCE.setCurrentPlayer(this.dsrp.getPlayerID(), false);
            return true;
        }

        l.warn("Received player turn change, but the current phase is not registration or upgrade. Ignoring.");
        EGameState.INSTANCE.setCurrentPlayer(this.dsrp.getPlayerID(), false);

        return false;
    }

    @Override
    protected boolean onErrorMsg() throws JSONException
    {
        l.error("The server run into an error. Message: {}.", this.dsrp.getErrorMessage());
        ViewSupervisor.createPopUpLater(new RPopUpMask(EPopUp.ERROR, this.dsrp.getErrorMessage()));
        return true;
    }

    @Override
    protected boolean onCardPlayed() throws JSONException
    {
        l.info("Player {} has played card {}.", this.dsrp.getPlayerID(), this.dsrp.getCard());
        EGameState.INSTANCE.executePostCardPlayedBehaviour(this.dsrp.getPlayerID(), this.dsrp.getCard());
        return true;
    }

    @Override
    protected boolean onStartingPointTaken() throws JSONException
    {
        synchronized (ViewSupervisor.getLoadGameSceneLock())
        {
            while (!ViewSupervisor.isGameScenePostLoaded())
            {
                l.warn("A starting position taken request was issued, but the game scene has not been loaded yet. Thread is waiting to be notified.");

                try
                {
                    // Note, that this is the server listener thread. If the game scene is kinda laggy on slower
                    // end hardware, this might cause a server disconnect forced by the servers keep-alive service.
                    ViewSupervisor.getLoadGameSceneLock().wait();
                }
                catch (final InterruptedException e)
                {
                    l.fatal("Failed to wait for game to load scene.");
                    GameInstance.kill(GameInstance.EXIT_FATAL);
                    return false;
                }

                continue;
            }

            /* We can just exit because we are using the thread safe Run Later methods of the Platform. */
            ViewSupervisor.getLoadGameSceneLock().notifyAll();
        }

        l.debug("Player {} took starting point {}.", this.dsrp.getPlayerID(), this.dsrp.getCoordinate().toString());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setStartingPosition(this.dsrp.getCoordinate());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getRobotView().addRotation("startingDirection");
        ViewSupervisor.handleChatInfo(String.format("Player %s has selected a starting Point.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));

        if (EGameState.INSTANCE.getClientRemotePlayer() == null)
        {
            l.warn("Tried to update view for starting point, but the local player is not set yet (Player ID: {}).", EClientInformation.INSTANCE.getPlayerID());
            return false;
        }

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
        l.info("Player {} has been forced to finish programming because they did not submit their selection in time. Filling cards: {}", EClientInformation.INSTANCE.getPlayerID(), String.join(", ", Arrays.asList(this.dsrp.getForcedCards())));

        EGameState.INSTANCE.setSelectionFinished(EClientInformation.INSTANCE.getPlayerID());
        EGameState.INSTANCE.clearGotRegisters();

        cardSelection: for (final String c : this.dsrp.getForcedCards())
        {
            for (int i = 0; i < EGameState.INSTANCE.getRegisters().length; ++i)
            {
                if (EGameState.INSTANCE.getRegisters()[i] == null)
                {
                    EGameState.INSTANCE.addRegister(i, c);
                    l.debug("Added card {} to register {}.", c, i);
                    continue cardSelection;
                }

                continue;
            }

            l.error("Could not add card {} to any register. Ignoring.", c);

            continue;
        }

        ViewSupervisor.updateFooter();
        ViewSupervisor.handleChatInfo("You did not submit your cards in time. Empty registers are being filled up.");

        return true;
    }

    @Override
    protected boolean onPlayerProgrammingCardsReceived() throws JSONException
    {
        /* TODO What should we do with this information? */
        l.trace("Player {} has received their programming cards ({}).", this.dsrp.getPlayerID(), this.dsrp.getCardsInHandCountNYC());
        ViewSupervisor.handleChatInfo(String.format("Player %s has received %s cards in his hand.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getCardsInHandCountNYC()));
        return true;
    }

    @Override
    protected boolean onProgrammingDeckShuffled() throws JSONException
    {
        l.trace("The programming deck of player {} has been shuffled.", this.dsrp.getPlayerID());
        ViewSupervisor.handleChatInfo(String.format("The deck of player %s has been shuffled.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));
        return true;
    }

    @Override
    protected boolean onProgrammingTimerStart() throws JSONException
    {
        l.info("Programming phase timer has started.");
        ViewSupervisor.handleChatInfo("The programming phase timer has started. Submit your cards in time!");
        EGameState.INSTANCE.setProgrammingTimerRunning(true);
        ViewSupervisor.updatePlayerInformationArea();
        return true;
    }

    @Override
    protected boolean onProgrammingTimerEnd() throws JSONException
    {
        l.info("Programming phase timer has ended. Forced to finish programming clients: {}.", this.dsrp.getForcedFinishedProgrammingClients());
        ViewSupervisor.handleChatInfo("The programming phase timer has ended.");
        EGameState.INSTANCE.setProgrammingTimerRunning(false);
        ViewSupervisor.updatePlayerInformationArea();
        return true;
    }

    @Override
    protected boolean onProgrammingCardsReceived() throws JSONException
    {
        l.debug("Received {} new programming cards from server: {}.", this.dsrp.getCardsInHand().length, String.join(", ", Arrays.asList(this.dsrp.getCardsInHand())));

        if (EGameState.INSTANCE.isMemorySwapPlayed())
        {
            ViewSupervisor.onMemoryCardsReceived(this.dsrp.getCardsInHand());
            return true;
        }

        if (EGameState.INSTANCE.isSpamBlockerPlayed())
        {
            ViewSupervisor.onSpamBlockerCardsReceived(this.dsrp.getCardsInHand());
            return true;
        }

        EGameState.INSTANCE.clearAllRegisters();
        for (final String c : this.dsrp.getCardsInHand())
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
        l.debug("Player {} has reached {} checkpoints.", this.dsrp.getPlayerID(), this.dsrp.getCheckpointNumber());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setCheckPointsReached(this.dsrp.getCheckpointNumber());
        ViewSupervisor.handleChatInfo(String.format("Player %s has reached %s checkpoints.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getCheckpointNumber()));
        ViewSupervisor.updatePlayerInformationArea();
        return true;
    }

    @Override
    protected boolean onEnergyTokenChanged() throws JSONException
    {
        l.debug("Player {}'s energy amount has been updated to {}. Source: {}.", this.dsrp.getPlayerID(), this.dsrp.getEnergyCount(), this.dsrp.getEnergySource());
        final int deprecatedEnergy = Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getEnergy();
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setEnergy(this.dsrp.getEnergyCount());
        ViewSupervisor.updatePlayerView();

        /* If a client bought an upgrade card, for example. */
        if (deprecatedEnergy - this.dsrp.getEnergyCount() >= 0)
        {
            return true;
        }

        if (this.dsrp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
        {
            ViewSupervisor.createEnergyTokenPopUpLater(this.dsrp.getEnergyCount() - deprecatedEnergy, this.dsrp.getEnergySource());
        }

        return true;
    }

    @Override
    protected boolean onGameEnd() throws JSONException
    {
        l.debug("Game has ended. The winner is player {}.", this.dsrp.getWinningPlayer());
        EGameState.INSTANCE.setWinner(this.dsrp.getWinningPlayer());
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

        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setHasRebooted(true);

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
        l.debug("Upgrade shop was exchanged with the following cards: {}.", String.join(", ", Arrays.asList(this.dsrp.getExchangeShopCards())));
        EGameState.INSTANCE.exchangeShop(new ArrayList<String>(Arrays.asList(this.dsrp.getExchangeShopCards())));
        return true;
    }

    @Override
    protected boolean onRefillShop() throws JSONException
    {
        l.debug("Upgrade shop was refilled with the following cards: {}.", String.join(", ", Arrays.asList(this.dsrp.getRefillShopCards())));
        EGameState.INSTANCE.refillShop(new ArrayList<String>(Arrays.asList(this.dsrp.getRefillShopCards())));
        return true;
    }

    @Override
    protected boolean onUpgradeBought() throws JSONException
    {
        l.debug("Client {} has bought the following upgrade card: {}.", this.dsrp.getPlayerID(), this.dsrp.getCard());
        EGameState.INSTANCE.onUpgradeCardBought(this.dsrp.getPlayerID(), this.dsrp.getCard());
        ViewSupervisor.handleChatInfo(String.format("Player %s has bought the following upgrade card: %s.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getCard()));
        return true;
    }

    @Override
    protected boolean onCheckpointMoved() throws JSONException
    {
        l.debug("Checkpoint {} has moved to {}.", this.dsrp.getCheckpointMovedID(), this.dsrp.getCoordinate().toString());

        for (int i = 0; i < EGameState.INSTANCE.getCurrentCheckpointLocations().size(); ++i)
        {
            if (EGameState.INSTANCE.getCurrentCheckpointLocations().get(i) == null)
            {
                continue;
            }

            if (EGameState.INSTANCE.getCurrentCheckpointLocations().get(i).id() == this.dsrp.getCheckpointMovedID())
            {
                EGameState.INSTANCE.getCurrentCheckpointLocations().set(i, new RCheckpointMask(this.dsrp.getCoordinate(), this.dsrp.getCheckpointMovedID(), true));
                ViewSupervisor.updateCheckpoints();
                return true;
            }

            continue;
        }

        l.fatal("Could not find checkpoint {} in the current checkpoint list. Ignoring.", this.dsrp.getCheckpointMovedID());
        GameInstance.kill(GameInstance.EXIT_FATAL);

        return false;
    }

    @Override
    protected boolean onRegisterChosen() throws JSONException
    {
        return false;
    }

    // endregion Server request handlers

}
