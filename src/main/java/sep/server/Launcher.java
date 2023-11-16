package sep.server;

import sep.server.viewmodel.ServerInstance;

import java.io.IOException;

public final class Launcher
{
    private Launcher() throws RuntimeException
    {
        super();
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
            System.err.printf("[SERVER] Server failed.%n");
            System.err.printf("[SERVER] %s%n", e.getMessage());
        }
        finally
        {
            System.out.printf("The application took %.2f seconds to run.%n", (System.currentTimeMillis() - t0) / 1000);
            System.exit(0);
        }

        return;
    }

}
