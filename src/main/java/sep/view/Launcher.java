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
     * @param args CMD arguments are ignored.
     */
    public static void main(String[] args)
    {
        double t0 = System.currentTimeMillis();

        GameInstance.run();

        System.out.printf("The application took %.2f seconds to run.%n", (System.currentTimeMillis() - t0) / 1000);
        System.exit(0);

        return;
    }

}
