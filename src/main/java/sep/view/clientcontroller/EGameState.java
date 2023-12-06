package sep.view.clientcontroller;

import sep.view.json.DefaultServerRequestParser;
import sep.view.viewcontroller.ViewSupervisor;
import sep.view.lib.EGamePhase;

import java.util.ArrayList;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

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
    public static final String[] FIGURE_NAMES = new String[] {"Hammer Bot", "Trundle Bot", "Squash Bot", "Hulk x90", "Spin Bot", "Twonky", "Twitch"};
    public static final int MAX_CHAT_MESSAGE_LENGTH = 64;

    public static final String[] PHASE_NAMES = new String[] {"Registration Phase", "Upgrade Phase", "Programming Phase", "Activation Phase"};
    private EGamePhase currentPhase;

    /**
     * Stores information that is shared for all players. The player cards for one client are unique to them and must
     * be stored here in the Game State. Information that is not unique for one player like their selected robot or
     * their name is stored in the {@link RemotePlayer} object.
     */
    private ArrayList<RemotePlayer> remotePlayers;
    private RemotePlayer currentPlayer;

    private String[] serverCourses;
    private String currentServerCourse;
    private JSONArray currentServerCourseJSON;

    private final String[] registers;
    private final ArrayList<String> gotRegisters;

    private EGameState()
    {
        this.remotePlayers = new ArrayList<RemotePlayer>();
        this.serverCourses = new String[0];
        this.currentServerCourse = "";
        this.currentServerCourseJSON = null;
        this.currentPhase = EGamePhase.INVALID;

        this.registers = new String[5];
        this.gotRegisters = new ArrayList<String>();

        return;
    }

    public static void reset()
    {
        EGameState.INSTANCE.remotePlayers = new ArrayList<RemotePlayer>();
        EGameState.INSTANCE.serverCourses = new String[0];
        EGameState.INSTANCE.currentServerCourse = "";
        EGameState.INSTANCE.currentServerCourseJSON = null;

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
        // TODO This is not safe at all. More type checking needed.

        if (EGameState.INSTANCE.isRemotePlayerAlreadyAdded(dsrp.getPlayerID()))
        {
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayer(dsrp.getPlayerID())).setPlayerName(dsrp.getPlayerName());
            Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayer(dsrp.getPlayerID())).setFigureID(dsrp.getFigureID());
            ViewSupervisor.updatePlayerSelection();

            l.debug("Remote player {} already added. Updating his name and figures.", dsrp.getPlayerID());
            return;
        }

        RemotePlayer rp = new RemotePlayer(dsrp.getPlayerID(), dsrp.getPlayerName(), dsrp.getFigureID(), false);
        EGameState.INSTANCE.remotePlayers.add(rp);
        ViewSupervisor.updatePlayerSelection();

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

    public int getClientSelectedRobotID()
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
            {
                return rp.getFigureID();
            }
        }

        return EGameState.INVALID_FIGURE_ID;
    }

    public RemotePlayer[] getRemotePlayers()
    {
        return this.remotePlayers.toArray(new RemotePlayer[0]);
    }

    public RemotePlayer getClientRemotePlayer()
    {
        for (RemotePlayer rp : this.remotePlayers)
        {
            if (rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
            {
                return rp;
            }
        }

        return null;
    }

    public String[] getServerCourses()
    {
        return this.serverCourses;
    }

    public void setServerCourses(String[] serverCourses)
    {
        this.serverCourses = serverCourses;
        return;
    }

    public String getCurrentServerCourse()
    {
        return this.currentServerCourse;
    }

    public void setCurrentServerCourse(String currentServerCourse)
    {
        this.currentServerCourse = currentServerCourse;
        return;
    }

    public JSONArray getCurrentServerCourseJSON()
    {
        return this.currentServerCourseJSON;
    }

    public void setCurrentServerCourseJSON(JSONArray currentServerCourseJSON)
    {
        this.currentServerCourseJSON = currentServerCourseJSON;
        return;
    }

    public EGamePhase getCurrentPhase()
    {
        return this.currentPhase;
    }

    public void setCurrentPhase(EGamePhase currentPhase)
    {
        if (this.currentPhase == currentPhase)
        {
            return;
        }

        // TODO
        //      We exited the registration phase, and there will be no more clickable actions on the course view,
        //      therefore, we re-render the course view to remove the hover effect. Note, this is not efficient, we
        //      must implement a faster way, where we just remove the hover effect and not re-render the whole
        //      course view.
        if (this.currentPhase == EGamePhase.REGISTRATION)
        {
            ViewSupervisor.updateCourseView();
        }

        this.currentPhase = currentPhase;
        if (this.currentPhase == EGamePhase.PROGRAMMING)
        {
//            this.clearAllRegisters();
            this.currentPlayer = null;
        }
        ViewSupervisor.updatePhase();
        return;
    }

    public void setCurrentPlayer(int playerID)
    {
        this.currentPlayer = this.getRemotePlayer(playerID);
        ViewSupervisor.updatePlayerView();
        return;
    }

    public RemotePlayer getCurrentPlayer()
    {
        return this.currentPlayer;
    }

    public String getRegister(int idx)
    {
        if (idx < 0 || idx >= this.registers.length)
        {
            return null;
        }

        return this.registers[idx];
    }

    public String getGotRegister(int idx)
    {
        if (idx < 0 || idx >= this.gotRegisters.size())
        {
            return null;
        }

        return this.gotRegisters.get(idx);
    }

    public String[] getRegisters()
    {
        return this.registers;
    }

    public ArrayList<String> getGotRegisters()
    {
        return this.gotRegisters;
    }

    public void clearAllRegisters()
    {
        this.registers[0] = null;
        this.registers[1] = null;
        this.registers[2] = null;
        this.registers[3] = null;
        this.registers[4] = null;
        this.gotRegisters.clear();
        return;
    }

    public void addRegister(int idx, String register)
    {
        if (idx < 0 || idx >= this.registers.length)
        {
            return;
        }

        this.registers[idx] = register;

        return;
    }

    public void addGotRegister(String register)
    {
        this.gotRegisters.add(register);
        return;
    }


    /**
     * Sets a register slot from a given got register slot.
     *
     * @param tIdx Target index
     * @param oIdx Origin index
     */
    public void setRegister(int tIdx, int oIdx)
    {
        if (tIdx < 0 || tIdx >= this.registers.length)
        {
            return;
        }

        if (oIdx < 0 || oIdx >= this.gotRegisters.size())
        {
            return;
        }

        if (this.registers[tIdx] != null)
        {
            return;
        }

        this.registers[tIdx] = this.gotRegisters.get(oIdx);
        this.gotRegisters.set(oIdx, null);

        return;
    }

    /**
     * Undoes a set register slot and add it back to the got registers.
     *
     * @param oIdx     Origin index from register
     */
    public void undoRegister(int oIdx)
    {
        if (oIdx < 0 || oIdx >= this.registers.length)
        {
            return;
        }

        if (this.registers[oIdx] == null)
        {
            return;
        }

        if (!this.gotRegisters.contains(null))
        {
            l.error("Could not undo register. Got registers are full.");
            return;
        }

        this.gotRegisters.set(this.gotRegisters.indexOf(null), this.registers[oIdx]);
        this.registers[oIdx] = null;

        return;

    }

    public boolean areRegistersFull()
    {
        for (String s : this.registers)
        {
            if (s == null)
            {
                return false;
            }
        }

        return true;
    }

    public void setSelectionFinished(final int playerID)
    {
        Objects.requireNonNull(this.getRemotePlayer(playerID)).setSelectionFinished(true);
        // TODO Also not efficient here. We must not update the whole HUD, but only the player view and if not already
        //      existing, the new timer view.
        ViewSupervisor.updatePlayerView();
        return;
    }

    // endregion Getters and Setters

}
