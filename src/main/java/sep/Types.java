package sep;

public class Types
{
    public enum OS
    {
        WINDOWS,
        OSX,
        OTHER;

        public static OS getOS()
        {
            String os = System.getProperty("os.name").toLowerCase();
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

}
