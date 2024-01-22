package sep.view.clientcontroller;

import sep.view.lib.                EFigure;
import sep.view.lib.                RCoordinate;

import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/** {@inheritDoc} */
public final class AgentRemotePlayerData extends RemotePlayer
{
    private static final Logger l = LogManager.getLogger(AgentRemotePlayerData.class);

    private RCoordinate     location;
    private int             rotation;

    public AgentRemotePlayerData(final int playerID, final String playerName, final EFigure figure, final boolean bReady)
    {
        super(playerID, playerName, figure, bReady);

        this.rotation = 0;

        return;
    }

    public boolean isRotatedNorth()
    {
        return this.rotation == 0;
    }

    public boolean isRotatedEast()
    {
        return this.rotation == 90;
    }

    public boolean isRotatedSouth()
    {
        return this.rotation == 180;
    }

    public boolean isRotatedWest()
    {
        return this.rotation == 270;
    }

    public int getRotation()
    {
        return this.rotation;
    }

    /** A rotation of zero is north. */
    public void setRotation(final int rotation)
    {
        this.rotation = (rotation % 360);
        return;
    }

    /** @param rotation Valid rotations are "clockwise" and "counterclockwise". */
    public void addRotation(final String rotation)
    {
        if (rotation.equals("clockwise"))
        {
            this.rotation = ((this.rotation + 90) % 360);
            return;
        }

        if (rotation.equals("counterclockwise"))
        {
            this.rotation = ((this.rotation - 90) % 360);
            return;
        }

        l.fatal("Invalid rotation: " + rotation);
        GameInstance.kill(GameInstance.EXIT_FATAL);

        return;
    }

    public RCoordinate getLocation()
    {
        if (this.location == null)
        {
            l.warn("Agent {} has no location. If this was during the first phase. This can be ignored. Assuming location is the start location.", this.getPlayerID());

            if (this.startPos == null)
            {
                l.fatal("Agent {} has no location nor a start location.", this.getPlayerID());
                GameInstance.kill(GameInstance.EXIT_FATAL);
                return null;
            }

            this.location = this.startPos;
        }

        return this.location;
    }

    public void setLocation(final RCoordinate location)
    {
        this.location = location;
        return;
    }

}
