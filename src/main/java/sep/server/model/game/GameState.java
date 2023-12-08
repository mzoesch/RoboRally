package sep.server.model.game;

import sep.server.viewmodel.PlayerController;

import sep.server.viewmodel.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * High-level supervisor for the entirety of a session. It manages the creation, destruction and activation of
 * Game Modes. It is persistent throughout the life-time of a session.
 */
public class GameState
{
    private static final Logger l = LogManager.getLogger(GameState.class);

    private static final String[] AVAILABLE_COURSES = new String[] { "Dizzy Highway", "DEBUG COURSE ONE", "DEBUG COURSE TWO", "DEBUG COURSE THREE" };

    // TODO As described in Protocol v0.1, this var should be passed as a cmd program argument
    public static final int MIN_PLAYER_START = 2;

    private String courseName;

    public static GameMode gameMode;
    private final Session session;

    private boolean bGameStarted;

    public GameState(Session session)
    {
        super();
        this.courseName = "";
        this.session = session;
        this.bGameStarted = false;
        return;
    }

    public void startGame(PlayerController[] playerControllers)
    {
        l.info("Creating Game Mode.");

        //Erstellen des Spiels
        this.bGameStarted = true;
        gameMode = new GameMode(this.courseName, playerControllers, this.session);

        l.info("Game Mode created. The game has started with {} players.", playerControllers.length);

        return;
    }

    // region Getters and Setters

    public GameMode getAuthGameMode()
    {
        return gameMode;
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

    public void sendShuffle(Player player){
        session.sendShuffleCodingNotification(player.getPlayerController().getPlayerID());
    }

    public void sendStartTimer(){
        session.sendTimerStarted();
    }

    public void sendStopTimer(int[] playerIDS) {
        session.sendTimerEnded(playerIDS);
    }

    public void setStartingPoint(PlayerController playerController, int x, int y){
        gameMode.setStartingPoint(playerController, x,y);
    }



    // endregion Getters and Setters



}
