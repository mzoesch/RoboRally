package sep.server.model.game;

import sep.server.viewmodel.    PlayerController;
import sep.server.viewmodel.    Session;
import sep.server.model.        IOwnershipable;
import sep.server.model.        Agent;
import sep.server.model.        EServerInformation;

import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/**
 * High-level supervisor for the entirety of a session. It manages the creation, destruction and activation of
 * Game Modes. It is persistent throughout the life-time of a session.
 */
public class GameState
{
    private static final Logger l = LogManager.getLogger(GameState.class);

    private static final String[]   AVAILABLE_COURSES                           = new String[] { "Dizzy Highway", "Lost Bearings", "Extra Crispy", "Death Trap" };

    public static final int         MIN_CONTROLLERS_ALLOWED                     = 2;
    public static final int         MAX_CONTROLLERS_ALLOWED                     = 6;
    public static final int         DEFAULT_MIN_REMOTE_PLAYER_COUNT_TO_START    = 1;
    public static final int         DEFAULT_MIN_HUMAN_PLAYER_COUNT_TO_START     = 1;

    private String          courseName;
    private GameMode        gameMode;
    private final Session   session;
    private boolean         bGameStarted;
    private final int       minRemotePlayerCountToStart;

    public GameState(final Session session)
    {
        super();

        this.courseName = "";
        this.gameMode = null;
        this.session = session;
        this.bGameStarted = false;
        this.minRemotePlayerCountToStart = EServerInformation.INSTANCE.getMinRemotePlayerCountToStart();

        return;
    }

    public void startGame()
    {
        l.info("Creating Game Mode.");

        l.debug("Found {} agents.{}", this.session.getAgents().length, this.session.getAgents().length > 0 ? " Preparing them for game start." : "");
        for (final Agent a : this.session.getAgents())
        {
            if (a.getFigure() == IOwnershipable.INVALID_FIGURE)
            {
                a.setFigure(this.getNextAvailableFigure());
                this.session.sendPlayerValuesToAllClients(a);
            }

            continue;
        }

        this.bGameStarted = true;
        this.gameMode = new GameMode(this.courseName, this);

        l.info("Game Mode created. The game has started with {} controllers.", this.getControllers().length);

        return;
    }

    public void onClose() throws InterruptedException
    {
        if (this.gameMode != null)
        {
            this.gameMode.onClose();
            this.gameMode = null;
        }

        l.debug("Game State of Session [{}] closed successfully.", this.session.getSessionID());

        return;
    }

    // region Getters and Setters

    public GameMode getAuthGameMode()
    {
        return this.gameMode;
    }

    public boolean hasGameStarted()
    {
        return this.bGameStarted;
    }

    public static String[] getAvailableCourses()
    {
        return AVAILABLE_COURSES;
    }

    public String getCourseName()
    {
        return courseName;
    }

    public void setCourseName(String courseName)
    {
        this.courseName = courseName;
        l.info("CourseName set to: " + courseName);
    }

    public void sendStartTimer(){
        session.sendTimerStarted();
    }

    public void sendStopTimer(int[] playerIDS) {
        session.sendTimerEnded(playerIDS);
    }

    public void setStartingPoint(PlayerController playerController, int x, int y){
        this.gameMode.setStartingPoint(playerController, x,y);
    }

    public void setRebootDirection(PlayerController playerController, String direction) {
        playerController.getPlayer().getPlayerRobot().setDirection(direction);
    }

    public Session getSession()
    {
        return session;
    }

    public IOwnershipable[] getControllers()
    {
        return this.session.getControllers();
    }

    private int getNextAvailableFigure()
    {
        int tFigure = 0;
        while (true)
        {
            boolean bFound = false;

            for (final IOwnershipable ctrl : this.getControllers())
            {
                if (ctrl.getFigure() == tFigure)
                {
                    bFound = true;
                    break;
                }
            }

            if (!bFound)
            {
                return tFigure;
            }

            tFigure++;

            continue;
        }
    }

    public int getMinRemotePlayersToStart()
    {
        return this.minRemotePlayerCountToStart;
    }

    // endregion Getters and Setters

}
