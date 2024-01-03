package sep;

import java.util.                   Objects;
import java.util.                   Properties;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     IOException;

public final class Types
{
    private static final Logger l = LogManager.getLogger(Types.class);

    public enum EProps
    {
        GROUP_ID        (   "groupId"       ),
        ARTIFACT_ID     (   "artifactId"    ),
        VERSION         (   "version"       ),
        NAME            (   "name"          ),
        ;

        private final String prop;

        private EProps(final String prop)
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

        /** Kinda sketchy but works for now. This is only a temporary sln. And should be removed later on.  */
        public static EConfigurations getConfiguration()
        {
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

}
