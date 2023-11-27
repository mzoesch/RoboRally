package sep;

public enum EPort
{
    INVALID(-1),
    DEFAULT(8080),
    MIN(1024),
    MAX(65535);

    public final int i;

    EPort(int i)
    {
        this.i = i;
    }

}
