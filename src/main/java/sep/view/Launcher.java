package sep.view;

import sep.view.clientcontroller.   GameInstance;
import sep.view.clientcontroller.   EClientInformation;

import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.util.                   Arrays;

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
     * This is the entry point of the client part of the application.
     * The Game Instance is created here.
     *
     * @param args Valid program arguments in descending order of precedence. Invalid arguments will be ignored:
     *             <ul>
     *              <li>[--dev]         - Start mock game view.
     *              <li>[--isAgent]     - Start agent view.
     *              <li>[--addr ADDR]   - The address to auto connect to (if isAgent flag is set). Default is
     *                                    {@link sep.EArgs#PREF_SERVER_IP EArgs.PREF_SERVER_IP}.
     *              <li>[--port PORT]   - The port number to auto connect to (if isAgent flag is set). Default is
     *                                    {@link sep.EArgs#PREF_SERVER_PORT EArgs.PREF_SERVER_PORT}.
     *              <li>[--sid SID]     - The session ID to auto connect to (if isAgent flag is set). Default is
     *                                    {@link sep.Types.EProps#DESCRIPTION EProps.DESCRIPTION}.
     *              <li>[--name NAME]   - The name of the agent (if isAgent flag is set).
     *              <li>[--help]        - Print help message.
     *             </ul>
     */
    public static void main(String[] args)
    {
        final double t0 = System.currentTimeMillis();

        if (args.length > 0)
        {

            if (Arrays.asList(args).contains("--dev"))
            {
                l.info("Command line argument [--dev] detected.");

                l.info("Starting mock game view.");
                sep.view.viewcontroller.MockViewLauncher.run();
                l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(sep.EArgs.OK);
                return;
            }

            if (Arrays.asList(args).contains("--isAgent"))
            {
                l.info("Command line argument [--isAgent] detected. Starting in Agent View Mode.");
                EClientInformation.INSTANCE.setIsAgent(true);
            }

            if (Arrays.asList(args).contains("--addr"))
            {
                l.info("Command line argument [--addr] detected.");

                if (Arrays.asList(args).indexOf("--addr") + 1 < args.length)
                {
                    l.info("Setting server address to {}.", args[Arrays.asList(args).indexOf("--addr") + 1]);
                    EClientInformation.INSTANCE.setServerIP(args[Arrays.asList(args).indexOf("--addr") + 1]);
                }
                else
                {
                    l.fatal("Invalid address.");
                    l.info("Type --help for more information.");
                    l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(sep.EArgs.ERR);
                    return;
                }
            }

            if (Arrays.asList(args).contains("--port"))
            {
                l.info("Command line argument [--port] detected.");

                if (Arrays.asList(args).indexOf("--port") + 1 < args.length)
                {
                    final int p;
                    try
                    {
                        p = Integer.parseInt(args[Arrays.asList(args).indexOf("--port") + 1]);
                    }
                    catch (final NumberFormatException e)
                    {
                        l.fatal("Invalid port number.");
                        l.fatal(e.getMessage());
                        l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                        System.exit(sep.EArgs.ERR);
                        return;
                    }

                    if (p < sep.Types.EPort.MIN.i || p > sep.Types.EPort.MAX.i)
                    {
                        l.fatal("Invalid port number. Must be between {} and {}.", sep.Types.EPort.MIN.i, sep.Types.EPort.MAX.i);
                        l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                        System.exit(sep.EArgs.ERR);
                        return;
                    }

                    l.info("Setting server port target to {}.", p);
                    EClientInformation.INSTANCE.setServerPort(p);
                }
                else
                {
                    l.fatal("Invalid port number.");
                    l.info("Type --help for more information.");
                    l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(sep.EArgs.ERR);
                    return;
                }
            }

            if (Arrays.asList(args).contains("--sid"))
            {
                l.info("Command line argument [--sid] detected.");

                if (Arrays.asList(args).indexOf("--sid") + 1 < args.length)
                {
                    l.info("Setting session ID to {}.", args[Arrays.asList(args).indexOf("--sid") + 1]);
                    EClientInformation.INSTANCE.setPreferredSessionID(args[Arrays.asList(args).indexOf("--sid") + 1]);
                }
                else
                {
                    l.fatal("Invalid session ID.");
                    l.info("Type --help for more information.");
                    l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(sep.EArgs.ERR);
                    return;
                }
            }

            if (Arrays.asList(args).contains("--name"))
            {
                l.info("Command line argument [--name] detected.");

                if (Arrays.asList(args).indexOf("--name") + 1 < args.length)
                {
                    l.info("Setting agent name to {}.", args[Arrays.asList(args).indexOf("--name") + 1]);
                    EClientInformation.INSTANCE.setPrefAgentName(args[Arrays.asList(args).indexOf("--name") + 1]);
                }
                else
                {
                    l.fatal("Invalid agent name.");
                    l.info("Type --help for more information.");
                    l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(sep.EArgs.ERR);
                    return;
                }
            }

            if (Arrays.asList(args).contains("--help"))
            {
                l.info("##################### CLIENT HELP #####################");
                l.info("Valid view program arguments in descending order of precedence. Invalid arguments will be ignored.");
                l.info("Usage: java -cp {jar-name}.jar sep.view.Launcher [--dev] [--help]");
                l.info("Valid server program arguments in descending order of precedence.");
                l.info("  --dev             Start mock game view.");
                l.info("  --isAgent         Start agent view.");
                l.info("  --addr <ADDR>     The address to auto connect to (if isAgent flag is set). Default is {}.", sep.EArgs.PREF_SERVER_IP);
                l.info("  --port <PORT>     The port number to auto connect to (if isAgent flag is set). Default is {}.", sep.EArgs.PREF_SERVER_PORT);
                l.info("  --sid <SID>       The session ID to auto connect to (if isAgent flag is set). Default is {}.", sep.Types.EProps.DESCRIPTION);
                l.info("  --name <NAME>     The name of the agent (if isAgent flag is set).");
                l.info("  --help            Print view help message.");
                l.info("#######################################################");
                System.exit(sep.EArgs.OK);
                return;
            }

        }

        try
        {
            GameInstance.run();
        }
        catch (final Exception e)
        {
            l.fatal("An unexpected error occurred.");
            l.fatal(e.getMessage());
            l.fatal(e.getStackTrace());
        }
        finally
        {
            l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(sep.EArgs.OK);
        }

        return;
    }

}
