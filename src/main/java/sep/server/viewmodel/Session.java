package sep.server.viewmodel;

import sep.server.model.game.GameState;
import sep.server.model.EServerInformation;
import sep.server.json.lobby.PlayerValuesModel;
import sep.server.json.common.ChatMsgModel;
import sep.server.json.lobby.PlayerReadyModel;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Handles how clients can join and leave a session and also how to communicate with each other. For example,
 * chat messages. If a client disconnects mid-game, this class must handle that as well. (Wait for reconnection
 * or replace that player by an AI. We will probably have to handle this later on in an upcoming milestone.)
 */
public final class Session
{
    private static final int DEFAULT_SESSION_ID_LENGTH = 5;

    private final ArrayList<PlayerController> playerControllers;
    private final String sessionID;

    private final GameState gameState;

    public Session()
    {
        this(Session.generateSessionID());
        return;
    }

    public Session(String sessionID)
    {
        super();

        this.playerControllers = new ArrayList<PlayerController>();
        this.sessionID = sessionID;
        this.gameState = new GameState();

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
            new PlayerValuesModel(newPC, PC.getPlayerID(), PC.getPlayerName(), PC.getFigure()).send();
            continue;
        }

        /* Sending information about the new client to all other clients. */
        for (PlayerController PC : this.playerControllers)
        {
            if (PC.getPlayerID() == newPC.getPlayerID())
            {
                continue;
            }

            new PlayerValuesModel(PC, newPC.getPlayerID(), newPC.getPlayerName(), newPC.getFigure()).send();

            continue;
        }

        this.broadcastChatMessage(ChatMsgModel.SERVER_ID, String.format("%s joined the session.", newPC.getPlayerName()));

        return;
    }

    public void sendPlayerValuesToAllClients(PlayerController changedPC)
    {
        for (PlayerController PC : this.playerControllers)
        {
            new PlayerValuesModel(PC, changedPC.getPlayerID(), changedPC.getPlayerName(), changedPC.getFigure()).send();
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
            new PlayerReadyModel(PC.getClientInstance(), playerController.getPlayerID(), playerController.isReady()).send();
            continue;
        }

        return;
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

    // endregion Getters and Setters

}
