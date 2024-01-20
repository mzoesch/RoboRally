package sep.view.lib;

import sep.view.viewcontroller.     Tile;
import sep.view.clientcontroller.   EGameState;
import sep.view.clientcontroller.   RemotePlayer;

public final record RCoordinate(int x, int y)
{
    public RCoordinate(final int x, final int y)
    {
        this.x  = x;
        this.y  = y;

        return;
    }

    public static RCoordinate fromIndex(final int idx, final int files)
    {
        return new RCoordinate(idx % files, idx / files);
    }

    /** @deprecated  */
    public RCoordinate(final int idx, final int files, final int ranks)
    {
        this(idx % files, idx / files);
        return;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj == this)
        {
            return true;
        }

        if (!(obj instanceof final RCoordinate location))
        {
            return false;
        }

        return (this.x == location.x) && (this.y == location.y);
    }

    @Override
    public String toString()
    {
        return String.format("(%d, %d)", this.x, this.y);
    }

    public static boolean isOccupied(final RCoordinate location)
    {
        for (final RemotePlayer rp : EGameState.INSTANCE.getRemotePlayers())
        {
            if (rp.getFigureLocation().equals(location))
            {
                return true;
            }
        }

        return false;
    }

    public RCoordinate getNeighbour(final ERotation rotation)
    {
        return switch (rotation)
        {
            case NORTH  ->  new RCoordinate(    this.x,         this.y - 1  );
            case EAST   ->  new RCoordinate(    this.x + 1,     this.y      );
            case SOUTH  ->  new RCoordinate(    this.x,         this.y + 1  );
            case WEST   ->  new RCoordinate(    this.x - 1,     this.y      );
            default     ->  null;
        };
    }

    public static boolean isOutOfBounds(final RCoordinate location, final Tile[][] tiles)
    {
        if (tiles == null)
        {
            return true;
        }

        return (location.x < 0) || (location.x >= tiles.length) || (location.y < 0) || (location.y >= tiles[0].length);
    }

    @Override
    public int x() {
        return x;
    }

}
