package sep.server.viewmodel;

import sep.server.json.game.activatingphase.    ActivePhaseModel;
import sep.server.json.game.activatingphase.    ReplaceCardModel;
import sep.server.json.game.activatingphase.    CardInfo;
import sep.server.json.game.activatingphase.    CurrentCardsModel;
import sep.server.json.game.effects.            AnimationModel;
import sep.server.json.game.effects.            CheckPointReachedModel;
import sep.server.json.game.effects.            CheckpointMovedModel;
import sep.server.json.game.effects.            EnergyModel;
import sep.server.json.game.effects.            GameFinishedModel;
import sep.server.json.game.effects.            MovementModel;
import sep.server.json.game.effects.            PlayerTurningModel;
import sep.server.json.game.effects.            RebootModel;
import sep.server.json.game.effects.            RegisterChosenModel;
import sep.server.json.lobby.                   PlayerAddedModel;
import sep.server.json.lobby.                   PlayerStatusModel;
import sep.server.json.lobby.                   SelectMapModel;
import sep.server.json.lobby.                   MapSelectedModel;
import sep.server.model.                        EServerInformation;
import sep.server.model.                        IOwnershipable;
import sep.server.model.                        Agent;
import sep.server.model.game.                   GameState;
import sep.server.model.game.                   EGamePhase;
import sep.server.model.game.                   Tile;
import sep.server.model.game.                   Player;
import sep.server.model.game.                   EAnimation;
import sep.server.json.common.                  ChatMsgModel;
import sep.server.json.common.                  CurrentPlayerModel;
import sep.server.json.common.                  ConnectionUpdateModel;
import sep.server.json.game.programmingphase.   NotYourCardsModel;
import sep.server.json.game.programmingphase.   YourCardsModel;
import sep.server.json.game.programmingphase.   ShuffleCodingModel;
import sep.server.json.game.programmingphase.   CardSelectedModel;
import sep.server.json.game.programmingphase.   CardsYouGotNowModel;
import sep.server.json.game.programmingphase.   TimerStartedModel;
import sep.server.json.game.programmingphase.   TimerEndedModel;
import sep.server.json.game.programmingphase.   SelectionFinishedModel;
import sep.server.model.game.tiles.             Coordinate;
import sep.server.json.game.                    GameStartedModel;
import sep.server.json.game.                    StartingPointTakenModel;
import sep.server.model.game.cards.             Card;
import sep.                                     Types;
import sep.server.json.game.upgradephase.       RefillShopModel;
import sep.server.json.game.upgradephase.       ExchangeShopModel;
import sep.server.json.game.upgradephase.       UpgradeBoughtModel;

import java.util.                   ArrayList;
import java.util.                   Objects;
import java.util.                   UUID;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/**
 * Handles how clients can join and leave a session and also how to communicate with each other.
 * If a client disconnects mid-game, this class must handle that as well.
 */
public final class Session
{
    private static final Logger l = LogManager.getLogger(Session.class);

    private static final int    DEFAULT_SESSION_ID_LENGTH       = 5;
    private static final int    DEV_SESSION_GAME_START_DELAY    = 5;
    private static final int    PROD_SESSION_GAME_START_DELAY   = 5_000;

    private final ArrayList<IOwnershipable>     ctrls;
    private final ArrayList<PlayerController>   readyCharacterOrder;
    private final String                        sessionID;

    private final GameState                     gameState;

    private Thread                              awaitGameStartThread;

    public Session()
    {
        this(Session.generateSessionID());
        return;
    }

    public Session(final String sessionID)
    {
        super();

        this.ctrls                  = new ArrayList<IOwnershipable>();
        this.readyCharacterOrder    = new ArrayList<PlayerController>();
        this.sessionID              = sessionID;
        this.gameState              = new GameState(this);

        this.awaitGameStartThread   = null;

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

            if (this.awaitGameStartThread != null)
            {
                this.awaitGameStartThread.interrupt();
                this.awaitGameStartThread = null;
            }

            l.debug("Client {} disconnected.", pc.getClientInstance().getAddr());
            this.broadcastConnectionUpdate(pc, EConnectionLoss.REMOVE, false);
            this.readyCharacterOrder.clear();
            for (final PlayerController humanPC : this.getHumanRemotePlayers())
            {
                humanPC.setReady(false);
                this.broadcastPlayerLobbyReadyStatus(humanPC);
                continue;
            }
            l.info("Client {} was successfully removed from the session.", pc.getClientInstance().getAddr());

            return;
        }

        /* If a local player is removed. They cannot disconnect mid-game. */
        l.info("Agent {} was successfully removed from the session [{}]. Executing post behaviour.", ctrl.getPlayerID(), this.sessionID);
        this.broadcastConnectionUpdate(ctrl, EConnectionLoss.REMOVE, false);
        this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("The %s was removed from the session.", ctrl.getName().replaceAll("[\\[\\]]", "")));

        return;
    }

    public void onClose() throws InterruptedException
    {
        if (this.awaitGameStartThread != null)
        {
            this.awaitGameStartThread.interrupt();
            this.awaitGameStartThread = null;
        }

        this.gameState.onClose();

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
        /* TODO Validate message. */
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

    public void sendKeepAlive(final ArrayList<ClientInstance> outDead)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            if (!pc.getClientInstance().isAlive())
            {
                outDead.add(pc.getClientInstance());
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
                    /* An Agent is always ready. So we do not need to send any information about him. */
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

        /* If an agent joined all remote player's ready status will be reset. */
        for (final PlayerController pc : this.getRemotePlayers())
        {
            pc.setReady(false);
            this.broadcastPlayerLobbyReadyStatus(pc);
            continue;
        }
        this.readyCharacterOrder.clear();

        return;
    }

    /** @param cCtrl The controller that changed in any way. */
    public void broadcastCorePlayerAttributes(final IOwnershipable cCtrl)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new PlayerAddedModel(pc, cCtrl.getPlayerID(), cCtrl.getName(), cCtrl.getFigure()).send();
            continue;
        }

        return;
    }

    public void parseAndExecuteChatMessage(final PlayerController callingPC, final String msg, final int receiverID)
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

        if (this.getPlayerControllerByID(receiverID) != null)
        {
            Objects.requireNonNull(this.getPlayerControllerByID(receiverID)).sendChatMessage(callingPC.getPlayerID(), msg, true);
            return;
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

    public synchronized void onPlayerReadyStatusUpdate(final PlayerController pc, final boolean bIsReady)
    {
        pc.setReady(bIsReady);

        if (!bIsReady)
        {
            if (this.awaitGameStartThread != null)
            {
                this.awaitGameStartThread.interrupt();
                this.awaitGameStartThread = null;
            }

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

        if (this.readyCharacterOrder.isEmpty() && !pc.isRemoteAgent())
        {
            this.readyCharacterOrder.add(pc);
            this.updateCourseSelectorPower();

            if (this.isReadyToStartGame())
            {
                this.prepareGameStart();
                return;
            }

            return;
        }

        if (this.readyCharacterOrder.contains(pc))
        {
            return;
        }

        if (!pc.isRemoteAgent())
        {
            this.readyCharacterOrder.add(pc);
        }

        if (this.isReadyToStartGame())
        {
            this.prepareGameStart();
            return;
        }

        return;
    }

    public void onCourseSelect(final PlayerController pc, final String courseName)
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

        // TODO
        //      We have to implement checks for if a client disconnects during this time
        //      or they pick a new name figure etc. Is is currently very unsafe!

        this.awaitGameStartThread = new Thread(() ->
        {
            l.info("Awaiting game start . . .");

            try
            {
                Thread.sleep(Session.getSessionGameStartDelay());
            }
            catch (final InterruptedException e)
            {
                l.info("Aborting game start.");
                l.debug(e.getMessage());
                this.awaitGameStartThread = null; /* Just for safety reasons, but the caller should always be responsible for setting this var to a nullptr again. */
                return;
            }

            this.gameState.startGame();

            this.awaitGameStartThread = null; /* Just for safety reasons, but the caller should always be responsible for setting this var to a nullptr again. */

            return;
        });
        this.awaitGameStartThread.setName(String.format("AwaitGameStart-%s", this.sessionID));
        this.awaitGameStartThread.start();

        return;
    }

    public void broadcastSelectedStartingPoint(final int ctrlID, final int x, final int y)
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

    public void broadcastCurrentCards(final int register) {
        final ArrayList<Player> players = this.getGameState().getAuthGameMode().getPlayers();
        if (players.isEmpty()) {
            l.fatal("No players exist.");
            EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
            return;
        }

        final CardInfo[] activeCards = new CardInfo[players.size()];
        for (int i = 0; i < activeCards.length; i++) {
            Player p = players.get(i);
            Card card = (Card) p.getCardByRegisterIndex(register);
            CardInfo ci;

            if (card == null) {
                ci = new CardInfo(p.getController().getPlayerID(), null);
            } else {
                ci = new CardInfo(p.getController().getPlayerID(), card.getCardType());
            }
            activeCards[i] = ci;
        }

        for (final PlayerController pc : this.getRemotePlayers()) {
            new CurrentCardsModel(pc.getClientInstance(), activeCards).send();
        }
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

    public void broadcastCheckPointMoved(int checkPointID, int x, int y){

        for (final PlayerController pc : this.getRemotePlayers())
        {
            new CheckpointMovedModel(pc.getClientInstance(), checkPointID, x, y).send();
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
     * @param ctrlID    The ID of the controller that has changed its energy.
     * @param energy    The new energy value.
     * @param source    The source of the energy change.
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

    public void broadcastShopRefill(final ArrayList<String> cards)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new RefillShopModel(pc.getClientInstance(), cards).send();
            continue;
        }

        return;
    }

    public void broadcastShopExchange(final ArrayList<String> cards)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new ExchangeShopModel(pc.getClientInstance(), cards).send();
            continue;
        }

        return;
    }

    public void broadcastBoughtUpgradeCard(final int ctrlID, final String card)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new UpgradeBoughtModel(pc.getClientInstance(), ctrlID, card).send();
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

    private synchronized boolean isReadyToStartGame()
    {
        if (this.awaitGameStartThread != null)
        {
            l.fatal("Tried to start gam but the session is already awaiting a game start.");
            EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
            return false;
        }

        if (this.gameState.hasGameStarted())
        {
            l.fatal("Tried to start game but the game has already started.");
            EServerInstance.INSTANCE.kill(EServerInstance.EServerCodes.FATAL);
            return false;
        }

        if (this.isLegacy())
        {
            l.warn("The session [{}] is a legacy session. And contains agents that should only be used for development purposes.", this.sessionID);

            if (this.getRemotePlayers().size() < this.gameState.getMinRemotePlayersToStart())
            {
                l.debug("The server is awaiting more remote controllers to join before considering starting the game.");
                return false;
            }

            if (this.ctrls.size() < GameState.MIN_CONTROLLERS_ALLOWED)
            {
                l.debug("The server is awaiting more controllers to join before considering starting the game.");
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

            l.debug("All controllers are ready and the session decided that the game may start now.");

            return true;
        }

        if (this.getHumanRemotePlayers().size() < this.gameState.getMinHumanPlayersToStart())
        {
            l.debug("The server is awaiting more human remote controllers to join before considering starting the game.");
            return false;
        }

        if (this.ctrls.size() < GameState.MIN_CONTROLLERS_ALLOWED)
        {
            l.debug("The server is awaiting more remote controllers to join before considering starting the game.");
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
            if (this.ctrls.size() == this.getAgentRemotePlayers().size())
            {
                l.info("This is an agent only session. The server will automatically select a course.");
                throw new UnsupportedOperationException("This is an agent only session. The server will automatically select a course. This is not implemented yet.");
            }

            l.info("All players are ready. The server is awaiting a course selection.");
            this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("All players are ready. The server is awaiting %s to select a course.", this.readyCharacterOrder.get(0).getName()));
            return false;
        }

        l.debug("All controllers are ready and the session decided that the game may start now.");

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
     * Notifies all players about the activation of the UpgradeCard SpamBlocker.
     *
     * @param playerID The ID of the player who activated the UpgradeCard SpamBlocker.
     */
    public void sendUsingSpamBlockerNotification (final int playerID, final String[] upgradeCard) {
        for (PlayerController pc : this.getRemotePlayers()) {
            YourCardsModel yourCardsModel = new YourCardsModel(pc.getClientInstance(), upgradeCard);
            yourCardsModel.send();
         }
    }

    /**
     * Sends a notification to all  players to inform about the selection of a register because of the AdminPriviledge Card.
     */
    public void sendRegisterChosenNotification(final int playerID, int register) {
        for (PlayerController playerController : getRemotePlayers()) {
            RegisterChosenModel registerChosenModel = new RegisterChosenModel(playerController.getClientInstance(), playerID, register);
            registerChosenModel.send();
        }
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

    public void sendIncompleteProgrammingCards(final PlayerController tPC, final String[] hand)
    {
        new CardsYouGotNowModel(tPC.getClientInstance(), hand).send();
        return;
    }

    public void broadcastProgrammingTimerStart()
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new TimerStartedModel(pc.getClientInstance()).send();
            continue;
        }

        return;
    }

    /** @param ctrlIDs The players that have not finished programming in time. */
    public void broadcastProgrammingTimerFinish(final int[] ctrlIDs)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new TimerEndedModel(pc.getClientInstance(), ctrlIDs).send();
            continue;
        }

        return;
    }

    public void broadcastProgrammingSelectionFinished(final int playerID)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            SelectionFinishedModel selectionFinishedModel = new SelectionFinishedModel(pc.getClientInstance(), playerID);
            selectionFinishedModel.send();

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

    public void broadcastAnimation(final EAnimation anim)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            new AnimationModel(pc.getClientInstance(), anim).send();
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
        final ArrayList<PlayerController> t = new ArrayList<PlayerController>();
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

    public ArrayList<PlayerController> getAgentRemotePlayers()
    {
        final ArrayList<PlayerController> t = new ArrayList<PlayerController>();
        for (final IOwnershipable ctrl : this.ctrls)
        {
            if (ctrl instanceof PlayerController && ((PlayerController) ctrl).isRemoteAgent())
            {
                t.add((PlayerController) ctrl);
                continue;
            }

            continue;
        }

        return t;
    }

    public ArrayList<PlayerController> getHumanRemotePlayers()
    {
        final ArrayList<PlayerController> t = new ArrayList<PlayerController>();
        for (final IOwnershipable ctrl : this.ctrls)
        {
            if (ctrl instanceof PlayerController && !((PlayerController) ctrl).isRemoteAgent())
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

        final Agent a = EServerInstance.createNewAgent(this);
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

    private PlayerController getPlayerControllerByID(final int id)
    {
        for (final PlayerController pc : this.getRemotePlayers())
        {
            if (pc.getPlayerID() == id)
            {
                return pc;
            }

            continue;
        }

        return null;
    }

    public static int getSessionGameStartDelay()
    {
        if (Types.EConfigurations.isDev())
        {
            l.warn("The session game start timeout is reduced because the server is in development mode.");
            return Session.DEV_SESSION_GAME_START_DELAY;
        }

        return Session.PROD_SESSION_GAME_START_DELAY;
    }

    public boolean isFull()
    {
        return this.ctrls.size() >= GameState.MAX_CONTROLLERS_ALLOWED;
    }

    private boolean isLegacy()
    {
        for (final IOwnershipable ctrl : this.getControllers())
        {
            if (ctrl instanceof Agent)
            {
                return true;
            }

            continue;
        }

        return false;
    }

    public boolean hasStarted()
    {
        return this.gameState.hasGameStarted();
    }

    public IOwnershipable getOwnershipableByID(final int playerID)
    {
        for (final IOwnershipable ctrl : this.getControllers())
        {
            if (ctrl.getPlayerID() == playerID)
            {
                return ctrl;
            }

            continue;
        }

        l.error("Could not find any ownershipable with the ID {}.", playerID);
        return null;
    }

    // endregion Getters and Setters

}
