package sep.view.clientcontroller;

import sep.view.json.           RDefaultServerRequestParser;
import sep.view.lib.            EShopState;
import sep.view.lib.            EGamePhase;
import sep.view.viewcontroller. SceneController;
import sep.view.viewcontroller. ViewSupervisor;

import java.io.                     IOException;
import java.io.                     InputStreamReader;
import java.io.                     BufferedReader;
import java.util.                   Objects;
import java.util.                   Arrays;
import java.util.                   HashMap;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.util.stream.            Collectors;
import java.util.stream.            IntStream;
import java.net.                    Socket;
import java.util.function.          Supplier;
import org.json.                    JSONException;
import org.json.                    JSONObject;

/**
 * We create a special object for listening to the server socket on a separate
 * thread to avoid blocking the main thread of the application.
 */
public final class ServerListener implements Runnable
{
    private static final Logger l = LogManager.getLogger(ServerListener.class);

    private static final int ORDERLY_CLOSE = -1;

    private final HashMap<String, Supplier<Boolean>> serverReq =
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
    }}
    ;

    private final Socket                socket;
    private final InputStreamReader     inputStreamReader;
    private final BufferedReader        bufferedReader;

    private RDefaultServerRequestParser dsrp;

    public ServerListener(final Socket socket, final InputStreamReader inputStreamReader, final BufferedReader bufferedReader)
    {
        super();

        this.socket             = socket;
        this.inputStreamReader  = inputStreamReader;
        this.bufferedReader     = bufferedReader;

        this.dsrp               = null;

        return;
    }

    public static void closeSocket()
    {
        EClientInformation.INSTANCE.closeSocket();
        EClientInformation.INSTANCE.resetServerConnectionAfterDisconnect();

        l.info("Client disconnected from server.");

        return;
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                final int escapeCharacter = this.bufferedReader.read();
                if (escapeCharacter == ServerListener.ORDERLY_CLOSE)
                {
                    GameInstance.handleServerDisconnect();
                    return;
                }

                final String r = String.format("%s%s", (char) escapeCharacter, this.bufferedReader.readLine());
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
        catch (IOException e)
        {
            GameInstance.handleServerDisconnect();
            return false;
        }

        return true;
    }

    private boolean onCorePlayerAttributesChanged() throws JSONException
    {
        l.debug("Player {}'s core attributes have changed. Updating.", this.dsrp.getPlayerID());
        EGameState.addRemotePlayer(this.dsrp);
        return true;
    }

    private boolean onChatMsg() throws JSONException
    {
        l.debug("New chat message received: [{}] from {}.", this.dsrp.getChatMsg(), this.dsrp.getChatMsgSourceID());
        ViewSupervisor.handleChatMessage(this.dsrp);
        return true;
    }

    private boolean onLobbyPlayerStatus() throws JSONException
    {
        l.debug("Received player status update. Client {} is ready: {}.", this.dsrp.getPlayerID(), this.dsrp.isLobbyPlayerStatusReady());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setReady(this.dsrp.isLobbyPlayerStatusReady());
        ViewSupervisor.updatePlayerStatus();
        return true;
    }

    private boolean onSelectMapRequest() throws JSONException
    {
        l.debug("Server requested client {} to choose a course. Available courses: {}.", EClientInformation.INSTANCE.getPlayerID(), String.join(", ", Arrays.asList(this.dsrp.getAvailableCourses())));
        EGameState.INSTANCE.setServerCourses(this.dsrp.getAvailableCourses());
        ViewSupervisor.updateAvailableCourses(true);
        return true;
    }

    private boolean onMapSelected() throws JSONException
    {
        l.debug("Current session course update: {}.", this.dsrp.getCourseName() == null || this.dsrp.getCourseName().isEmpty() ? "none" : this.dsrp.getCourseName());
        EGameState.INSTANCE.setCurrentServerCourse(this.dsrp.getCourseName());
        ViewSupervisor.updateCourseSelected();
        return true;
    }

    private boolean onGameStart() throws JSONException
    {
        l.debug("Game has started. Loading game scene . . .");
        ViewSupervisor.startGameLater(this.dsrp.getGameCourse());
        return true;
    }

    public boolean onPhaseChange() throws JSONException
    {
        l.debug("Game phase has changed. New phase: {}.", EGamePhase.fromInt(this.dsrp.getPhase()));
        EGameState.INSTANCE.setCurrentPhase(EGamePhase.fromInt(this.dsrp.getPhase()));
        return true;
    }

    private boolean onPlayerTurnChange() throws JSONException
    {
        l.debug("It is now player {}'s turn.", this.dsrp.getPlayerID());
        EGameState.INSTANCE.setCurrentPlayer(this.dsrp.getPlayerID());
        ViewSupervisor.handleChatInfo(String.format("Player %s is now current Player.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));
        return true;
    }

    private boolean onErrorMsg() throws JSONException
    {
        l.debug("The server run into an error. Message: {}.", this.dsrp.getErrorMessage());
        /* TODO Print err msg to UI Msg Scroll Pane. */
        return true;
    }

    /**
     * TODO For what reason does this exists in the protocol???
     */
    private boolean onCardPlayed() throws JSONException
    {
        l.debug("Player {} has played {}.", this.dsrp.getPlayerID(), this.dsrp.getCardName());
        ViewSupervisor.handleChatInfo(String.format("Player %s has played %s.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getCardName()));
        return true;
    }

    public boolean onStartingPointTaken() throws JSONException
    {
        l.debug("Player {} took starting point {}.", this.dsrp.getPlayerID(), this.dsrp.getCoordinate().toString());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setStartingPosition(this.dsrp.getCoordinate());
        ViewSupervisor.updatePlayerTransforms();
        ViewSupervisor.handleChatInfo(String.format("Player %s has selected a starting Point.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));
        return true;
    }

    public boolean onRobotRotationUpdate() throws JSONException
    {
        l.debug("Player {} has rotated {}.", this.dsrp.getPlayerID(), this.dsrp.getRotation());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getRobotView().addRotationWithLerp(this.dsrp.getRotation());
        return true;
    }

    public boolean onRegisterSlotUpdate() throws JSONException
    {
        // TODO We want to update the UI-Footer with this method call. Check if the playerID is the local player.
        //      Currently we update regardless if the select card action was affirmed by the server.
        l.debug("Player {} has updated their register {}. Filled: {}.", this.dsrp.getPlayerID(), this.dsrp.getRegister(), this.dsrp.getRegisterFilled() ? "true" : "false");
        return true;
    }

    private boolean onPlayerFinishedProgramming() throws JSONException
    {
        l.debug("Player {} has finished programming.", this.dsrp.getPlayerID());
        EGameState.INSTANCE.setSelectionFinished(this.dsrp.getPlayerID());
        ViewSupervisor.handleChatInfo(String.format("Player %s has finished his card selection.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));
        return true;
    }

    private boolean onForcedFinishProgramming() throws JSONException
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

    private boolean onPlayerProgrammingCardsReceived() throws JSONException
    {
        l.debug("Player {} has received their programming cards ({}).", this.dsrp.getPlayerID(), this.dsrp.getCardsInHandCountNYC());
        ViewSupervisor.handleChatInfo(String.format("Player %s has received %s cards in his hand.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getCardsInHandCountNYC()));
        return true;
    }

    private boolean onProgrammingDeckShuffled() throws JSONException
    {
        l.debug("The programming deck of player {} has been shuffled.", this.dsrp.getPlayerID());
        ViewSupervisor.handleChatInfo(String.format("The deck of player %s has been shuffled.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));
        return true;
    }

    private boolean onProgrammingTimerStart() throws JSONException
    {
        /* TODO Implement timer. */
        l.debug("Programming phase timer has started.");
        ViewSupervisor.handleChatInfo("The programming phase timer has started. Submit your cards in time!");
        return true;
    }

    private boolean onProgrammingTimerEnd() throws JSONException
    {
        /* TODO Implement timer. */
        l.debug("Programming phase timer has ended.");
        ViewSupervisor.handleChatInfo("The programming phase timer has ended.");
        return true;
    }

    private boolean onProgrammingCardsReceived() throws JSONException
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

    private boolean onCurrentRegisterCards() throws JSONException
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

    private boolean onCurrentRegisterCardReplacement() throws JSONException
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

        String info = String.format("Player %s received following card %s as replacement.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getNewCard());
        ViewSupervisor.handleChatInfo(info);

        return true;
    }

    private boolean onAnimationPlay() throws JSONException
    {
        l.debug("Server requested client {} to play an animation: {}.", EClientInformation.INSTANCE.getPlayerID(), this.dsrp.getAnimation().toString());
        ViewSupervisor.playAnimation(this.dsrp.getAnimation());
        return true;
    }

    private boolean onCheckpointReached() throws JSONException
    {
        l.debug("Player {} has reached {} checkpoints.", this.dsrp.getPlayerID(), this.dsrp.getNumber());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setCheckPointsReached(this.dsrp.getNumber());
        ViewSupervisor.handleChatInfo(String.format("Player %s has reached %s checkpoints.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName(), this.dsrp.getNumber()));
        return true;
    }

    private boolean onEnergyTokenChanged() throws JSONException
    {
        l.debug("Player {}'s energy amount has been updated to {}.", this.dsrp.getPlayerID(), this.dsrp.getEnergyCount());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).setEnergy(this.dsrp.getEnergyCount());
        ViewSupervisor.updatePlayerView();
        return true;
    }

    private boolean onGameEnd() throws JSONException
    {
        l.debug("Game has ended. The winner is player {}.", this.dsrp.getWinningPlayer());
        EGameState.INSTANCE.determineWinningPlayer(this.dsrp.getWinningPlayer());
        ViewSupervisor.getSceneController().renderNewScreen(SceneController.END_SCENE_ID, SceneController.PATH_TO_END_SCENE, true);
        return true;
    }

    private boolean onPlayerPositionUpdate() throws JSONException
    {
        l.debug("Player {} has moved to {}.", this.dsrp.getPlayerID(), this.dsrp.getCoordinate().toString());
        Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getRobotView().lerpTo(this.dsrp.getCoordinate());
        return true;
    }

    private boolean onPlayerReboot() throws JSONException
    {
        l.debug("Player {} was rebooted.", this.dsrp.getPlayerID());
        if(dsrp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID()){
            ViewSupervisor.handleChatInfo(String.format("You were rebooted. Choose your RebootDirection in the Shop Section"));
            //TODO hier fehlt noch Bearbeitung, wenn Shop schon aktiviert ist
            EGameState.INSTANCE.setShopState(EShopState.REBOOT);
            EGameState.INSTANCE.addShopSlot(0, "top");
            EGameState.INSTANCE.addShopSlot(1, "right");
            EGameState.INSTANCE.addShopSlot(2, "bottom");
            EGameState.INSTANCE.addShopSlot(3, "left");
            ViewSupervisor.updateFooter();
            l.debug("Updated Shop for RebootDirection");
        }else {
            l.debug("Player {} was rebooted.", this.dsrp.getPlayerID());
            ViewSupervisor.handleChatInfo(String.format("Player %s was rebooted.", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(this.dsrp.getPlayerID())).getPlayerName()));
        }

        return true;
    }

    private boolean onClientConnectionUpdate() throws JSONException
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

    private boolean onPickDamageType() throws JSONException
    {
        /* TODO Implement this to UI. */
        final String s = String.format("You have to pick %s damage cards. Available piles are: (%s).", this.dsrp.getDamageCardsCountToDraw(), String.join(", ", Arrays.asList(this.dsrp.getAvailableDamagePilesToDraw())));
        l.debug(s);
        ViewSupervisor.handleChatInfo(s);
        //TODO hier fehlt noch Bearbeitung, wenn Shop schon aktiviert ist
        EGameState.INSTANCE.setShopState(EShopState.DAMAGE);
        EGameState.INSTANCE.setDamageCardsCountToDraw(this.dsrp.getDamageCardsCountToDraw());
        String [] availableCards = this.dsrp.getAvailableDamagePilesToDraw();
        for(int i = 0; i < availableCards.length; i++) {
            EGameState.INSTANCE.addShopSlot(i, availableCards[i]);
        }
        ViewSupervisor.updateFooter();
        return true;
    }

    private boolean onDrawDamage() throws JSONException
    {
        /* TODO Implement this to UI. */
        final String i = String.format("Player %s has drawn the following damage cards: %s.", EClientInformation.INSTANCE.getPlayerID(), String.join(", ", Arrays.asList(this.dsrp.getDrawnDamageCards())));
        l.debug(i);
        ViewSupervisor.handleChatInfo(i);
        return true;
    }

    // endregion Server request handlers

    private void parseRequest(final RDefaultServerRequestParser dsrp) throws JSONException
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
