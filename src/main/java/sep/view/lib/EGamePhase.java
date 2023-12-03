package sep.view.lib;

public enum EGamePhase
{
    INVALID(-1),
    REGISTRATION(0),
    UPGRADE(1),
    PROGRAMMING(2),
    ACTIVATION(3);

    public final int i;

    private EGamePhase(int i)
    {
        this.i = i;
        return;
    }

    public static EGamePhase fromInt(int i)
    {
        for (EGamePhase e : EGamePhase.values())
        {
            if (e.i == i)
            {
                return e;
            }
        }

        return EGamePhase.INVALID;
    }

}
