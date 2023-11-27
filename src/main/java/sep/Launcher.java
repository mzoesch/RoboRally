package sep;

import sep.wrapper.Wrapper;

import java.io.IOException;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO Save screen size and position of the wrapper Graphical User Interface and inherit it to the follow-up process.
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
     * When wrapping. Support for Win and Mac OS X.
     *
     * @param args Valid program arguments in descending order of precedence. All arguments that are not consumed by
     *             the wrapper will be passed down to the follow-up process:
     *             <ul>
     *              <li>[--cmd]     - Start a new process terminal and run this application in it.
     *              <li>[--sv]      - Will instantly start a server process.
     *              <li>[--cl]      - Will instantly start a client process (IO is inherited to calling process).
     *              <li>[--nocmd]   - Will not create a new process terminal for the follow-up server process.
     *              <li>[--noclose] - If allowed a new process terminal will not be closed after the follow-up process
     *                                has exited.
     *             </ul>
     */
    public static void main(final String[] args)
    {
        final double t0 = System.currentTimeMillis();

        l.info("Starting application.");
        l.info("Detected operating system: " + System.getProperty("os.name"));

        /* This only works with jar files because else the getPath() will return a dir. */
        final String fp = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String f = fp.substring(fp.lastIndexOf("/") + 1);

        if (args.length == 0)
        {
            l.info("No command line arguments detected. Starting wrapper with default configuration.");
            Wrapper.run();
        }
        else if (Arrays.asList(args).contains("--cmd"))
        {
            l.info("Command line argument [--cmd] detected. Starting new process terminal.");
            final ProcessBuilder pb =
                    System.getProperty("os.name").toLowerCase().contains("windows")
                    ? new ProcessBuilder(System.getenv("COMSPEC"), "/c", "start", "cmd", "/k", String.format("java -cp %s sep.Launcher %s --nocmd%s", f, String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd")).toArray(String[]::new)), Arrays.asList(args).contains("--noclose") ? "" : " & exit"))
                    :
                    System.getProperty("os.name").toLowerCase().contains("mac")
                    ? new ProcessBuilder(String.format("osascript -e tell application \"Terminal\" to do script \"cd %s && java -cp %s sep.Launcher %s --nocmd%s\"", fp.substring(0, fp.lastIndexOf("/")), f, String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd")).toArray(String[]::new)), Arrays.asList(args).contains("--noclose") ? "" : " & exit"))
                    : null
                    ;
            /* I do not have a linux machine, therefore, I cannot test this. */
            if (pb == null)
            {
                Launcher.stdoutForNoOSSupport(t0);
                return;
            }

            final Process p;
            try
            {
                p = pb.start();
            }
            catch (IOException e)
            {
                l.fatal("Failed to start new process terminal. Shutting down.");
                l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(EArgs.ERR);
                return;
            }

            int RC = EArgs.OK;
            try
            {
                /* We do not inherit IO. This may still be necessary. To be tested. */
                RC = p.waitFor();
            }
            catch (InterruptedException e)
            {
                l.fatal("Process terminal was interrupted. Shutting down.");
                l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(EArgs.ERR);
                return;
            }

            l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(EArgs.OK);
            return;
        }
        else if (Arrays.asList(args).contains("--sv"))
        {
            l.info("Command line argument [--sv] detected. Starting server.");
            EArgs.setMode(EArgs.SERVER);
        }
        else if (Arrays.asList(args).contains("--cl"))
        {
            l.info("Command line argument [--cl] detected. Starting client.");
            EArgs.setMode(EArgs.CLIENT);
        }
        else {
            l.info("No relevant program command line arguments detected that are necessary to start the application. Starting wrapper with default configuration.");
            Wrapper.run();
        }

        l.info("Wrapping complete. Starting follow up process if necessary.");

        if (EArgs.getMode() == EArgs.DEFAULT)
        {
            l.fatal("No mode specified. Shutting down.");
            l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(EArgs.ERR);
            return;
        }
        else if (EArgs.getMode() == EArgs.CLIENT)
        {
            l.info("Launching client.");

            // The client will be started immediately, therefore, the wrapper Graphical User Interface was not started.
            // We can immediately call the client process in this process.
            if (Arrays.asList(args).contains("--cl"))
            {
                sep.view.Launcher.main(Arrays.stream(args).filter(s -> !s.equals("--cl")).toArray(String[]::new));
                l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(EArgs.OK);
                return;
            }

            /* We have to call a new process because we already initialized the wrapper Graphical User Interface. */
            final ProcessBuilder pb = new ProcessBuilder("java", "-cp", f, "sep.view.Launcher", String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cl")).toArray(String[]::new)));
            pb.inheritIO();

            Process p = null;
            try
            {
                p = pb.start();
            }
            catch (IOException e)
            {
                l.fatal("Failed to start client. Shutting down.");
                l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(EArgs.ERR);
                return;
            }

            try
            {
               p.waitFor();
            }
            catch (InterruptedException e)
            {
                l.fatal("Client was interrupted. Shutting down.");
                l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(EArgs.ERR);
                return;
            }

            l.info("Client shutdown. Wrapping complete. Shutting down.");
        }
        else if (EArgs.getMode() == EArgs.SERVER)
        {
            l.info("Launching server.");

            if (Arrays.asList(args).contains("--nocmd"))
            {
                sep.server.Launcher.main(Arrays.stream(args).filter(s -> !s.equals("--nocmd") && !s.equals("--sv")).toArray(String[]::new));
                /* Currently this code is unreachable, because the sv will never terminate. */
                l.info("Server shutdown. Wrapping complete. Shutting down.");
            }
            else
            {
                if (EArgs.getCustomServerPort() != EPort.INVALID.i) /* Set through GUI. */
                {
                    l.info("Detected custom port request: {}.", EArgs.getCustomServerPort());
                }

                final ProcessBuilder pb =
                        System.getProperty("os.name").toLowerCase().contains("windows")
                        ? new ProcessBuilder(System.getenv("COMSPEC"), "/c", "start", "cmd", "/k", String.format("java -cp %s sep.Launcher --sv --nocmd %s %s %s", f, EArgs.getCustomServerPort() != EPort.INVALID.i ? String.format("--port %d", EArgs.getCustomServerPort()) : "", String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd") && !s.equals("--sv") ).toArray(String[]::new)), Arrays.asList(args).contains("--noclose") ? "" : "& exit"))
                        :
                        System.getProperty("os.name").toLowerCase().contains("mac")
                        ? new ProcessBuilder(String.format("osascript -e tell application \"Terminal\" to do script \"cd %s && java -cp %s sep.Launcher --sv --nocmd %s %s %s\"", fp.substring(0, fp.lastIndexOf("/")), f, EArgs.getCustomServerPort() != EPort.INVALID.i ? String.format("--port %d", EArgs.getCustomServerPort()) : "", String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd")).toArray(String[]::new)), Arrays.asList(args).contains("--noclose") ? "" : "& exit"))
                        : null
                        ;
                if (pb == null)
                {
                    Launcher.stdoutForNoOSSupport(t0);
                    System.exit(EArgs.ERR);
                    return;
                }

                Process p = null;
                try
                {
                    p = pb.start();
                }
                catch (IOException e)
                {
                    l.fatal("Failed to start new server process terminal. Shutting down.");
                    l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(EArgs.ERR);
                    return;
                }

                int RC = EArgs.OK;
                try
                {
                    /* We do not inherit IO. This may still be necessary. To be tested. */
                    RC = p.waitFor();
                }
                catch (InterruptedException e)
                {
                    l.fatal("Server process terminal was interrupted. Shutting down.");
                    l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(EArgs.ERR);
                    return;
                }

                l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(EArgs.OK);
                return;
            }
        }
        else if (EArgs.getMode() == EArgs.EXIT)
        {
            l.info("Shutdown requested. Ok.");
            l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(EArgs.OK);
            return;
        }
        else
        {
            l.fatal("Invalid mode. Shutting down.");
            l.fatal("Invalid mode: " + EArgs.getMode());
            l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(EArgs.ERR);
            return;
        }

        l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
        System.exit(EArgs.OK);

        return;
    }

    private static void stdoutForNoOSSupport(double t0)
    {
        l.fatal("Unsupported operating system. Shutting down.");
        l.fatal(String.format("Detected operating system: %s", System.getProperty("os.name")));
        l.info("You may run this application with [--nocmd].");
        l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);

        return;
    }

}
