package sep.view.lib;

public enum EModifier
{
    INVALID         (   "Invalid"       ),
    EMPTY           (   "Empty"         ),
    START_POINT     (   "StartPoint"    ),
    CONVEYOR_BELT   (   "ConveyorBelt"  ),
    PUSH_PANEL      (   "PushPanel"     ),
    GEAR            (   "Gear"          ),
    PIT             (   "Pit"           ),
    ENERGY_SPACE    (   "EnergySpace"   ),
    WALL            (   "Wall"          ),
    LASER           (   "Laser"         ),
    ANTENNA         (   "Antenna"       ),
    CHECK_POINT     (   "CheckPoint"    ),
    RESTART_POINT   (   "RestartPoint"  ),
    ;

    private final String s;

    private EModifier(final String s)
    {
        this.s = s;
        return;
    }

    public static EModifier fromString(final String s)
    {
        for (final EModifier e : EModifier.values())
        {
            if (e.s.equals(s))
            {
                return e;
            }

            continue;
        }

        return null;
    }

    @Override
    public String toString()
    {
        return this.s;
    }

}
