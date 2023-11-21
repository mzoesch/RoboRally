package sep.view.clientcontroller;

/**
 * Represents a player in the lobby. Not just remote players but also the client player.
 * We may store information on the remote player state here that is unique to a player and not to the game. This
 * class should not contain any kind of logic it is just the long-term storage solution from the {@link ServerListener}.
 */
public final class RemotePlayer
{
    private final int playerID;
    private String playerName;
    private int figureID;

    public RemotePlayer(int playerID, String playerName, int figureID)
    {
        this.playerID = playerID;
        this.playerName = playerName;
        this.figureID = figureID;

        return;
    }

    public int getPlayerID()
    {
        return this.playerID;
    }

    public String getPlayerName()
    {
        return this.playerName;
    }

    public int getFigureID()
    {
        return this.figureID;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
        return;
    }

    public void setFigureID(int figureID)
    {
        this.figureID = figureID;
        return;
    }

}
