package sep.server.model.game;

import sep.server.viewmodel.PlayerController;

/**
 * The rules of the game are implemented here. It is a high-level manager object for one game and controls the
 * overall flow of it. It is spawned at the start of a game and not destroyed until the game has ended.
 */
public class GameMode
{
    public GameMode(String course, PlayerController[] playerControllers)
    {
    }
}
