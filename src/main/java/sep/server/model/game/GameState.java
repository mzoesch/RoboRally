package sep.server.model.game;

import sep.server.viewmodel.PlayerController;

import org.json.JSONObject;

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
    public void startGame(PlayerController[] playerControllers, String course /* etc. config */)
    {
        this.bGameStarted = true;
        this.gameMode = new GameMode(course, playerControllers);

        return;
    }

    public GameMode getAuthGameMode()
    {
        return gameMode;
    }

    public boolean hasGameStarted()
    {
        return this.bGameStarted;
    }

    /**
     * Should return a JSON object that represents the state of the game to sync to all clients.
     * They will have to figure out how to interpret it and update the view accordingly.
     */
    public JSONObject getUpdatedState()
    {
        return new JSONObject();
    }

}
