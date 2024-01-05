package sep.view.lib;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Represents a rotation of an actor in degrees. A rotation of zero degrees means that the actor is facing north.
 */
public record RRotation(int rotation)
{
    private static final Logger l = LogManager.getLogger(RRotation.class);

    public static final int NORTH = 0;

    public RRotation(final int rotation)
    {
        this.rotation = rotation;
        return;
    }

    /**
     * Valid rotation inputs are "clockwise", "counterclockwise", "NORTH", "EAST", "SOUTH", "WEST".
     *
     * @param r The rotation to add to this rotation.
     * @return  The sum of this rotation and the given rotation.
     */
    public RRotation addRotation(final String r)
    {
        if (r.equals("clockwise"))
        {
            return new RRotation(this.rotation + 90);
        }

        if (r.equals("counterclockwise"))
        {
            return new RRotation(this.rotation + -90);
        }

        /* Legacy */
        {

            if (r.equals("NORTH"))
            {
                return new RRotation(0);
            }

            if (r.equals("EAST"))
            {
                return new RRotation(90);
            }

            if (r.equals("SOUTH"))
            {
                return new RRotation(180);
            }

            if (r.equals("WEST"))
            {
                return new RRotation(270);
            }

        }

        l.error("Invalid rotation input: {}", r);
        return null;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof final RRotation r))
        {
            return false;
        }

        return (this.rotation == r.rotation);
    }

    @Override
    public String toString()
    {
        return String.format("%d", this.rotation);
    }

    private static int normalize(final int inR)
    {
        final int r = (inR % 360 + 360) % 360;

        if (r >= 315 || r < 45)
        {
            return 0;
        }

        if (r < 135)
        {
            return 90;
        }

        if (r < 225)
        {
            return 180;
        }

        return 270;
    }

    public ERotation toEnum()
    {
        final int r = RRotation.normalize(this.rotation);

        if (r == 0)
        {
            return ERotation.NORTH;
        }

        if (r == 90)
        {
            return ERotation.EAST;
        }

        if (r == 180)
        {
            return ERotation.SOUTH;
        }

        if (r == 270)
        {
            return ERotation.WEST;
        }

        l.error("Invalid rotation: {}, normalized: {}.", this.rotation, r);
        return null;
    }

}
