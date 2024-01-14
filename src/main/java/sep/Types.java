package sep;

import java.util.                   Objects;
import java.util.                   Properties;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     IOException;

/** Common types and constants used throughout the application. */
public final class Types
{
    private static final Logger l = LogManager.getLogger(Types.class);

    private Types() throws RuntimeException
    {
        l.error("Types class should not be instantiated.");
        throw new RuntimeException("Types class should not be instantiated.");
    }

    public enum EProps
    {
        GROUP_ID        (   "groupId"       ),
        ARTIFACT_ID     (   "artifactId"    ),
        VERSION         (   "version"       ),
        NAME            (   "name"          ),
        DESCRIPTION     (   "description"   ),
        ;

        private final String prop;

        private EProps(final String prop) throws RuntimeException
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

    public enum EOS
    {
        WINDOWS,
        OSX,
        OTHER,
        ;

        public static EOS getOS()
        {
            final String os = System.getProperty("os.name").toLowerCase();

            if (os.contains("windows"))
            {
                return EOS.WINDOWS;
            }

            if (os.contains("mac"))
            {
                return EOS.OSX;
            }

            return EOS.OTHER;
        }

        public static boolean isWindows()
        {
            return EOS.getOS() == EOS.WINDOWS;
        }

        public static boolean isOSX()
        {
            return EOS.getOS() == EOS.OSX;
        }

        public static boolean isOther()
        {
            return EOS.getOS() == EOS.OTHER;
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

    public enum EConfigurations
    {
        DEV,
        PROD,
        ;

        /** TODO Kinda sketchy but works for now. This is only a temporary sln. And should be removed later on.  */
        public static EConfigurations getConfiguration()
        {
            /* Depending on if we are in a JAR or compiled .class files, we get a dir or file as a return path. */
            final String fp = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();

            if (fp.lastIndexOf("/") == -1)
            {
                return null;
            }

            return fp.lastIndexOf("/") == fp.length() - 1 ? EConfigurations.DEV : EConfigurations.PROD;
        }

        public static boolean isDev()
        {
            return EConfigurations.getConfiguration() == EConfigurations.DEV;
        }

        public static boolean isProd()
        {
            return EConfigurations.getConfiguration() == EConfigurations.PROD;
        }

    }

    /** Delays that affect client and server to keep them in sync with each other. Delays must only be used for animations. */
    public enum EDelay
    {
        REGISTER_PHASE_ITERATION    (   5_000   ),
        PHASE_CHANGE                (   2_000   ),
        CARD_PLAY                   (   1_000   ),
        ;

        public final int i;

        private EDelay(final int i)
        {
            this.i = i;
            return;
        }

    }

}
