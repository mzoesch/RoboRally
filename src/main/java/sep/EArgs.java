package sep;

import sep.Types.   EPort;

import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;

/** Singleton class for storing universal program arguments. */
public enum EArgs
{
    INSTANCE;

    private static final Logger l = LogManager.getLogger(EArgs.class);

    public final static int     OK          = 0;
    public final static int     ERR         = 1;

    public enum EMode
    {
        DEFAULT,
        CLIENT,
        SERVER,
        EXIT,
        HELP,
        NUM
    }

    public static final String  PREF_SERVER_IP              = "localhost";
    public static final EPort   PREF_SERVER_PORT            = EPort.DEFAULT;
    private String              customServerIP;
    private int                 customServerPort;
    public static final int     DEFAULT_MIN_REMOTE_PLAYERS  = -1;
    private int                 customMinRemotePlayers;
    private EMode               mode;

    private EArgs()
    {
        this.customServerIP             = "";
        this.customServerPort           = EPort.INVALID.i;
        this.customMinRemotePlayers     = EArgs.DEFAULT_MIN_REMOTE_PLAYERS;
        this.mode                       = EMode.DEFAULT;

        return;
    }

    // region Getters and Setters

    public static EMode getMode()
    {
        return EArgs.INSTANCE.mode;
    }

    public static void setMode(final EMode mode) throws IllegalArgumentException
    {
        if (mode.ordinal() > EMode.NUM.ordinal())
        {
            l.fatal("Invalid mode ({} {}).", mode.ordinal(), mode.toString());
            throw new IllegalArgumentException(String.format("Invalid mode (%d %s).", mode.ordinal(), mode.toString()));
        }

        EArgs.INSTANCE.mode = mode;

        return;
    }

    public static void setCustomServerIP(final String customServerIP)
    {
        EArgs.INSTANCE.customServerIP = customServerIP;
        return;
    }

    public static int getCustomServerPort()
    {
        return EArgs.INSTANCE.customServerPort;
    }

    public static void setCustomServerPort(final int customServerPort)
    {
        EArgs.INSTANCE.customServerPort = customServerPort;
        return;
    }

    public static int getCustomMinRemotePlayers()
    {
        return EArgs.INSTANCE.customMinRemotePlayers;
    }

    public static void setCustomMinRemotePlayers(final int min)
    {
        EArgs.INSTANCE.customMinRemotePlayers = min;
        return;
    }

    // endregion Getters and Setters

}
