package sep.view.lib;

import java.util.Objects;

public enum ERotation
{
    INVALID (   "Invalid"   ),
    NORTH   (   "top"       ),
    EAST    (   "right"     ),
    SOUTH   (   "bottom"    ),
    WEST    (   "left"      ),
    ;

    private final String s;

    private ERotation(final String s)
    {
        this.s = s;
        return;
    }

    public static ERotation fromString(final String s)
    {
        for (final ERotation e : ERotation.values())
        {
            if (Objects.equals(e.s, s))
            {
                return e;
            }

            continue;
        }

        return null;
    }

    @Override
    public String toString()
    {
        return this.s;
    }

    public ERotation getOpposite()
    {
        return switch (this)
        {
            case NORTH  -> ERotation.SOUTH;
            case EAST   -> ERotation.WEST;
            case SOUTH  -> ERotation.NORTH;
            case WEST   -> ERotation.EAST;
            default     -> null;
        };
    }

}
