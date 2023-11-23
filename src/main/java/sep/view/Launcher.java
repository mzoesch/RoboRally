package sep.view;

import sep.view.clientcontroller.GameInstance;

public class Launcher
{
    private Launcher() throws RuntimeException
    {
        super();
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
            System.out.printf("[CLIENT] Wrapping main method.%n");
            sep.view.clientcontroller.EClientInformation.INSTANCE.setWrap(true);
        }

        GameInstance.run();

        System.out.printf("[CLIENT] The application took %.2f seconds to run.%n", (System.currentTimeMillis() - t0) / 1000);

        if (bWrap)
        {
            return;
        }

        System.exit(sep.EArgs.OK);

        return;
    }

}
