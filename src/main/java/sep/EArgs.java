package sep;

public enum EArgs
{
    INSTANCE;

    public final static int OK = 0;
    public final static int ERR = 1;

    public final static int DEFAULT = 0;
    public final static int CLIENT = 1;
    public final static int SERVER = 2;
    public final static int EXIT = 3;

    public static final String PREF_SERVER_IP = "localhost";
    public static final EPort PREF_SERVER_PORT = EPort.DEFAULT;
    private String customServerIP;
    private int customServerPort;
    public static final int DEFAULT_MIN_REMOTE_PLAYERS = -1;
    private int customMinRemotePlayers;

    private int mode;

    private EArgs()
    {
        this.mode = EArgs.DEFAULT;
        this.customServerIP = "";
        this.customServerPort = EPort.INVALID.i;
        this.customMinRemotePlayers = EArgs.DEFAULT_MIN_REMOTE_PLAYERS;
        return;
    }

    public static int getMode()
    {
        return EArgs.INSTANCE.mode;
    }

    public static void setMode(final int mode) throws IllegalArgumentException
    {
        if (mode < EArgs.DEFAULT || mode > EArgs.EXIT)
        {
            throw new IllegalArgumentException("Invalid mode.");
        }

        EArgs.INSTANCE.mode = mode;
        return;
    }

    public static String getCustomServerIP()
    {
        return EArgs.INSTANCE.customServerIP;
    }

    public static void setCustomServerIP(String customServerIP)
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
}
