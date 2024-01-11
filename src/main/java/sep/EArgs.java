package sep;

import sep.Types.   EPort;

import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.util.                   ArrayList;

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
        AGENT,
        NUM
    }

    public static final String  PREF_SERVER_IP              = "localhost";
    public static final EPort   PREF_SERVER_PORT            = EPort.DEFAULT;
    private String              customServerIP;
    private int                 customServerPort;
    public static final int     DEFAULT_MIN_REMOTE_PLAYERS  = -1;
    private int                 customMinRemotePlayers;
    private int                 customMinHumanPlayers;
    public static final int     DEFAULT_MIN_HUMAN_PLAYERS   = -1;
    private EMode               mode;
    public static final int     MAX_AGENT_COUNT             = 6;
    private ArrayList<String>   agentNames;
    private String              customSessionID;
    private boolean             bAllowServerStart;
    private boolean             bAllowClientStart;

    private EArgs()
    {
        this.customServerIP             = "";
        this.customServerPort           = EPort.INVALID.i;
        this.customMinRemotePlayers     = EArgs.DEFAULT_MIN_REMOTE_PLAYERS;
        this.customMinHumanPlayers      = EArgs.DEFAULT_MIN_HUMAN_PLAYERS;
        this.mode                       = EMode.DEFAULT;
        this.agentNames                 = new ArrayList<String>();
        this.customSessionID            = "";
        this.bAllowServerStart          = true;
        this.bAllowClientStart          = true;

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

    public static String getCustomServerIP()
    {
        return EArgs.INSTANCE.customServerIP;
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

    public static int getCustomMinHumanPlayers()
    {
        return EArgs.INSTANCE.customMinHumanPlayers;
    }

    public static void setCustomMinHumanPlayers(final int min)
    {
        EArgs.INSTANCE.customMinHumanPlayers = min;
        return;
    }

    public static ArrayList<String> getAgentNames()
    {
        return EArgs.INSTANCE.agentNames;
    }

    public static String getCustomSessionID()
    {
        return EArgs.INSTANCE.customSessionID;
    }

    public static void setCustomSessionID(final String customSessionID)
    {
        EArgs.INSTANCE.customSessionID = customSessionID;
        return;
    }

    public static boolean getAllowServerStart()
    {
        return EArgs.INSTANCE.bAllowServerStart;
    }

    public static void setAllowServerStart(final boolean bAllowServerStart)
    {
        EArgs.INSTANCE.bAllowServerStart = bAllowServerStart;
        return;
    }

    public static boolean getAllowClientStart()
    {
        return EArgs.INSTANCE.bAllowClientStart;
    }

    public static void setAllowClientStart(final boolean bAllowClientStart)
    {
        EArgs.INSTANCE.bAllowClientStart = bAllowClientStart;
        return;
    }

    // endregion Getters and Setters

}
