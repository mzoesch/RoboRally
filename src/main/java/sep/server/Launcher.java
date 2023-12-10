package sep.server;

import sep.EArgs;
import sep.EPort;
import sep.server.model.EServerInformation;
import sep.server.viewmodel.ServerInstance;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Arrays;

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
     * @param args Invalid arguments will be ignored. Valid program arguments are:
     *              <ul>
     *              <li>[--port PORT] - The port number to listen on. Default is
     *                                  {@link sep.EPort#DEFAULT EPort.DEFAULT}.
     *              </ul>
     */
    public static void main(String[] args)
    {
        final double t0 = System.currentTimeMillis();

        if (args.length > 0)
        {
            if (Arrays.asList(args).contains("--port"))
            {
                final int i = Arrays.asList(args).indexOf("--port");
                if (i + 1 < args.length)
                {
                    try
                    {
                        int p = Integer.parseInt(args[i + 1]);
                        l.info("Detected custom port: {}.", p);
                        EServerInformation.INSTANCE.setPort(p);
                    }
                    catch (NumberFormatException e)
                    {
                        l.fatal("Invalid port number.");
                        l.fatal(e.getMessage());
                        l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                        System.exit(EArgs.ERR);
                    }
                }
                else
                {
                    l.fatal("Invalid port number.");
                    l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(EArgs.ERR);
                }
            }
        }

        if (EServerInformation.INSTANCE.getPort() == EPort.INVALID.i)
        {
            l.info("No port change request detected. Using default port: {}.", EPort.DEFAULT.i);
            EServerInformation.INSTANCE.setPort(EPort.DEFAULT.i);
        }

        try
        {
            ServerInstance.run();
        }
        catch (IOException e)
        {
            l.fatal("Server failed.");
            l.fatal(e.getMessage());
            System.exit(EArgs.ERR);
        }
        finally
        {
            l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(sep.EArgs.OK);
        }

        return;
    }

}
