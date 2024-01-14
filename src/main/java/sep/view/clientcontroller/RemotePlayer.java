package sep.view.clientcontroller;

import sep.view.viewcontroller.RobotView;
import sep.view.lib.RCoordinate;
import sep.view.lib.EFigure;

import java.util.ArrayList;

/**
 * Represents a player in the lobby. Not just remote players but also the client player.
 * We may store information on the remote player state here that is unique to a player and not to the game. This
 * class should not contain any kind of logic it is just the long-term storage solution from the {@link ServerListener}.
 */
public final class RemotePlayer
{
    private final int playerID;
    private String playerName;
    private EFigure figure;
    private boolean bReady;

    private RCoordinate startPos;
    private RobotView possessing;

    private static final int REGISTER_SLOTS = 5;
    private String[] registerSlots;

    private int energyCubes = 5;

    private boolean bSelectionFinished;
    private int checkPointsReached;

    private final ArrayList<String> playedRCards;

    public RemotePlayer(final int playerID, final String playerName, final EFigure figure, final boolean bReady)
    {
        this.playerID = playerID;
        this.playerName = playerName;
        this.figure = figure;
        this.bReady = bReady;

        this.startPos = null;
        this.possessing = new RobotView(this);

        this.registerSlots = new String[REGISTER_SLOTS];

        this.bSelectionFinished = false;
        this.checkPointsReached = 0;

        this.playedRCards = new ArrayList<String>();

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

    public EFigure getFigure()
    {
        return this.figure;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
        return;
    }

    public void setFigure(final EFigure figure)
    {
        this.figure = figure;
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

    public RCoordinate getStartingPosition()
    {
        return this.startPos;
    }

    public void setStartingPosition(RCoordinate startPos)
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

    public boolean hasSelectionFinished()
    {
        return this.bSelectionFinished;
    }

    public void setSelectionFinished(final boolean b)
    {
        this.bSelectionFinished = b;
        return;
    }

    public int getEnergyCubes() {
        return energyCubes;
    }

    public void setEnergy(int number){
        energyCubes = number;
    }

    public void setCheckPointsReached(int number){ checkPointsReached = number;}

    public void clearPlayedRCards()
    {
        this.playedRCards.clear();
        return;
    }

    public void addPlayedRCards(final String card)
    {
        this.playedRCards.add(card);
        return;
    }

    public String[] getPlayedRCards()
    {
        return this.playedRCards.toArray(new String[0]);
    }

    public RCoordinate getFigureLocation()
    {
        return this.possessing.getPosition();
    }

}
