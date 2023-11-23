package sep;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Launcher
{
    private Launcher() throws RuntimeException
    {
        super();
        throw new RuntimeException("This class cannot be instantiated.");
    }

    private static void runWrapper(String[] args)
    {
        String[] targs = new String[args.length + 1];
        targs[0] = "nocmd";
        System.arraycopy(args, 0, targs, 1, args.length);
        Launcher.main(targs);
        return;
    }

    /**
     * When wrapping.
     *
     * @param args Call with "nocmd" to prevent creating a new process terminal.
     *             All other args will always be passed down.
     *             When developing, call with "nocmd" program argument because we already
     *             have the stdout and stderr pipeline to the integrated terminal.
     */
    public static void main(String[] args)
    {
        // This is highly sketchy. We essentially start the wrapper again but with the stdout & stderr to the new
        // created process. We need this because the server does not have a Graphical User Interface and thus no
        // way to display the std pipeline to the user.
        // We could also just start the server as a service and read the stdout & stderr from the logs.
        if (args.length == 0 || !Arrays.asList(args).contains("nocmd"))
        {
            System.out.printf("[WRAPPER] Trying to cast stdout to new terminal.%n");
            try
            {
                double t0 = System.currentTimeMillis();

                String fp = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                /* This only works with a jar file. Because else we get a dir. */
                String f = fp.substring(fp.lastIndexOf("/") + 1);

                ProcessBuilder pb;
                if (System.getProperty("os.name").toLowerCase().contains("windows"))
                {
                    pb = new ProcessBuilder(System.getenv("COMSPEC"), "/c", "start", "cmd", "/k", String.format("java -jar %s nocmd %s & exit", f, String.join(" ", args)));
                }
                else if (System.getProperty("os.name").toLowerCase().contains("mac"))
                {
                    String p = fp.substring(0, fp.lastIndexOf("/"));
                    String rcmd = String.format("cd %s && java -jar %s nocmd %s & exit", p, f, String.join(" ", args));

                    ArrayList<String> cmd = new ArrayList<String>();
                    cmd.add("osascript");
                    cmd.add("-e");
                    cmd.add(String.format("tell application \"Terminal\" to do script \"%s\"", rcmd));
                    pb = new ProcessBuilder(cmd);
                }
                else
                {
                    // To test:
                    // pb = new ProcessBuilder("xterm", "-e", String.format("java -jar sep-0.1.jar nocmd %s ; exit", String.join(" ", args)));
                    System.err.println("[WRAPPER] Unsupported OS. Starting wrapper normally.");
                    Launcher.runWrapper(args);
                    System.exit(EArgs.OK);
                    return;
                }
                Process p = pb.start();

                int rc = p.waitFor();
                if (rc != EArgs.OK)
                {
                    System.err.printf("[WRAPPER] Something went wrong. When trying to cast stdout to new terminal.%n");
                    System.err.printf("[WRAPPER] Starting wrapper normally.%n");
                    p.destroy();
                    Launcher.runWrapper(args);
                    return;
                }

                /* Also super sketchy. But win will always return a rc of 0 somehow. */
                /* Only uncomment this, when using an IDE, and this method is not called with "nocmd" argument. For the actual JAR-File, this won't work. */
//                if (System.currentTimeMillis() - t0 < 100 && System.getProperty("os.name").toLowerCase().contains("windows"))
//                {
//                    System.err.printf("[WRAPPER] Something went wrong. When trying to cast stdout to new terminal.%n");
//                    System.err.printf("[WRAPPER] Starting wrapper normally.%n");
//                    p.destroy();
//                    Launcher.runWrapper(args);
//                    return;
//                }

                System.out.printf("[WRAPPER] Wrapper took %.2f seconds to run.%n", (System.currentTimeMillis() - t0) / 1000);
            }
            catch (IOException e)
            {
                System.err.printf("[WRAPPER] Failed to start wrapper.%n");
                System.exit(EArgs.ERROR);
                return;
            }
            catch (InterruptedException e)
            {
                System.err.printf("[WRAPPER] Wrapper was interrupted.%n");
                System.exit(EArgs.ERROR);
                return;
            }
            finally
            {
                System.out.printf("[WRAPPER] Shutting down.%n");
                System.exit(EArgs.OK);
            }

            return;
        }

        double t0 = System.currentTimeMillis();

        System.out.printf("[WRAPPER] Wrapping main methods.%n");

        String[] targs = new String[args.length + 1];
        targs[0] = "wrap";
        System.arraycopy(args, 0, targs, 1, args.length);

        sep.view.Launcher.main(targs);

        System.out.printf("[WRAPPER] Wrapper took %.2f seconds to run.%n", (System.currentTimeMillis() - t0) / 1000);

        if (EArgs.getMode() == EArgs.DEFAULT)
        {
            System.err.println("[WRAPPER] Wrapper did not receive a return code from the GUI. Shutting down.");
            System.exit(EArgs.ERROR);
            return;
        }

        if (EArgs.getMode() == EArgs.CLIENT)
        {
            System.out.printf("[WRAPPER] Client closed. Shutting down.%n");
            System.exit(EArgs.OK);
            return;
        }

        if (EArgs.getMode() == EArgs.SERVER)
        {
            System.out.printf("[WRAPPER] Launching server.%n");
            sep.server.Launcher.main(args);
            System.exit(EArgs.OK);
            return;
        }

        if (EArgs.getMode() == EArgs.EXIT)
        {
            System.out.printf("[WRAPPER] Shutting down.%n");
            System.exit(EArgs.OK);
            return;
        }

        System.out.printf("[WRAPPER] The wrapper received an invalid return code from the GUI: %d.%n", EArgs.getMode());
        System.exit(EArgs.ERROR);

        return;
    }

}
