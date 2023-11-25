package sep.server.model.game;

import sep.server.viewmodel.PlayerController;

/**
 * High-level supervisor for the entirety of a session. It manages the creation, destruction and activation of
 * Game Modes. It is persistent throughout the life-time of a session.
 */
public class GameState
{
    private GameMode gameMode;
    private boolean bGameStarted;

    public GameState()
    {
        this.bGameStarted = false;
        return;
    }

    /** The host of a session will be able to call this method. */
    public void startGame(PlayerController[] playerControllers /* etc. config */)
    {
        this.bGameStarted = true;
        this.gameMode = new GameMode(this.courseName, playerControllers);

        System.out.printf("Game started with %d players.\n", playerControllers.length);

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
        return;
    }

    // endregion Getters and Setters

}
