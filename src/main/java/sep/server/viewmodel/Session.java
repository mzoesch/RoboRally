package sep.server.viewmodel;

import sep.server.json.SessionStateModel;
import sep.server.model.game.GameState;
import sep.server.model.EServerInformation;

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
    private static final int SESSION_ID_LENGTH = 5;

    /** May have more rights. (Initial game-start and change the config of it.) */
    private PlayerController hostPlayerController;
    private final ArrayList<PlayerController> playerControllers;
    private final String sessionID;

    private final GameState gameState;

    public Session()
    {
        super();

        this.hostPlayerController = null;
        this.playerControllers = new ArrayList<PlayerController>();
        this.sessionID = Session.generateSessionID();
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

    public PlayerController getHostPlayerController()
    {
        return hostPlayerController;
    }

    public void setHostPlayerController(PlayerController hostPlayerController)
    {
        this.hostPlayerController = hostPlayerController;
        return;
    }

    public void joinSession(PlayerController playerController)
    {
        this.playerControllers.add(playerController);

        if (this.hostPlayerController == null)
        {
            this.hostPlayerController = playerController;
        }

        return;
    }

    public void leaveSession(PlayerController playerController)
    {
        this.playerControllers.remove(playerController);

        if (this.playerControllers.isEmpty())
        {
            EServerInformation.INSTANCE.removeSession(this);
            return;
        }

        if (this.hostPlayerController == playerController)
        {
            this.hostPlayerController = this.playerControllers.get(0);
        }

        this.broadcastChatMessage("SERVER", String.format("%s left the session.", playerController.getPlayerName()));
        this.replicateSessionStateToClients();

        return;
    }

    public void broadcastChatMessage(String caller, String message)
    {
        // TODO Validate message.
        if (message.isEmpty())
        {
            return;
        }

        for (PlayerController PC : this.playerControllers)
        {
            PC.sendChatMessage(caller, message);
            continue;
        }

        return;
    }

    public void defaultBehaviourAfterPostLogin(PlayerController playerController)
    {
        this.broadcastChatMessage("SERVER", String.format("%s joined the session.", playerController.getPlayerName()));
        this.replicateSessionStateToClients();

        return;
    }

    private void replicateSessionStateToClients()
    {
        String[] playerNames = new String[this.playerControllers.size()];
        for (int i = 0; i < this.playerControllers.size(); i++)
        {
            playerNames[i] = this.playerControllers.get(i).getPlayerName();
            continue;
        }

        for (PlayerController PC : this.playerControllers)
        {
            new SessionStateModel(PC.getClientInstance(), playerNames, this.hostPlayerController.getPlayerName()).send();
            continue;
        }

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
}

