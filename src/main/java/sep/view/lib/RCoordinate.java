package sep.view.lib;

import sep.view.viewcontroller.     Tile;
import sep.view.clientcontroller.   EGameState;
import sep.view.clientcontroller.   RemotePlayer;

public record RCoordinate(int x, int y)
{
    public RCoordinate(final int x, final int y)
    {
        this.x = x;
        this.y = y;

        return;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof final RCoordinate c))
        {
            return false;
        }

        return (this.x == c.x) && (this.y == c.y);
    }

    @Override
    public String toString()
    {
        return String.format("(%d, %d)", this.x, this.y);
    }

    public static boolean isOccupied(final RCoordinate c)
    {
        for (final RemotePlayer rp : EGameState.INSTANCE.getRemotePlayers())
        {
            if (rp.getFigureLocation().equals(c))
            {
                return true;
            }
        }

        return false;
    }

    public RCoordinate getNeighbour(final ERotation rot)
    {
        switch (rot)
        {
        case NORTH:
            return new RCoordinate(this.x, this.y - 1);
        case EAST:
            return new RCoordinate(this.x + 1, this.y);
        case SOUTH:
            return new RCoordinate(this.x, this.y + 1);
        case WEST:
            return new RCoordinate(this.x - 1, this.y);
        default:
            return null;
        }
    }

    public static boolean exists(final RCoordinate c, final Tile[][] tiles)
    {
        if (tiles == null)
        {
            return false;
        }

        return (c.x >= 0) && (c.x < tiles.length) && (c.y >= 0) && (c.y < tiles[0].length);
    }

}
