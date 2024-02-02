package sep.view.clientcontroller;

import sep.view.viewcontroller.     RobotView;
import sep.view.lib.                RCoordinate;
import sep.view.lib.                EFigure;
import sep.view.lib.                RRegisterCard;

import java.util.                   ArrayList;
import java.util.                   Objects;

/**
 * Represents a player in a session. Not just remote players but also the client player.
 * We may store information of the remote player state here that is unique to a player and not to the game. This
 * class should not contain any kind of logic it is just the long-term storage solution from the {@link ServerListener}.
 */
public sealed class RemotePlayer permits AgentRemotePlayerData
{
    private final int                   playerID;
    private String                      playerName;
    private EFigure                     figure;
    private boolean                     bReady;

    protected RCoordinate               startPos;
    /** Only updated by Human Server Listeners. */
    private final RobotView             possessing;

    private static final int            REGISTER_SLOTS      = 5;
    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    private final String[]              registerSlots;
    private boolean                     bRebooted;

    private static final int            START_ENERGY        = 5;
    private int                         energyCubes;

    private boolean                     bSelectionFinished;
    private int                         checkPointsReached;

    private final ArrayList<String>     playedRCards;

    private final ArrayList<String>     boughtUpgradeCards;

    public RemotePlayer(final int playerID, final String playerName, final EFigure figure, final boolean bReady)
    {
        this.playerID               = playerID;
        this.playerName             = playerName;
        this.figure                 = figure;
        this.bReady                 = bReady;

        this.startPos               = null;
        this.possessing             = new RobotView(this);

        this.registerSlots          = new String[RemotePlayer.REGISTER_SLOTS];
        this.bRebooted              = false;

        this.energyCubes            = RemotePlayer.START_ENERGY;

        this.bSelectionFinished     = false;
        this.checkPointsReached     = 0;

        this.playedRCards           = new ArrayList<String>();

        this.boughtUpgradeCards     = new ArrayList<String>();

        return;
    }

    // region Getters and Setters

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

    public void setPlayerName(final String name)
    {
        this.playerName = name;
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

    public void setReady(final boolean bReady)
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

    public void setStartingPosition(final RCoordinate location)
    {
        this.startPos = location;
        return;
    }

    public RobotView getRobotView()
    {
        return this.possessing;
    }

    public String getRegisterSlot(final int idx)
    {
        return this.registerSlots[idx];
    }

    public boolean hasSelectionFinished()
    {
        return this.bSelectionFinished;
    }

    public void setSelectionFinished(final boolean bFinished)
    {
        this.bSelectionFinished = bFinished;
        return;
    }

    public int getEnergy()
    {
        return this.energyCubes;
    }

    public void setEnergy(final int number)
    {
        this.energyCubes = number;
        return;
    }

    public void setCheckPointsReached(final int count)
    {
        this.checkPointsReached = count;
        return;
    }

    public void clearPlayedRCards()
    {
        this.playedRCards.clear();
        this.bRebooted = false;
        return;
    }

    public void addPlayedRCards(final String card)
    {
        /* If a pawn has been rebooted. */
        if (Objects.equals(card, RRegisterCard.NULL_CARD))
        {
            this.playedRCards.clear();
            return;
        }

        this.playedRCards.add(card);

        return;
    }

    public ArrayList<String> getPlayerRCardsInMockOnly()
    {
        return this.playedRCards;
    }

    public String[] getPlayedRCards()
    {
        return this.playedRCards.toArray(new String[0]);
    }

    public RCoordinate getFigureLocation()
    {
        return this.possessing.getPosition();
    }

    public int getCheckPointsReached() {
        return checkPointsReached;
    }

    @Override
    public String toString()
    {
        return String.format("RemotePlayer{id:%d,name:%s,figure:%s,ready:%b}", this.playerID, this.playerName, this.figure, this.bReady);
    }

    public boolean hasRebooted()
    {
        return this.bRebooted;
    }

    public void setHasRebooted(final boolean bRebooted)
    {
        this.bRebooted = bRebooted;
        return;
    }

    public ArrayList<String> getBoughtUpgradeCards()
    {
        return this.boughtUpgradeCards;
    }

    public boolean hasRearLaser()
    {
        return this.boughtUpgradeCards.contains("RearLaser");
    }

    // endregion Getters and Setters

}
