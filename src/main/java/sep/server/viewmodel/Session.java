package sep.server.viewmodel;

import sep.server.model.game.GameState;
import sep.server.model.EServerInformation;
import sep.server.json.lobby.PlayerValuesModel;
import sep.server.json.ChatMsgModel;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Handles how clients can join and leave a session, which of the clients is the current host and also how to
 * communicate with each other. For example, chat messages. If a client disconnects mid-game, this class will handle
 * that as well. (Wait for reconnection or replace that player by an AI. We will probably have to handle this later
 * on in an upcoming milestone.)
 */
public final class Session
{
    /** @deprecated */
    private static final int SESSION_ID_LENGTH = 5;

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

    private static String generateSessionID()
    {
        String t = UUID.randomUUID().toString().substring(0, Session.SESSION_ID_LENGTH);
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

    public String getSessionID()
    {
        return sessionID;
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
        // Sending all current player values to the new client.
        for (PlayerController PC : this.playerControllers)
        {
            new PlayerValuesModel(newPC, PC.getPlayerID(), PC.getPlayerName(), PC.getFigure()).send();
            continue;
        }

        // Sending information about the new client to all other clients.
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

}
