package sep.server.viewmodel;

import sep.server.json.common.CurrentPlayerModel;
import sep.server.json.game.activatingphase.ActivePhaseModel;
import sep.server.json.game.StartingPointTakenModel;
import sep.server.json.game.effects.PlayerTurningModel;
import sep.server.model.game.GameState;
import sep.server.model.EServerInformation;
import sep.server.json.lobby.PlayerAddedModel;
import sep.server.json.common.ChatMsgModel;
import sep.server.json.lobby.PlayerStatusModel;
import sep.server.json.lobby.SelectCourseModel;
import sep.server.json.lobby.CourseSelectedModel;
import sep.server.json.game.programmingphase.*;
import sep.server.json.game.programmingphase.SelectionFinishedModel;
import sep.server.model.game.EGamePhase;

import java.util.ArrayList;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles how clients can join and leave a session and also how to communicate with each other. For example,
 * chat messages. If a client disconnects mid-game, this class must handle that as well. (Wait for reconnection
 * or replace that player by an AI. We will probably have to handle this later on in an upcoming milestone.)
 */
public final class Session
{
    private static final Logger l = LogManager.getLogger(Session.class);

    private static final int DEFAULT_SESSION_ID_LENGTH = 5;

    private final ArrayList<PlayerController> playerControllers;
    private final ArrayList<PlayerController> readyPlayerControllerOrder;
    private final String sessionID;

    private final GameState gameState;

    private Thread awaitGameStartThread;

    public Session()
    {
        this(Session.generateSessionID());
        return;
    }

    public Session(String sessionID)
    {
        super();

        this.playerControllers = new ArrayList<PlayerController>();
        this.readyPlayerControllerOrder = new ArrayList<PlayerController>();
        this.sessionID = sessionID;
        this.gameState = new GameState(this);

        this.awaitGameStartThread = null;

        return;
    }

    public void joinSession(PlayerController playerController)
    {
        this.playerControllers.add(playerController);
        return;
    }

    // TODO We handle the leaving player logic here. But we can not inform other clients about it. Because
    //      we do not have the protocol for it yet.
    public void leaveSession(PlayerController playerController)
    {
        this.playerControllers.remove(playerController);

        if (this.playerControllers.isEmpty())
        {
            EServerInformation.INSTANCE.removeSession(this);
            return;
        }

//        this.broadcastChatMessage("SERVER", String.format("%s left the session.", playerController.getPlayerName()));

        return;
    }

    public void broadcastChatMessage(int caller, String message)
    {
        // TODO Validate message.
        if (message.isEmpty())
        {
            return;
        }

        for (PlayerController PC : this.playerControllers)
        {
            PC.sendChatMessage(caller, message, false);
            continue;
        }

        return;
    }

    public void sendKeepAlive(ArrayList<ClientInstance> dead)
    {

        for (PlayerController PC : this.playerControllers)
        {
            if (!PC.getClientInstance().isAlive())
            {
                dead.add(PC.getClientInstance());
                continue;
            }

            PC.getClientInstance().sendKeepAlive();
            continue;
        }

        return;
    }

    public void defaultBehaviourAfterPostLogin(PlayerController newPC)
    {
        /* Information for the new client to understand the current state of the game. */
        for (PlayerController PC : this.playerControllers)
        {
            new PlayerAddedModel(newPC, PC.getPlayerID(), PC.getPlayerName(), PC.getFigure()).send();
            new PlayerStatusModel(newPC.getClientInstance(), PC.getPlayerID(), PC.isReady()).send();
            continue;
        }
        new CourseSelectedModel(newPC, this.gameState.getCourseName()).send();

        /* Sending information about the new client to all other clients. */
        for (PlayerController PC : this.playerControllers)
        {
            if (PC.getPlayerID() == newPC.getPlayerID())
            {
                continue;
            }

            new PlayerAddedModel(PC, newPC.getPlayerID(), newPC.getPlayerName(), newPC.getFigure()).send();

            continue;
        }

        this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s joined the session.", newPC.getPlayerName()));

        return;
    }

    public void sendPlayerValuesToAllClients(PlayerController changedPC)
    {
        for (PlayerController PC : this.playerControllers)
        {
            new PlayerAddedModel(PC, changedPC.getPlayerID(), changedPC.getPlayerName(), changedPC.getFigure()).send();
            continue;
        }

        return;
    }

    public void handleChatMessage(PlayerController playerController, String chatMessageV2, int receiverID)
    {
        if (receiverID == ChatMsgModel.CHAT_MSG_BROADCAST)
        {
            this.broadcastChatMessage(playerController.getPlayerID(), chatMessageV2);
            return;
        }

        for (PlayerController PC : this.playerControllers)
        {
            if (PC.getPlayerID() == receiverID)
            {
                PC.sendChatMessage(playerController.getPlayerID(), chatMessageV2, true);
                return;
            }

            continue;
        }

        return;
    }

    public void broadcastPlayerLobbyReadyStatus(PlayerController playerController)
    {
        for (PlayerController PC : this.playerControllers)
        {
            new PlayerStatusModel(PC.getClientInstance(), playerController.getPlayerID(), playerController.isReady()).send();
            continue;
        }

        return;
    }

    private void broadcastCourseSelected(PlayerController playerController)
    {
        for (PlayerController PC : this.playerControllers)
        {
            new CourseSelectedModel(PC, this.gameState.getCourseName()).send();
            continue;
        }

        this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s selected the course %s.", playerController.getPlayerName(), this.gameState.getCourseName()));

        return;
    }

    private void updateCourseSelectorPower()
    {
        if (this.readyPlayerControllerOrder.isEmpty())
        {
            return;
        }

        new SelectCourseModel(this.readyPlayerControllerOrder.get(0)).send();

        return;
    }

    public void handlePlayerReadyStatus(PlayerController PC, boolean bIsReady)
    {
        PC.setReady(bIsReady);

        if (!bIsReady)
        {
            if (!this.readyPlayerControllerOrder.contains(PC))
            {
                return;
            }

            if (this.readyPlayerControllerOrder.indexOf(PC) != 0)
            {
                this.readyPlayerControllerOrder.remove(PC);
                return;
            }

            this.readyPlayerControllerOrder.remove(PC);
            this.updateCourseSelectorPower();

            return;
        }

        if (this.readyPlayerControllerOrder.isEmpty())
        {
            this.readyPlayerControllerOrder.add(PC);
            this.updateCourseSelectorPower();
            return;
        }

        if (this.readyPlayerControllerOrder.contains(PC))
        {
            return;
        }

        this.readyPlayerControllerOrder.add(PC);

        if (this.isReadyToStartGame())
        {
            this.prepareGameStart();
            return;
        }

        return;
    }

    public void handleSelectCourseName(PlayerController PC, String courseName)
    {
        this.gameState.setCourseName(courseName);
        this.broadcastCourseSelected(PC);

        if (this.isReadyToStartGame())
        {
            this.prepareGameStart();
            return;
        }

        return;
    }

    public void prepareGameStart()
    {
        this.broadcastChatMessage(ChatMsgModel.SERVER_ID, "All players are ready. The game will start in 5 seconds.");

        this.awaitGameStartThread = new Thread(() ->
        {
            l.info("Awaiting game start. . .");

            try
            {
                Thread.sleep(5);
            }
            catch (InterruptedException e)
            {
                l.warn("Prepare Game Start thread was interrupted.");
                l.warn(e.getMessage());
                return;
            }

            this.gameState.startGame(this.playerControllers.toArray(new PlayerController[0]));
            return;
        });

        this.awaitGameStartThread.start();

        return;
    }
    public void handleSelectedStartingPoint(int playerID, int x, int y){
        for(PlayerController pc : this.playerControllers){
            new StartingPointTakenModel(pc.getClientInstance(), x, y, playerID).send();
        }
    }

    public void handleCurrentPlayer(int playerID){
        for(PlayerController pc : this.playerControllers){
            new CurrentPlayerModel(pc.getClientInstance(), playerID).send();
        }
    }

    public void handleActivePhase(EGamePhase phase){
        for (PlayerController pc : playerControllers) {
            new ActivePhaseModel(pc.getClientInstance(), phase.i).send();
        }
    }


    // region Getters and Setters

    public String getSessionID()
    {
        return sessionID;
    }

    /** @deprecated Because multiple clients can have the same name. */
    public boolean isPlayerNameInSession(String playerName)
    {
        for (PlayerController PC : this.playerControllers)
        {
            if (PC.getPlayerName().equals(playerName))
            {
                return true;
            }

            continue;
        }

        return false;
    }

    private static String generateSessionID()
    {
        String t = UUID.randomUUID().toString().substring(0, Session.DEFAULT_SESSION_ID_LENGTH);
        for (Session s : EServerInformation.INSTANCE.getSessions())
        {
            if (s.getSessionID().equals(t))
            {
                return Session.generateSessionID();
            }

            continue;
        }

        return t;
    }

    public GameState getGameState()
    {
        return gameState;
    }

    private boolean isReadyToStartGame()
    {
        // TODO Don't check PC size but the human player size.
        if (this.playerControllers.size() < GameState.MIN_PLAYER_START)
        {
            return false;
        }

        for (PlayerController PC : this.playerControllers)
        {
            if (!PC.isReady())
            {
                return false;
            }

            continue;
        }

        if (this.gameState.getCourseName().isEmpty() || this.gameState.getCourseName().isBlank())
        {
            l.info("All players are ready. The server is awaiting a course selection.");
            this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("All players are ready. The server is awaiting %s to select a course.", this.readyPlayerControllerOrder.get(0).getPlayerName()));
            return false;
        }

        return true;
    }


    /**
     * Sends a set of hand cards to a specified player controller and notifies all players
     */
    public void sendHandCardsToPlayer(PlayerController targetPlayerController, String[] hand) {
        for (PlayerController playerController : this.playerControllers) {

            NotYourCardsModel notYourCardsModel = new NotYourCardsModel(playerController.getClientInstance(), targetPlayerController.getPlayerID(), hand.length);
            notYourCardsModel.send();

            if (playerController == targetPlayerController) {
                YourCardsModel yourCardsModel = new YourCardsModel(playerController.getClientInstance(),hand);
                yourCardsModel.send();
            }
        }
    }

    /**
     * Sends a shuffle  notification to all clients
     *
     * @param playerID The ID of the player who shuffled.
     */
    public void sendShuffleCodingNotification(int playerID) {
        for (PlayerController playerController : this.playerControllers) {
            ShuffleCodingModel shuffleCodingModel = new ShuffleCodingModel(playerController.getClientInstance(), playerID);
            shuffleCodingModel.send();
        }
    }


    /**
     * @param playerID The player ID of the player who made the selection
     * @param register The register of the selection
     * @param filled true for placed, false for removed
     */
    public void sendCardSelected(int playerID, int register, boolean filled) {
        for (PlayerController playerController : this.playerControllers) {
            CardSelectedModel cardSelectedModel = new CardSelectedModel(playerController.getClientInstance(), playerID, register, filled);
            cardSelectedModel.send();
        }
    }

    public void sendCardsYouGotNow(PlayerController targetPlayerController,String[] hand ){
        for (PlayerController playerController : this.playerControllers) {
            if (playerController == targetPlayerController) {
                CardsYouGotNowModel cardsYouGotNowModel = new CardsYouGotNowModel(playerController.getClientInstance(),hand);
                cardsYouGotNowModel.send();
            }
        }
    }

    public void sendTimerStarted() {
        for (PlayerController playerController : this.playerControllers) {
            TimerStartedModel timerStartedModel = new TimerStartedModel(playerController.getClientInstance());
            timerStartedModel.send();
        }
    }

    /**
     * @param playerIDS An array of player IDs who  have responded too slowly
     */
    public void sendTimerEnded(int[] playerIDS) {
        for (PlayerController playerController : this.playerControllers) {
            TimerEndedModel timerEndedModel = new TimerEndedModel(playerController.getClientInstance(), playerIDS);
            timerEndedModel.send();
        }
    }

    public void sendSelectionFinished(int playerID) {
        for (PlayerController playerController : this.playerControllers) {
            SelectionFinishedModel selectionFinishedModel = new SelectionFinishedModel(playerController.getClientInstance(), playerID);
            selectionFinishedModel.send();
        }
    }

    public void handlePlayerTurning(int playerID, String startingTurn) {
        for (PlayerController pc : this.playerControllers) {
            l.debug("Player " + playerID + " has turned: " + startingTurn);
            PlayerTurningModel playerTurningModel = new PlayerTurningModel(pc.getClientInstance(), playerID, startingTurn);
            playerTurningModel.send();
        }
    }


    // endregion Getters and Setters

}
