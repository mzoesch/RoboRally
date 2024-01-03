package sep;

import java.util.                   Objects;
import java.util.                   Properties;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     IOException;

public final class Types
{
    private static final Logger l = LogManager.getLogger(Types.class);

    public enum Props
    {
        GROUP_ID        (   "groupId"       ),
        ARTIFACT_ID     (   "artifactId"    ),
        VERSION         (   "version"       ),
        NAME            (   "name"          ),
        ;

        private final String prop;

        private Props(final String prop)
        {
            final Properties props = new Properties();
            try
            {
                props.load(Types.class.getClassLoader().getResourceAsStream(".properties"));
            }
            catch (final IOException e)
            {
                l.fatal("Could not load properties file.");
                l.fatal(e.getMessage());
                throw new RuntimeException(e);
            }

            this.prop = props.getProperty(prop);

            if (this.prop == null || this.prop.isEmpty() || this.prop.isBlank() || Objects.equals(this.prop, "null"))
            {
                l.fatal("Could not load property: {}.", prop);
                throw new RuntimeException(String.format("Could not load property: %s.", prop));
            }

            return;
        }

        @Override
        public String toString()
        {
            return this.prop;
        }

    }

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

    public enum EPort
    {
        INVALID (   -1      ),
        MIN     (   1024    ),
        DEFAULT (   8080    ),
        MAX     (   65535   ),
        ;

        public final int i;

        private EPort(final int i)
        {
            this.i = i;
            return;
        }

    }

    public enum Configurations
    {
        DEV,
        PROD,
        ;

        /** Kinda sketchy but works for now. This is only a temporary sln. And should be removed later on.  */
        public static Configurations getConfiguration()
        {
            final String fp = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();

            if (fp.lastIndexOf("/") == -1)
            {
                return null;
            }

            return fp.lastIndexOf("/") == fp.length() - 1 ? Configurations.DEV : Configurations.PROD;
        }

        public static boolean isDev()
        {
            return Configurations.getConfiguration() == Configurations.DEV;
        }

        public static boolean isProd()
        {
            return Configurations.getConfiguration() == Configurations.PROD;
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

        public static Animation fromString(final String s)
        {
            for (final Animation a : Animation.values())
            {
                if (Objects.equals(a.s, s))
                {
                    return a;
                }
            }

            return null;
        }

        @Override
        public String toString()
        {
            return this.s;
        }

    }

}
