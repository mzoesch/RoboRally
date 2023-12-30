package sep;

public final class Types
{
    public enum OS
    {
        WINDOWS,
        OSX,
        OTHER,
        ;

        public static OS getOS()
        {
            final String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("windows"))
            {
                return OS.WINDOWS;
            }

            if (os.contains("mac"))
            {
                return OS.OSX;
            }

            return OS.OTHER;
        }

        public static boolean isWindows()
        {
            return OS.getOS() == OS.WINDOWS;
        }

        public static boolean isOSX()
        {
            return OS.getOS() == OS.OSX;
        }

        public static boolean isOther()
        {
            return OS.getOS() == OS.OTHER;
        }
    }

    public enum Animation
    {
        BLUE_CONVEYOR_BELT  (   "BlueConveyorBelt"     ),
        GREEN_CONVEYOR_BELT (   "GreenConveyorBelt"    ),
        PUSH_PANEL          (   "PushPanel"            ),
        GEAR                (   "Gear"                 ),
        CHECK_POINT         (   "CheckPoint"           ), /* TODO What anim should we play here?? */
        PLAYER_SHOOTING     (   "PlayerShooting"       ),
        WALL_SHOOTING       (   "WallShooting"         ),
        ENERGY_SPACE        (   "EnergySpace"          ),
        ;

        private final String s;

        private Animation(final String s)
        {
            this.s = s;
            return;
        }

        @Override
        public String toString()
        {
            return this.s;
        }

    }

}
