package sep.view.lib;

import java.util.Objects;

public enum EAnimation
{
    BLUE_CONVEYOR_BELT  (   "BlueConveyorBelt"     ),
    GREEN_CONVEYOR_BELT (   "GreenConveyorBelt"    ),
    PUSH_PANEL          (   "PushPanel"            ),
    GEAR                (   "Gear"                 ),
    CHECK_POINT         (   "CheckPoint"           ), /* TODO What anim should we play here?? */
    PLAYER_SHOOTING     (   "PlayerShooting"       ),
    WALL_SHOOTING       (   "WallShooting"         ),
    ENERGY_SPACE        (   "EnergySpace"          ),
    ;

    private final String s;

    private EAnimation(final String s)
    {
        this.s = s;
        return;
    }

    public static EAnimation fromString(final String s)
    {
        for (final EAnimation a : EAnimation.values())
        {
            if (Objects.equals(a.s, s))
            {
                return a;
            }
        }

        return null;
    }

    @Override
    public String toString()
    {
        return this.s;
    }

}