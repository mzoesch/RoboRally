package sep.view;

import sep.view.clientcontroller.GameInstance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Arrays;

public class Launcher
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
     * @param args [--dev] - Starts mock game view.
     */
    public static void main(String[] args)
    {
        double t0 = System.currentTimeMillis();

        if (args.length > 0 && Arrays.asList(args).contains("--dev"))
        {
            l.info("Starting mock game view.");
            sep.view.viewcontroller.MockViewLauncher.run();
            l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(sep.EArgs.OK);
            return;
        }

        GameInstance.run();

        l.debug("The client application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);

        System.exit(sep.EArgs.OK);

        return;
    }

}
