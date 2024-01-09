package sep.server;

import sep.EArgs;
import sep.server.model.EServerInformation;
import sep.server.viewmodel.ServerInstance;
import sep.server.model.game.GameState;

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

    /* TODO Help arg. */
    /**
     * This is the entry point of the server part of the application.
     * The Server Instance is created here.
     *
     * @param args Invalid arguments will be ignored. Valid program arguments are:
     *              <ul>
     *              <li>[--port PORT]        - The port number to listen on. Default is
     *                                         {@link sep.Types.EPort#DEFAULT EPort.DEFAULT}.
     *              <li>[--minRemotePlayers] - The minimum number of remote players required to start a game. Default is
     *                                         {@link sep.server.model.game.GameState#DEFAULT_MIN_REMOTE_PLAYER_COUNT_TO_START
     *                                         MIN_REMOTE_PLAYERS}.
     *              </ul>
     */
    public static void main(final String[] args)
    {
        final double t0 = System.currentTimeMillis();

        if (args.length > 0)
        {

            if (Arrays.asList(args).contains("--port"))
            {
                final int i = Arrays.asList(args).indexOf("--port");
                if (i + 1 < args.length)
                {
                    final int p;
                    try
                    {
                        p = Integer.parseInt(args[i + 1]);
                    }
                    catch (final NumberFormatException e)
                    {
                        l.fatal("Invalid port number.");
                        l.fatal(e.getMessage());
                        l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                        System.exit(EArgs.ERR);
                        return;
                    }

                    l.info("Detected custom port: {}.", p);
                    EServerInformation.INSTANCE.setPort(p);
                }
                else
                {
                    l.fatal("Invalid port number.");
                    l.info("Type --help for more information.");
                    l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(EArgs.ERR);
                    return;
                }
            }

            if (Arrays.asList(args).contains("--minRemotePlayers"))
            {
                final int i = Arrays.asList(args).indexOf("--minRemotePlayers");
                if (i + 1 < args.length)
                {
                    final int min;
                    try
                    {
                        min = Integer.parseInt(args[i + 1]);
                    }
                    catch (final NumberFormatException e)
                    {
                        l.fatal("Invalid min remote player count.");
                        l.fatal(e.getMessage());
                        l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                        System.exit(EArgs.ERR);
                        return;
                    }

                    if (min < 0 || min > GameState.MAX_CONTROLLERS_ALLOWED)
                    {
                        l.fatal("Invalid minimum number of remote players.");
                        l.info("Type --help for more information.");
                        l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                        System.exit(EArgs.ERR);
                        return;
                    }

                    l.info("Detected request to change minimum required remote players: {}.", min);
                    EServerInformation.INSTANCE.setMinRemotePlayerCountToStart(min);
                }
                else
                {
                    l.fatal("Invalid minimum number of remote players.");
                    l.info("Type --help for more information.");
                    l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(EArgs.ERR);
                    return;
                }
            }

            if (Arrays.asList(args).contains("--help"))
            {
                l.info("##################### SERVER HELP #####################");
                l.info("Valid server program arguments in descending order of precedence. Invalid arguments will be ignored.");
                l.info("Usage: java -cp {jar-name}.jar sep.server.Launcher [--port PORT] [--minRemotePlayers MIN_REMOTE_PLAYERS] [--help]");
                l.info("  --port <PORT>                             The port number to listen on. Default is {}.", sep.Types.EPort.DEFAULT.i);
                l.info("  --minRemotePlayers <MIN_REMOTE_PLAYERS>   The minimum number of remote clients required to start a game. Default is {}.", sep.server.model.game.GameState.DEFAULT_MIN_REMOTE_PLAYER_COUNT_TO_START);
                l.info("  --help                                    Print this help message.");
                l.info("#######################################################");
                l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(EArgs.OK);
                return;
            }

        }

        if (EServerInformation.INSTANCE.getPort() == sep.Types.EPort.INVALID.i)
        {
            l.info("No port change request detected. Using default port: {}.", sep.Types.EPort.DEFAULT.i);
            EServerInformation.INSTANCE.setPort(sep.Types.EPort.DEFAULT.i);
        }

        try
        {
            ServerInstance.run();
        }
        catch (final IOException e)
        {
            l.fatal("Server failed.");
            l.fatal(e.getMessage());
            System.exit(EArgs.ERR);
            return;
        }
        finally
        {
            l.debug("Server shutting down. The server took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(sep.EArgs.OK);
        }

        return;
    }

}
