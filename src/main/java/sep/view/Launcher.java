package sep.view;

import sep.view.clientcontroller.GameInstance;

public class Launcher
{
    private Launcher() throws RuntimeException
    {
        super();
        throw new RuntimeException("This class cannot be instantiated.");
    }

    public static void main(String[] args)
    {
        double t0 = System.currentTimeMillis();

        GameInstance.run();

        System.out.printf("The application took %.2f seconds to run.%n", (System.currentTimeMillis() - t0) / 1000);
        System.exit(0);

        return;
    }

}
