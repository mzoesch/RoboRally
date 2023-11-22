package sep;

public enum EArgs
{
    INSTANCE;

    public final static String WIN_TITLE = "SEP WRAPPER";

    public final static int OK = 0;
    public final static int ERROR = 1;

    public final static int DEFAULT = 0;
    public final static int CLIENT = 1;
    public final static int SERVER = 2;
    public final static int EXIT = 3;

    private int mode;

    private EArgs()
    {
        this.mode = EArgs.DEFAULT;
        return;
    }

    public static int getMode()
    {
        return EArgs.INSTANCE.mode;
    }

    public static void setMode(int mode) throws IllegalArgumentException
    {
        if (mode < EArgs.DEFAULT || mode > EArgs.EXIT)
        {
            throw new IllegalArgumentException("Invalid mode.");
        }

        EArgs.INSTANCE.mode = mode;
        return;
    }

}
