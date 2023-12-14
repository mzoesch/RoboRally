package sep.view.lib;

public enum EShopState {

    DEACTIVATED(-1),
    UPGRADE(0),
    DAMAGE(1),
    REBOOT(2);

    public final int i;

    private EShopState(int i)
    {
        this.i = i;
        return;
    }

    public static EShopState fromInt(int i)
    {
        for (EShopState e : EShopState.values())
        {
            if (e.i == i)
            {
                return e;
            }
        }

        return EShopState.DEACTIVATED;
    }

}

