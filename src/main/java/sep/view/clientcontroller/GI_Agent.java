package sep.view.clientcontroller;

import org.json.                    JSONException;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     IOException;

public final class GI_Agent extends GameInstance
{
    private static final Logger l = LogManager.getLogger(GI_Agent.class);

    private static final int    MAX_CONNECTION_TRIES   = 3;
    private static final int    RETRY_TIMEOUT          = 5_000;

    public GI_Agent()
    {
        super();
        l.info("Creating Game Instance for main thread (agent). Connecting to server.");
        return;
    }

    private static void runAgentLoop()
    {
        int tries = 0;

        while (tries < GI_Agent.MAX_CONNECTION_TRIES)
        {
            try
            {
                if (GameInstance.connectToServer())
                {
                    l.info("Agent successfully connected to server.");
                    break;
                }

                l.error("Some unknown error occurred while connecting to server.");

                if (tries > GI_Agent.MAX_CONNECTION_TRIES - 2)
                {
                    break;
                }

                l.info("Retrying in five seconds. Failed {} times. Quitting after {} tries.", tries + 1, GI_Agent.MAX_CONNECTION_TRIES);

                try
                {
                    Thread.sleep(GI_Agent.RETRY_TIMEOUT);
                }
                catch (final InterruptedException e)
                {
                    l.fatal("Interrupted while waiting to retry.");
                    l.fatal(e.getMessage());
                    return;
                }

                tries++;

                continue;
            }
            catch (final IOException e)
            {
                l.error("Interrupted while waiting to retry.");
                l.error(e.getMessage());

                l.info("Retrying in five seconds. Failed {} times. Quitting after {} tries.", tries + 1, GI_Agent.MAX_CONNECTION_TRIES);

                try
                {
                    Thread.sleep(GI_Agent.RETRY_TIMEOUT);
                }
                catch (final InterruptedException ie)
                {
                    l.fatal("Interrupted while waiting to connect to server.");
                    l.fatal(ie.getMessage());
                    return;
                }

                tries++;

                continue;
            }
        }

        if (!EClientInformation.INSTANCE.hasServerConnection())
        {
            l.fatal("Failed to connect to server.");
            l.fatal("Requesting shutdown.");
            GameInstance.kill();
            return;
        }

        try
        {
            if (!GameInstance.connectToSessionPostLogin(null))
            {
                l.fatal("Failed to connect to session.");
                l.fatal("Requesting shutdown.");
                GameInstance.kill();
                return;
            }
        }
        catch (final IOException | JSONException e)
        {
            l.fatal("Failed to connect to session.");
            l.fatal("Requesting shutdown.");
            GameInstance.kill();
            return;
        }

        return;
    }

    @Override
    public void run()
    {
        GI_Agent.runAgentLoop();
        return;
    }

}
