package sep.view.clientcontroller;

import sep.view.viewcontroller.RobotView;
import sep.view.lib.Coordinate;

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
    private boolean bReady;

    Coordinate startPos;
    RobotView possessing;

    private static final int REGISTER_SLOTS = 5;
    private String[] registerSlots;

    public RemotePlayer(int playerID, String playerName, int figureID, boolean bReady)
    {
        this.playerID = playerID;
        this.playerName = playerName;
        this.figureID = figureID;
        this.bReady = bReady;

        this.startPos = null;
        this.possessing = new RobotView(this);

        this.registerSlots = new String[REGISTER_SLOTS];

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

    public boolean isReady()
    {
        return this.bReady;
    }

    public void setReady(boolean bReady)
    {
        this.bReady = bReady;
        return;
    }

    public boolean hasStartingPosition()
    {
        return this.startPos != null;
    }

    public Coordinate getStartingPosition()
    {
        return this.startPos;
    }

    public void setStartingPosition(Coordinate startPos)
    {
        this.startPos = startPos;
        return;
    }

    public RobotView getRobotView()
    {
        return this.possessing;
    }

    public String getRegisterSlot(int i)
    {
        return this.registerSlots[i];
    }

    public int countCheckPoints() {
        int checkpoint = 0;
        for (String card : registerSlots) {
            if ("CheckPoint".equals(card)) {
                checkpoint++;
            }
        }
        return checkpoint;
    }

    public int countEnergy() {
        int energy = 0;
        for (String card : registerSlots) {
            if ("Energy".equals(card)) {
                energy++;
            }
        }
        return energy;
    }

}
