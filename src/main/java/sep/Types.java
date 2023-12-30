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
        BlueConveyorBelt    (   "BlueConveyorBelt"     ),
        GreenConveyorBelt   (   "GreenConveyorBelt"    ),
        PushPanel           (   "PushPanel"            ),
        Gear                (   "Gear"                 ),
        CheckPoint          (   "CheckPoint"           ), /* TODO What anim should we play here?? */
        PlayerShooting      (   "PlayerShooting"       ),
        WallShooting        (   "WallShooting"         ),
        EnergySpace         (   "EnergySpace"          ),
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
