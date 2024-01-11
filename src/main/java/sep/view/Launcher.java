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
     *              <li>[--dev]     - Start mock game view.
     *              <li>[--isAgent] - Start agent view.
     *              <li>[--help]    - Print help message.
     *             </ul>
     */
    public static void main(String[] args)
    {
        final double t0 = System.currentTimeMillis();

        if (args.length > 0)
        {

            if (Arrays.asList(args).contains("--dev"))
            {
                l.info("Starting mock game view.");
                sep.view.viewcontroller.MockViewLauncher.run();
                l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(sep.EArgs.OK);
                return;
            }

            if (Arrays.asList(args).contains("--isAgent"))
            {
                l.info("Detected agent view. Starting in agent view mode.");
                EClientInformation.INSTANCE.setIsAgent(true);
            }

            if (Arrays.asList(args).contains("--help"))
            {
                l.info("##################### CLIENT HELP #####################");
                l.info("Valid view program arguments in descending order of precedence. Invalid arguments will be ignored.");
                l.info("Usage: java -cp {jar-name}.jar sep.view.Launcher [--dev] [--help]");
                l.info("Valid server program arguments in descending order of precedence.");
                l.info("  --dev         Start mock game view.");
                l.info("  --isAgent     Start agent view.");
                l.info("  --help        Print view help message.");
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
