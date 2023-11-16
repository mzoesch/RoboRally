package sep.view.clientcontroller;

import sep.view.json.DefaultServerRequestParser;

/**
 * Holds the state of the game. Like player positions, player names, cards in hand, cards on table, etc.
 * Does not contain actual game logic. If the view needs to know something about the game, it will be stored here. This
 * object is shared across all threads and should be automatically updated by the server listener.
 */
public enum EGameState
{
    INSTANCE;

    private String[] playerNames;
    private String hostPlayerName;

    private EGameState()
    {
        this.playerNames = null;
        this.hostPlayerName = null;

        return;
    }

    public static void reset()
    {
        INSTANCE.playerNames = null;
        INSTANCE.hostPlayerName = null;

        return;
    }

    public void update(DefaultServerRequestParser dsrp)
    {
        try
        {
            this.playerNames = dsrp.getPlayerNames();
            this.hostPlayerName = dsrp.getHostPlayerName();
        }
        catch (Exception e)
        {
            System.err.println("[CLIENT] Failed to update game state.");
            System.err.println(e.getMessage());
        }

        return;
    }

    public String[] getPlayerNames()
    {
        return this.playerNames;
    }

    public String getHostPlayerName()
    {
        return this.hostPlayerName;
    }

}
