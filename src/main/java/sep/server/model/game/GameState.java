package sep.server.model.game;

import sep.server.viewmodel.        PlayerController;
import sep.server.viewmodel.        Session;
import sep.server.model.            IOwnershipable;
import sep.server.model.            Agent;
import sep.server.model.            EServerInformation;
import sep.server.model.game.tiles. Coordinate;

import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/**
 * High-level supervisor for the entirety of a session. It manages the creation, destruction and activation of
 * Game Modes. It is persistent throughout the life-time of a session.
 */
public final class GameState
{
    private static final Logger l = LogManager.getLogger(GameState.class);

    private static final String[]   AVAILABLE_COURSES                           = new String[] { "Dizzy Highway", "Lost Bearings", "Extra Crispy", "Death Trap", "Twister"};

    public static final int         MIN_CONTROLLERS_ALLOWED                     = 2;
    public static final int         MAX_CONTROLLERS_ALLOWED                     = 6;
    public static final int         DEFAULT_MIN_REMOTE_PLAYER_COUNT_TO_START    = 1;
    public static final int         DEFAULT_MIN_HUMAN_PLAYER_COUNT_TO_START     = 1;

    private String          courseName;
    private GameMode        gameMode;
    private final Session   session;
    private boolean         bGameStarted;

    /** @deprecated */
    private final int       minRemotePlayerCountToStart;
    private final int       minHumanPlayerCountToStart;

    public GameState(final Session session)
    {
        super();

        this.courseName                     = "";
        this.gameMode                       = null;
        this.session                        = session;
        this.bGameStarted                   = false;

        /* In the future, we might want to have this session independent. This is why we save the value here. */
        this.minRemotePlayerCountToStart    = EServerInformation.INSTANCE.getMinRemotePlayerCountToStart();
        this.minHumanPlayerCountToStart     = EServerInformation.INSTANCE.getMinHumanPlayerCountToStart();

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
                this.session.broadcastCorePlayerAttributes(a);
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

    public synchronized void setStartPoint(final PlayerController playerController, final Coordinate coordinate)
    {
        this.gameMode.setStartingPoint(playerController, coordinate.getX(), coordinate.getY());
        return;
    }

    public void setRebootDirection(PlayerController playerController, String direction) {
        playerController.getPlayer().getPlayerRobot().setDirection(direction);

        switch(direction) {
            case "right" -> this.getSession().broadcastRotationUpdate(playerController.getPlayerID(), "counterclockwise");
            case "bottom" -> {
                this.getSession().broadcastRotationUpdate(playerController.getPlayerID(), "counterclockwise");
                this.getSession().broadcastRotationUpdate(playerController.getPlayerID(), "counterclockwise");
            }
            case "left" -> this.getSession().broadcastRotationUpdate(playerController.getPlayerID(), "clockwise");
        }
    }

    /** @deprecated  */
    public void setMemorySwapCards (PlayerController playerController, String[] cards) {
        playerController.getPlayer().setMemorySwapCards(cards);
    }

    /** @deprecated  */
    public void setRegisterForAdminPriviledge (PlayerController playerController, int register) {
        playerController.getPlayer().setChosenRegisterAdminPrivilegeUpgrade(register);
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

    /** @deprecated */
    public int getMinRemotePlayersToStart()
    {
        return this.minRemotePlayerCountToStart;
    }

    public int getMinHumanPlayersToStart()
    {
        return this.minHumanPlayerCountToStart;
    }

    // endregion Getters and Setters

}
