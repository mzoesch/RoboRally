package sep.view.lib;

public final class OutErr
{
    private String msg;

    public OutErr()
    {
        this.msg = "";
        return;
    }

    public boolean isSet()
    {
        return !this.msg.isEmpty() && !this.msg.isBlank();
    }

    public void set(final String msg)
    {
        this.msg = msg;
        return;
    }

    public String get()
    {
        return this.msg;
    }

}
