package sep.server;

import sep.server.viewmodel.ServerInstance;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Launcher
{
    private static final Logger l = LogManager.getLogger(Launcher.class);

    private Launcher() throws RuntimeException
    {
        super();
        l.error("This class cannot be instantiated.");
        throw new RuntimeException("This class cannot be instantiated.");
    }

    /**
     * This is the entry point of the server part of the application.
     * The Server Instance is created here.
     *
     * @param args CMD arguments are ignored.
     */
    public static void main(String[] args)
    {
        double t0 = System.currentTimeMillis();

        try
        {
            ServerInstance.run();
        }
        catch (IOException e)
        {
            l.fatal("Server failed.");
            l.fatal(e.getMessage());
        }
        finally
        {
            l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(sep.EArgs.OK);
        }

        return;
    }

}
