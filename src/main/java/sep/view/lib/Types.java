package sep.view.lib;

import sep.view.viewcontroller.Tile;

import java.util.           Objects;
import javafx.scene.image.  ImageView;

public final class Types
{
    private Types() throws IllegalStateException
    {
        throw new IllegalStateException("Types class");
    }

    public enum EFigure
    {
        INVALID (  -1,  "Invalid"       ),
        HAMMER  (   0,  "Hammer Bot"    ),
        TRUNDLE (   1,  "Trundle Bot"   ),
        SQUASH  (   2,  "Squash Bot"    ),
        X90     (   3,  "Hulk x90"      ),
        SPIN    (   4,  "Spin Bot"      ),
        TWONKY  (   5,  "Twonky"        ),
        TWITCH  (   6,  "Twitch"        ),
        NUM     (   7,  "FALSE USE"     )
        ;

        public final int i;
        private final String s;

        private EFigure(final int i, final String name)
        {
            this.i = i;
            this.s = name;

            return;
        }

        public static EFigure fromInt(final int i)
        {
            for (final EFigure e : EFigure.values())
            {
                if (e.i == i)
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

    }

    public enum EPopUp
    {
        ERROR,
    }

    public record RPopUpMask(EPopUp type, String header, String msg)
    {
        public RPopUpMask
        {
        }

        public RPopUpMask(final EPopUp type)
        {
            this(type, type.toString(), null);
        }

        public RPopUpMask(final EPopUp type, final String msg)
        {
            this(type, type.toString() , msg);
        }

    }

    public enum EModifier
    {
        INVALID         (   "Invalid"       ),
        EMPTY           (   "Empty"         ),
        START_POINT     (   "StartPoint"    ),
        CONVEYOR_BELT   (   "ConveyorBelt"  ),
        PUSH_PANEL      (   "PushPanel"     ),
        GEAR            (   "Gear"          ),
        PIT             (   "Pit"           ),
        ENERGY_SPACE    (   "EnergySpace"   ),
        WALL            (   "Wall"          ),
        LASER           (   "Laser"         ),
        ANTENNA         (   "Antenna"       ),
        CHECK_POINT     (   "CheckPoint"    ),
        RESTART_POINT   (   "RestartPoint"  ),
        ;

        private final String s;

        private EModifier(final String s)
        {
            this.s = s;
            return;
        }

        public static EModifier fromString(final String s)
        {
            for (final EModifier e : EModifier.values())
            {
                if (e.s.equals(s))
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

    }

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

    public record RLaserMask(Tile t, ERotation rot, int count)
    {
    }

    public record RGearMask(ImageView iv, boolean clockwise, int rotation)
    {
    }

}
