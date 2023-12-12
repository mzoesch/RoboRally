package sep.server.viewmodel;

import sep.server.json.common.CurrentPlayerModel;
import sep.server.json.game.activatingphase.ActivePhaseModel;
import sep.server.json.game.StartingPointTakenModel;
import sep.server.json.game.effects.GameFinishedModel;
import sep.server.json.game.effects.MovementModel;
import sep.server.json.game.effects.PlayerTurningModel;
import sep.server.model.game.GameState;
import sep.server.model.EServerInformation;
import sep.server.json.lobby.PlayerAddedModel;
import sep.server.json.common.ChatMsgModel;
import sep.server.json.lobby.PlayerStatusModel;
import sep.server.json.lobby.SelectMapModel;
import sep.server.json.lobby.MapSelectedModel;
import sep.server.json.game.programmingphase.*;
import sep.server.json.game.programmingphase.SelectionFinishedModel;
import sep.server.model.game.EGamePhase;
import sep.server.model.game.Tile;
import sep.server.json.game.GameStartedModel;
import sep.server.json.game.activatingphase.CardInfo;
import sep.server.json.game.activatingphase.CurrentCardsModel;
import sep.server.model.game.Player;
import sep.server.model.game.cards.Card;
import sep.server.json.common.ConnectionUpdateModel;
import sep.server.model.IOwnershipable;
import sep.server.model.Agent;
import sep.server.json.game.effects.CheckPointReachedModel;
import sep.server.json.game.effects.EnergyModel;
import sep.server.json.game.effects.RebootModel;
import sep.server.json.game.activatingphase.ReplaceCardModel;
import sep.server.model.game.tiles.Coordinate;

import java.util.ArrayList;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles how clients can join and leave a session and also how to communicate with each other. For example,
 * chat messages. If a client disconnects mid-game, this class must handle that as well.
 */
public final class Session
{
    private static final Logger l = LogManager.getLogger(Session.class);

    private static final int DEFAULT_SESSION_ID_LENGTH = 5;

    private final ArrayList<IOwnershipable> ctrls;
    private final ArrayList<PlayerController> readyCharacterOrder;
    private final String sessionID;

    private final GameState gameState;

    private Thread awaitGameStartThread;

    public Session()
    {
        this(Session.generateSessionID());
        return;
    }

    public Session(final String sessionID)
    {
        super();

        this.ctrls = new ArrayList<IOwnershipable>();
        this.readyCharacterOrder = new ArrayList<PlayerController>();
        this.sessionID = sessionID;
        this.gameState = new GameState(this);

        this.awaitGameStartThread = null;

        return;
    }

    public void joinSession(final IOwnershipable ctrl)
    {
        this.ctrls.add(ctrl);
        return;
    }

    public void leaveSession(final IOwnershipable ctrl)
    {
        this.ctrls.remove(ctrl);

        if (this.ctrls.isEmpty())
        {
            EServerInformation.INSTANCE.removeSession(this);
            return;
        }

        if (this.getRemotePlayers().isEmpty())
        {
            EServerInformation.INSTANCE.removeSession(this);
            return;
        }

        if (ctrl instanceof final PlayerController pc)
        {
            this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s left the session.", pc.getName()));

            if (this.getGameState().hasGameStarted())
            {
                l.debug("Client {} disconnected mid-game.", pc.getClientInstance().getAddr());
                this.getGameState().getAuthGameMode().removePlayer(pc.getPlayerID());
                this.broadcastConnectionUpdate(pc, EConnectionLoss.REMOVE, false);
                l.info("Client {} was successfully removed from the game.", pc.getClientInstance().getAddr());
                return;
            }

            /* TODO If the await thread is already running. */

            l.debug("Client {} disconnected.", pc.getClientInstance().getAddr());
            this.broadcastConnectionUpdate(pc, EConnectionLoss.REMOVE, false);
            this.readyCharacterOrder.remove(pc);
            l.info("Client {} was successfully removed from the session.", pc.getClientInstance().getAddr());

            return;
        }

        /* If a local player is removed. */
        l.info("Agent {} was successfully removed from the session [{}]. Executing post behaviour.", ctrl.getPlayerID(), this.sessionID);
        this.broadcastConnectionUpdate(ctrl, EConnectionLoss.REMOVE, false);
        this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("The %s was removed from the session.", ctrl.getName().replaceAll("[\\[\\]]", "")));

        return;
    }

    private void broadcastConnectionUpdate(final IOwnershipable tCtrl, final EConnectionLoss eCL, final boolean bConnected)
    {
        if (tCtrl == null)
        {
            return;
        }

        for (PlayerController pc : this.getRemotePlayers())
        {
            new ConnectionUpdateModel(pc.getClientInstance(), tCtrl.getPlayerID(), eCL.toString(), bConnected).send();
            continue;
        }

        return;
    }

    public void broadcastChatMessage(final int caller, final String msg)
    {
        // TODO Validate message.
        if (msg.isEmpty())
        {
            return;
        }

        for (final PlayerController pc : this.getRemotePlayers())
        {
            pc.sendChatMessage(caller, msg, false);
            continue;
        }

        return;
    }

    public void sendKeepAlive(final ArrayList<ClientInstance> dead)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            if (!pc.getClientInstance().isAlive())
            {
                dead.add(pc.getClientInstance());
                continue;
            }

            pc.getClientInstance().sendKeepAlive();
            continue;
        }

        return;
    }

    /**
     * This method must be called after a client has joined the session. Currently, there is no automatic call to
     * this after a client has joined. So be careful.
     *
     * @param newCtrl The controller that just joined the session.
     */
    public void onPostJoin(final IOwnershipable newCtrl)
    {
        /* Information for the new client to understand the current state of the game. */
        if (newCtrl instanceof final PlayerController newPC)
        {
            for (final IOwnershipable ow : this.ctrls)
            {
                new PlayerAddedModel(newPC, ow.getPlayerID(), ow.getName(), ow.getFigure()).send();

                if (ow instanceof final PlayerController pc)
                {
                    /* An Agent is always ready. */
                    new PlayerStatusModel(newPC.getClientInstance(), pc.getPlayerID(), pc.isReady()).send();
                    continue;
                }

                continue;
            }

            new MapSelectedModel(newPC, this.gameState.getCourseName()).send();
        }

        /* Sending information about the new client to all other clients. */
        for (final PlayerController pc : this.getRemotePlayers())
        {
            if (pc.getPlayerID() == newCtrl.getPlayerID())
            {
                continue;
            }

            new PlayerAddedModel(pc, newCtrl.getPlayerID(), newCtrl.getName(), newCtrl.getFigure()).send();

            continue;
        }

        this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s joined the session.", newCtrl.getName()));

        return;
    }

    /** @param cCtrl The controller that changed in any way. */
    public void sendPlayerValuesToAllClients(final IOwnershipable cCtrl)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new PlayerAddedModel(pc, cCtrl.getPlayerID(), cCtrl.getName(), cCtrl.getFigure()).send();
            continue;
        }

        return;
    }

    public void handleChatMessage(final PlayerController callingPC, final String msg, final int receiverID)
    {
        if (Session.isChatMsgARemoteCommand(msg))
        {
            l.debug("Detected remote command from client {}.", callingPC.getClientInstance().getAddr());
            this.handleRemoteCommand(callingPC, msg);
            return;
        }

        if (receiverID == ChatMsgModel.CHAT_MSG_BROADCAST)
        {
            this.broadcastChatMessage(callingPC.getPlayerID(), msg);
            return;
        }

        for (final PlayerController pc : this.getRemotePlayers())
        {
            if (pc.getPlayerID() == receiverID)
            {
                pc.sendChatMessage(pc.getPlayerID(), msg, true);
                return;
            }

            continue;
        }

        l.warn("Client {} tried to send a private message to a non-existing remote player.", callingPC.getClientInstance().getAddr());

        return;
    }

    private void handleRemoteCommand(final PlayerController callingPC, final String msg)
    {
        final String token = msg.substring(1, msg.indexOf(ChatMsgModel.ARG_BEGIN));
        final String[] args = Session.getChatRemoteCommandArguments(msg);

        /* DETACH */
        if (token.equals(ChatMsgModel.remoteCommands[0]))
        {
            this.handleDetachCommand(callingPC, args);
            return;
        }

        return;
    }

    private void handleDetachCommand(final PlayerController callingPC, final String[] args)
    {
        l.debug("Client {} is trying to detach an agent.", callingPC.getClientInstance().getAddr());

        if (args.length != 1)
        {
            l.warn("Client {} tried to detach an agent but did not provide the correct amount of arguments.", callingPC.getClientInstance().getAddr());
            return;
        }

        final int agentID;
        try
        {
            agentID = Integer.parseInt(args[0]);
        }
        catch (NumberFormatException e)
        {
            l.warn("Client {} tried to detach an agent but provided an invalid agent ID.", callingPC.getClientInstance().getAddr());
            return;
        }

        for (final Agent a : this.getAgents())
        {
            if (a.getPlayerID() == agentID)
            {
                this.leaveSession(a);
                return;
            }

            continue;
        }

        l.warn("Client {} tried to detach an agent but provided an invalid agent ID.", callingPC.getClientInstance().getAddr());

        return;
    }

    public void broadcastPlayerLobbyReadyStatus(final PlayerController sourcePC)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new PlayerStatusModel(pc.getClientInstance(), sourcePC.getPlayerID(), sourcePC.isReady()).send();
            continue;
        }

        return;
    }

    private void broadcastCourseSelected(final PlayerController selector)
    {
        for (PlayerController pc : this.getRemotePlayers())
        {
            new MapSelectedModel(pc, this.gameState.getCourseName()).send();
            continue;
        }

        this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s selected the course %s.", selector.getName(), this.gameState.getCourseName()));

        return;
    }

    private void updateCourseSelectorPower()
    {
        if (this.readyCharacterOrder.isEmpty())
        {
            return;
        }

        new SelectMapModel(this.readyCharacterOrder.get(0)).send();

        return;
    }

    public void handlePlayerReadyStatus(final PlayerController pc, final boolean bIsReady)
    {
        pc.setReady(bIsReady);

        if (!bIsReady)
        {
            if (!this.readyCharacterOrder.contains(pc))
            {
                return;
            }

            if (this.readyCharacterOrder.indexOf(pc) != 0)
            {
                this.readyCharacterOrder.remove(pc);
                return;
            }

            this.readyCharacterOrder.remove(pc);
            this.updateCourseSelectorPower();

            return;
        }

        if (this.readyCharacterOrder.isEmpty())
        {
            this.readyCharacterOrder.add(pc);
            this.updateCourseSelectorPower();
            return;
        }

        if (this.readyCharacterOrder.contains(pc))
        {
            return;
        }

        this.readyCharacterOrder.add(pc);

        if (this.isReadyToStartGame())
        {
            this.prepareGameStart();
            return;
        }

        return;
    }

    public void handleSelectCourseName(final PlayerController pc, final String courseName)
    {
        this.gameState.setCourseName(courseName);
        this.broadcastCourseSelected(pc);

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
            l.info("Awaiting game start . . .");

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

            /* TODO Here set the figures of the agents if available. */

            this.gameState.startGame();

            return;
        });

        this.awaitGameStartThread.start();

        return;
    }

    public void handleSelectedStartingPoint(final int ctrlID, final int x, final int y)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new StartingPointTakenModel(pc.getClientInstance(), x, y, ctrlID).send();
            continue;
        }

        return;
    }

    public void broadcastCurrentPlayer(final int playerID)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new CurrentPlayerModel(pc.getClientInstance(), playerID).send();
            continue;
        }

        return;
    }

    public void broadcastNewGamePhase(final EGamePhase phase)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new ActivePhaseModel(pc.getClientInstance(), phase.i).send();
            continue;
        }

        return;
    }

    public void broadcastGameStart(final ArrayList<ArrayList<Tile>> course)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new GameStartedModel(pc.getClientInstance(), course).send();
            continue;
        }

        return;
    }

    public void broadcastCurrentCards(final int register)
    {
        final CardInfo[] activeCards = new CardInfo[this.getGameState().getAuthGameMode().getPlayers().size()];
        for (int i = 0; i < activeCards.length; i++)
        {
            for (Player p : this.getGameState().getAuthGameMode().getPlayers())
            {
                CardInfo ci = new CardInfo(p.getController().getPlayerID(), ( (Card) p.getCardByRegisterIndex(register) ).getCardType());
                activeCards[i] = ci;

                continue;
            }

            continue;
        }

        for (final PlayerController pc : this.getRemotePlayers())
        {
            new CurrentCardsModel(pc.getClientInstance(), activeCards).send();
            continue;
        }

        return;
    }

    public void broadcastCheckPointReached(final int ctrlID, final int checkpointsReached)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new CheckPointReachedModel(pc.getClientInstance(), ctrlID, checkpointsReached).send();
            continue;
        }

        return;
    }

    /** @param ctrlID The ID of the controller that won the game. */
    public void broadcastGameFinish(final int ctrlID)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new GameFinishedModel(pc.getClientInstance(), ctrlID).send();
            continue;
        }

        return;
    }

    /**
     * @param ctrlID The ID of the controller that has changed its energy.
     * @param energy The new energy value.
     * @param source The source of the energy change.
     */
    public void broadcastEnergyUpdate(final int ctrlID, final int energy, final String source)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new EnergyModel(pc.getClientInstance(), ctrlID, energy, source).send();
            continue;
        }

        return;
    }

    public void broadcastPositionUpdate(final int ctrlID, final int x, final int y)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new MovementModel(pc.getClientInstance(), ctrlID, x, y).send();
            continue;
        }

        return;
    }

    public void broadcastPositionUpdate(final int ctrlID, final Coordinate c)
    {
        this.broadcastPositionUpdate(ctrlID, c.getX(), c.getY());
        return;
    }

    public void broadcastRotationUpdate(final int ctrlID, final String rotation)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new PlayerTurningModel(pc.getClientInstance(), ctrlID, rotation).send();
            continue;
        }

        return;
    }

    /**
     * @param ctrlID   The ID of the controller that replaced a card.
     * @param register The current activation phase register.
     * @param card     The new card that was placed in the register.
     */
    public void broadcastReplacedCard(final int ctrlID, final int register, final String card)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new ReplaceCardModel(pc.getClientInstance(), ctrlID, register, card).send();
            continue;
        }

        return;
    }

    public void broadcastReboot(final int ctrlID)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new RebootModel(pc.getClientInstance(), ctrlID).send();
            continue;
        }

        return;
    }

    // region Getters and Setters

    public String getSessionID()
    {
        return this.sessionID;
    }

    /** @deprecated Because multiple clients can have the same name. */
    public boolean isPlayerNameInSession(final String playerName)
    {
        for (IOwnershipable ctrl : this.ctrls)
        {
            if (ctrl.getName().equals(playerName))
            {
                return true;
            }

            continue;
        }

        return false;
    }

    private static String generateSessionID()
    {
        final String t = UUID.randomUUID().toString().substring(0, Session.DEFAULT_SESSION_ID_LENGTH);
        for (final Session s : EServerInformation.INSTANCE.getSessions())
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
        return this.gameState;
    }

    private boolean isReadyToStartGame()
    {
        if (this.getRemotePlayers().size() < GameState.MIN_REMOTE_PLAYER_COUNT_TO_START)
        {
            return false;
        }

        for (final PlayerController pc : this.getRemotePlayers())
        {
            if (!pc.isReady())
            {
                return false;
            }

            continue;
        }

        if (this.gameState.getCourseName().isEmpty() || this.gameState.getCourseName().isBlank())
        {
            l.info("All players are ready. The server is awaiting a course selection.");
            this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("All players are ready. The server is awaiting %s to select a course.", this.readyCharacterOrder.get(0).getName()));
            return false;
        }

        return true;
    }

    /** Sends a set of hand cards to a specified player controller and notifies all remote players. */
    public void sendHandCardsToPlayer(final PlayerController tPC, final String[] hand)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            NotYourCardsModel notYourCardsModel = new NotYourCardsModel(pc.getClientInstance(), tPC.getPlayerID(), hand.length);
            notYourCardsModel.send();

            if (pc == tPC)
            {
                YourCardsModel yourCardsModel = new YourCardsModel(pc.getClientInstance(), hand);
                yourCardsModel.send();
            }

            continue;
        }

        return;
    }

    /**
     * Sends a shuffle notification to all remote players.
     *
     * @param playerID The ID of the player who shuffled.
     */
    public void sendShuffleCodingNotification(final int playerID)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            ShuffleCodingModel shuffleCodingModel = new ShuffleCodingModel(pc.getClientInstance(), playerID);
            shuffleCodingModel.send();

            continue;
        }

        return;
    }

    /**
     * @param playerID The player ID of the player who made the selection.
     * @param register The register of the selection.
     * @param bFilled  True if the card was places in the register, false if it was removed-
     */
    public void sendCardSelected(final int playerID, final int register, final boolean bFilled)
    {
        for (PlayerController pc : this.getRemotePlayers())
        {
            CardSelectedModel cardSelectedModel = new CardSelectedModel(pc.getClientInstance(), playerID, register, bFilled);
            cardSelectedModel.send();

            continue;
        }

        return;
    }

    public void sendCardsYouGotNow(final PlayerController tPC, final String[] hand)
    {
        for (PlayerController pc : this.getRemotePlayers())
        {
            if (pc == tPC)
            {
                CardsYouGotNowModel cardsYouGotNowModel = new CardsYouGotNowModel(pc.getClientInstance(), hand);
                cardsYouGotNowModel.send();
            }

            continue;
        }

        return;
    }

    public void sendTimerStarted()
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            TimerStartedModel timerStartedModel = new TimerStartedModel(pc.getClientInstance());
            timerStartedModel.send();

            continue;
        }

        return;
    }

    /** @param playerIDs The players that have not finished programming in time. */
    public void sendTimerEnded(final int[] playerIDs)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            TimerEndedModel timerEndedModel = new TimerEndedModel(pc.getClientInstance(), playerIDs);
            timerEndedModel.send();

            continue;
        }

        return;
    }

    public void sendSelectionFinished(final int playerID)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            SelectionFinishedModel selectionFinishedModel = new SelectionFinishedModel(pc.getClientInstance(), playerID);
            selectionFinishedModel.send();

            continue;
        }

        return;
    }

    public void handlePlayerTurning(final int playerID, final String startingTurn)
    {
        for (PlayerController pc : this.getRemotePlayers())
        {
            l.debug("Player {} has turned {}.", playerID, startingTurn);
            PlayerTurningModel playerTurningModel = new PlayerTurningModel(pc.getClientInstance(), playerID, startingTurn);
            playerTurningModel.send();

            continue;
        }

        return;
    }

    public void handleGameFinished(final int playerID)
    {
        l.debug("Notifying remote players that {} has won the game.", playerID);
        for (final PlayerController pc : this.getRemotePlayers())
        {
            GameFinishedModel gameFinishedModel = new GameFinishedModel(pc.getClientInstance(), playerID);
            gameFinishedModel.send();

            continue;
        }

        return;
    }

    public void handleMovement(final int playerID, final int x, final int y)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            MovementModel movementModel = new MovementModel(pc.getClientInstance(), playerID, x, y);
            movementModel.send();

            continue;
        }

        return;
    }

    public boolean haveAllPlayersFinishedProgramming()
    {
        /* Because agents will always instantly have finished their programming. */
        for (final PlayerController pc : this.getRemotePlayers())
        {
            if (!pc.getPlayer().hasPlayerFinishedProgramming())
            {
                return false;
            }

            continue;
        }

        return true;
    }

    public IOwnershipable[] getControllers()
    {
        return this.ctrls.toArray(new IOwnershipable[0]);
    }

    /** @return All remote players in this session. */
    public ArrayList<PlayerController> getRemotePlayers()
    {
        ArrayList<PlayerController> t = new ArrayList<PlayerController>();
        for (final IOwnershipable ctrl : this.ctrls)
        {
            if (ctrl instanceof PlayerController)
            {
                t.add((PlayerController) ctrl);
                continue;
            }

            continue;
        }

        return t;
    }

    public void addAgent()
    {
        if (this.ctrls.size() >= GameState.MAX_CONTROLLERS_ALLOWED)
        {
            l.warn("The maximum amount of controllers has been reached. No more agents can be added.");
            return;
        }

        final Agent a = ServerInstance.createNewAgent(this);
        this.joinSession(a);
        this.onPostJoin(a);

        l.info("Agent {} was added to session [{}].", a.getPlayerID(), this.sessionID);

        return;
    }

    public Agent[] getAgents()
    {
        return this.ctrls.stream().filter(c -> c instanceof Agent).map(c -> (Agent) c).toArray(Agent[]::new);
    }

    public boolean isAgentNameTaken(final String agentName)
    {
        for (final Agent a : this.getAgents())
        {
            if (a.getName().equals(agentName))
            {
                return true;
            }

            continue;
        }

        return false;
    }

    /**
     * This method will auto split the message into the command and the arguments.
     * You must not parse the message in any way before passing it to this method.
     *
     * @param msg The chat message to check.
     * @return    The arguments of the remote command. If the message is not a remote command, null is returned.
     */
    public static String[] getChatRemoteCommandArguments(final String msg)
    {
        return msg.substring(msg.indexOf(ChatMsgModel.ARG_BEGIN) + 1, msg.lastIndexOf(ChatMsgModel.ARG_END)).split(ChatMsgModel.ARG_SEPARATOR);
    }

    public static boolean isChatMsgARemoteCommand(final String msg)
    {
        return          msg.startsWith(ChatMsgModel.COMMAND_PREFIX)
                &&      msg.length() > 1
                &&      msg.indexOf(ChatMsgModel.ARG_BEGIN) > 0
                &&      msg.lastIndexOf(ChatMsgModel.ARG_END) > 0
                &&      msg.indexOf(ChatMsgModel.ARG_BEGIN) < msg.lastIndexOf(ChatMsgModel.ARG_END)
                && !    msg.substring(1, msg.indexOf(ChatMsgModel.ARG_BEGIN)).isEmpty()
                && !    msg.substring(msg.indexOf(ChatMsgModel.ARG_BEGIN) + 1, msg.lastIndexOf(ChatMsgModel.ARG_END) - 1).isEmpty();
    }

    // endregion Getters and Setters

}
