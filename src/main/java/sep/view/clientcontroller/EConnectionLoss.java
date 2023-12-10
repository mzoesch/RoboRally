package sep.view.clientcontroller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public enum EConnectionLoss
{
    REMOVE(0),
    AI_CONTROL(1),
    WAIT_FOR_RECONNECT(2),
    RECONNECT(3)
    ;

    private static final Logger l = LogManager.getLogger(EConnectionLoss.class);

    private final int i;

    private EConnectionLoss(final int i)
    {
        this.i = i;
    }

    @Override
    public String toString()
    {
        return switch (i)
        {
            case 3 -> "Reconnect";
            case 2 -> "Ignore";
            case 1 -> "AIControl";
            case 0 -> "Remove";
            default ->
            {
                l.error("Invalid EConnectionLoss value: {}. ", i);
                yield null;
            }
        };
    }

    public static EConnectionLoss fromString(final String s)
    {
        return switch (s)
        {
            case "Reconnect" -> RECONNECT;
            case "Ignore" -> WAIT_FOR_RECONNECT;
            case "AIControl" -> AI_CONTROL;
            case "Remove" -> REMOVE;
            default ->
            {
                l.error("Invalid EConnectionLoss value: {}. ", s);
                yield null;
            }
        };
    }

}
