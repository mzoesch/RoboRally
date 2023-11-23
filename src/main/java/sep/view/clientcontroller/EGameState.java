package sep.view.clientcontroller;

import sep.view.json.DefaultServerRequestParser;
import sep.view.viewcontroller.ViewLauncher;

import java.util.ArrayList;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holds the state of the game. Like player positions, player names, cards in hand, cards on table, etc.
 * Does not contain actual game logic. If the view needs to know something about the game, it will be stored here. This
 * object is shared across all threads and is automatically updated by the server listener.
 */
public enum EGameState
{
    INSTANCE;

    private static final Logger l = LogManager.getLogger(EGameState.class);

    public static final int INVALID_FIGURE_ID = -1;
    public static final String[] FIGURE_NAMES = new String[] {"Hulk x90", "Spin Bot", "Hammer Bot", "Twonky", "Trundle Bot", "Twitch", "Squash Bot"};
    public static final int MAX_CHAT_MESSAGE_LENGTH = 64;

    /**
     * Stores information that is shared for all players. The player cards for one client are unique to them and must
     * be stored here in the Game State. Information that is not unique for one player like their selected robot or
     * their name is stored in the {@link RemotePlayer} object.
     */
    private ArrayList<RemotePlayer> remotePlayers;

    // HERE
    // The current course with its midfielders...
    // The current programming cards of this client.
    // The current registers of this client.
    // ...

    private EGameState()
    {
        this.remotePlayers = new ArrayList<RemotePlayer>();
        return;
    }

    public static void reset()
    {
        EGameState.INSTANCE.remotePlayers = new ArrayList<RemotePlayer>();
        return;
    }

    // region Getters and Setters

    private RemotePlayer getRemotePlayer(int playerID)
    {
        for (RemotePlayer rp : EGameState.INSTANCE.remotePlayers)
        {
            if (rp.getPlayerID() == playerID)
            {
                return rp;
            }
        }

        return null;
    }

    private boolean isRemotePlayerAlreadyAdded(int playerID)
    {
        for (RemotePlayer rp : EGameState.INSTANCE.remotePlayers)
        {
            if (rp.getPlayerID() == playerID)
            {
                return true;
            }
        }

        return false;
    }

    public static void addRemotePlayer(DefaultServerRequestParser dsrp)
    {
        // TODO This is not save at all. More type checking needed.

        if (EGameState.INSTANCE.isRemotePlayerAlreadyAdded(dsrp.getPlayerID()))
        {
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayer(dsrp.getPlayerID())).setPlayerName(dsrp.getPlayerName());
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayer(dsrp.getPlayerID())).setFigureID(dsrp.getFigureID());
            ViewLauncher.updatePlayerSelection();

            l.debug("Remote player {} already added. Updating his name and figures.", dsrp.getPlayerID());
            return;
        }

        RemotePlayer rp = new RemotePlayer(dsrp.getPlayerID(), dsrp.getPlayerName(), dsrp.getFigureID());
        EGameState.INSTANCE.remotePlayers.add(rp);
        ViewLauncher.updatePlayerSelection();

        return;
    }

    /** If the robot at a specific index is already selected by a player. */
    public boolean isPlayerRobotUnavailable(int idx)
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getFigureID() == idx)
            {
                return true;
            }
        }

        return false;
    }

    /** If this client has already selected a robot. */
    public boolean hasClientSelectedARobot()
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID() && rp.getFigureID() != EGameState.INVALID_FIGURE_ID)
            {
                return true;
            }
        }

        return false;
    }

    public RemotePlayer getRemotePlayerByFigureID(int idx)
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getFigureID() == idx)
            {
                return rp;
            }
        }

        return null;
    }

    public RemotePlayer getRemotePlayerByPlayerID(int caller)
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == caller)
            {
                return rp;
            }
        }

        return null;
    }

    public RemotePlayer getRemotePlayerByPlayerName(String targetPlayer)
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerName().equals(targetPlayer))
            {
                return rp;
            }
        }

        return null;
    }

    // endregion Getters and Setters

}
