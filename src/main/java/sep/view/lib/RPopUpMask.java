package sep.view.lib;

public final record RPopUpMask(EPopUp type, String header, String msg)
{
    public RPopUpMask
    {
    }

    public RPopUpMask(final EPopUp type)
    {
        this(type, type.toString(), null);
        return;
    }

    public RPopUpMask(final EPopUp type, final String msg)
    {
        this(type, type.toString() , msg);
        return;
    }

}
