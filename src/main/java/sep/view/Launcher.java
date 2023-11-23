package sep.view;

import sep.view.clientcontroller.GameInstance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
     * @param args To determine wrapping. Else ignored.
     */
    public static void main(String[] args)
    {
        double t0 = System.currentTimeMillis();

        boolean bWrap = false;
        if (args.length > 0)
        {
            if (args[0].equals("wrap"))
            {
                bWrap = true;
            }
        }

        if (bWrap)
        {
            l.info("Wrapping main method.");
            sep.view.clientcontroller.EClientInformation.INSTANCE.setWrap(true);
        }

        GameInstance.run();

        l.debug("The application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);

        if (bWrap)
        {
            return;
        }

        System.exit(sep.EArgs.OK);

        return;
    }

}
