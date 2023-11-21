package sep.view.clientcontroller;

public class RemotePlayer
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
