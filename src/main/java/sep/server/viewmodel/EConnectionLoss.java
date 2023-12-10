package sep.server.viewmodel;

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
            case 0 -> "Remove";
            case 1 -> "AIControl";
            case 2 -> "Ignore";
            case 3 -> "Reconnect";
            default ->
            {
                l.error("Invalid EConnectionLoss value: {}. ", i);
                yield null;
            }
        };
    }

}
