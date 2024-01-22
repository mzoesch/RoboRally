package sep;

import sep.wrapper.                 Wrapper;
import sep.server.model.game.       GameState;

import java.util.                   Arrays;
import java.util.                   Objects;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     IOException;

/* TODO Save screen size and position of the wrapper Graphical User Interface and inherit it to the follow-up process. */
/* TODO Osascript will always open a new window (Meaning two if Terminal is not running). */
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
     * When wrapping. Support for Win and OSX.
     *
     * @param args Valid program arguments in descending order of precedence. All arguments that are not consumed by
     *             the wrapper will be passed down to the follow-up process. Invalid arguments will be ignored (the
     *             follow-up process might not ignore invalid arguments and may fail to start):
     *             <ul>
     *              <li>[--cmd]     - Start a new process terminal and run this application in it.
     *              <li>[--sv]      - Will instantly start a server process.
     *              <li>[--cl]      - Will instantly start a client process (IO is inherited to calling process).
     *              <li>[--nocmd]   - Will not create a new process terminal for the follow-up server process.
     *              <li>[--noclose] - If allowed a new process terminal will not be closed after the follow-up process
     *                                has exited.
     *              <li>[--help]    - Print help message.
     *             </ul>
     */
    public static void main(final String[] args)
    {
        final double t0 = System.currentTimeMillis();

        l.info(     "Starting application."                                              );
        l.debug(    "Detected operating system: {}.",   Types.EOS.getOS().toString()     );
        l.debug(    "Protocol version: {}.",            Types.EProps.VERSION.toString()  );

        if (Types.EConfigurations.isDev())
        {
            l.error("Wrapper code is not meant to run outside of MVN packaged JAR files.");
        }

        final String fp     = Launcher.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String f      = fp.substring(fp.lastIndexOf("/") + 1);

        if (f.isEmpty() || f.isBlank())
        {
            l.error("Failed to detect JAR file. If this is not a development build consider running the application with [--nocmd] and the [--sv] or [--cl] flag.");
        }
        else
        {
            l.debug("Detected JAR file: {}.", f);
        }

        if (args.length == 0)
        {
            l.info("No command line arguments detected. Starting wrapper with default configuration.");
            Wrapper.run();
        }
        else if (Arrays.asList(args).contains("--cmd"))
        {
            l.info("Command line argument [--cmd] detected. Starting new process terminal.");

            final ProcessBuilder pb =
                Types.EOS.isWindows()
                ?
                new ProcessBuilder(
                    System.getenv("COMSPEC"), "/c", "start", "cmd", "/k",
                    String.format(
                        "java -cp %s sep.Launcher %s --nocmd%s",
                        f,
                        String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd")).toArray(String[]::new)),
                        Arrays.asList(args).contains("--noclose") ? "" : " & exit"
                        )
                    )
                :
                Types.EOS.isOSX()
                ?
                new ProcessBuilder(
                    "osascript", "-e",
                    String.format(
                        "tell application \"Terminal\" to do script \"cd %s && java -cp %s sep.Launcher %s --nocmd%s\"",
                        fp.substring(0, fp.lastIndexOf("/")),
                        f,
                        String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd")).toArray(String[]::new)),
                        Arrays.asList(args).contains("--noclose") ? "" : " & exit"
                        )
                    )
                : null
                ;

            /* I do not have a linux machine, therefore, I cannot test this. */
            if (pb == null)
            {
                Launcher.stdoutForNoOSSupport(t0);
                System.exit(EArgs.ERR);
                return;
            }

            /* The process cannot start if not packaged in a jar file. We inherit to get the stderr. */
            if (Types.EConfigurations.isDev())
            {
                pb.inheritIO();
            }

            final Process p;
            try
            {
                p = pb.start();
            }
            catch (final IOException e)
            {
                l.fatal("Failed to start new process terminal. Shutting down.");
                l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(EArgs.ERR);
                return;
            }

            int RC = EArgs.OK;
            try
            {
                RC = p.waitFor();
            }
            catch (final InterruptedException e)
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
            EArgs.setMode(EArgs.EMode.SERVER);
        }
        else if (Arrays.asList(args).contains("--cl"))
        {
            l.info("Command line argument [--cl] detected. Starting client.");
            EArgs.setMode(EArgs.EMode.CLIENT);
        }
        else if (Arrays.asList(args).contains("--help"))
        {
            l.info("Command line argument [--help] detected. Printing help message.");
            EArgs.setMode(EArgs.EMode.HELP);
        }
        else
        {
            l.info("No relevant program command line arguments detected that are necessary to start the application. Starting wrapper with default configuration.");
            Wrapper.run();
        }

        l.info("Wrapping complete. Starting follow up process if necessary.");

        if (EArgs.getMode() == EArgs.EMode.DEFAULT)
        {
            l.fatal("No mode specified. Shutting down.");
            l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(EArgs.ERR);
            return;
        }
        else if (EArgs.getMode() == EArgs.EMode.CLIENT)
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

            final Process p;
            try
            {
                p = pb.start();
            }
            catch (final IOException e)
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
            catch (final InterruptedException e)
            {
                l.fatal("Client was interrupted. Shutting down.");
                l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                System.exit(EArgs.ERR);
                return;
            }

            l.info("Client shutdown. Wrapping complete. Shutting down.");
        }
        else if (EArgs.getMode() == EArgs.EMode.SERVER)
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
                if (EArgs.getCustomServerPort() != sep.Types.EPort.INVALID.i)
                {
                    l.info("Detected custom port request: {}.", EArgs.getCustomServerPort());
                }

                if (EArgs.getCustomMinRemotePlayers() != EArgs.DEFAULT_MIN_REMOTE_PLAYERS)
                {
                    l.info("Detected custom minimum remote players request: {}.", EArgs.getCustomMinRemotePlayers());
                }

                final ProcessBuilder pb =
                    Types.EOS.isWindows()
                    ?
                    new ProcessBuilder(
                        System.getenv("COMSPEC"), "/c", "start", "cmd", "/k",
                        String.format(
                            "java -cp %s sep.Launcher --sv --nocmd %s %s %s %s",
                            f,
                            EArgs.getCustomServerPort() != sep.Types.EPort.INVALID.i ? String.format("--port %d", EArgs.getCustomServerPort()) : "",
                            EArgs.getCustomMinRemotePlayers() != EArgs.DEFAULT_MIN_REMOTE_PLAYERS ? String.format("--minRemotePlayers %d", EArgs.getCustomMinRemotePlayers()) : "",
                            String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd") && !s.equals("--sv") ).toArray(String[]::new)),
                            Arrays.asList(args).contains("--noclose") ? "" : "& exit"
                            )
                        )
                    :
                    Types.EOS.isOSX()
                    ?
                    new ProcessBuilder(
                        "osascript", "-e",
                        String.format(
                            "tell application \"Terminal\" to do script \"cd %s && java -cp %s sep.Launcher --sv --nocmd %s %s %s %s\"",
                            fp.substring(0, fp.lastIndexOf("/")),
                            f,
                            EArgs.getCustomServerPort() != sep.Types.EPort.INVALID.i ? String.format("--port %d", EArgs.getCustomServerPort()) : "",
                            EArgs.getCustomMinRemotePlayers() != EArgs.DEFAULT_MIN_REMOTE_PLAYERS ? String.format("--minRemotePlayers %d", EArgs.getCustomMinRemotePlayers()) : "",
                            String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd")).toArray(String[]::new)),
                            Arrays.asList(args).contains("--noclose") ? "" : "& exit"
                            )
                        )
                    : null
                    ;

                if (pb == null)
                {
                    Launcher.stdoutForNoOSSupport(t0);
                    System.exit(EArgs.ERR);
                    return;
                }

                /* The process cannot start if not packaged in a jar file. We inherit to get the stderr. */
                if (Types.EConfigurations.isDev())
                {
                    pb.inheritIO();
                }

                final Process p;
                try
                {
                    p = pb.start();
                }
                catch (final IOException e)
                {
                    l.fatal("Failed to start new server process terminal. Shutting down.");
                    l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(EArgs.ERR);
                    return;
                }

                int RC = EArgs.OK;
                try
                {
                    RC = p.waitFor();
                }
                catch (final InterruptedException e)
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
        else if (EArgs.getMode() == EArgs.EMode.EXIT)
        {
            l.info("Shutdown requested. Ok.");
            l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(EArgs.OK);
            return;
        }
        else if (EArgs.getMode() == EArgs.EMode.HELP)
        {
            l.info("");
            l.info("#################### WRAPPER HELP #####################");
            l.info("Valid wrapper program arguments in descending order of precedence. All arguments that are not consumed by the wrapper will be passed down to the follow-up process. Invalid arguments will be ignored (the follow-up process might not ignore invalid arguments and may fail to start):");
            l.info("Usage: java -jar {jar-name}.jar [--cmd] [--sv] [--cl] [--nocmd] [--noclose] [--help]");
            l.info("  --cmd         Start a new process terminal and run this application in it.");
            l.info("  --sv          Will instantly start a server process.");
            l.info("  --cl          Will instantly start a client process (IO is inherited to calling process).");
            l.info("  --nocmd       Will not create a new process terminal for the follow-up server process.");
            l.info("  --noclose     If allowed a new process terminal will not be closed after the follow-up process has exited.");
            l.info("  --help        Print this help message.");
            l.info("");
            l.info("##################### CLIENT HELP #####################");
            l.info("Valid view program arguments in descending order of precedence. Invalid arguments will be ignored.");
            l.info("Usage: java -cp {jar-name}.jar sep.view.Launcher [--dev] [--isAgent] [--addr ADDR] [--port PORT] [--sid SID] [--name NAME] [--allowLegacyAgents] [--help]");
            l.info("Valid server program arguments in descending order of precedence.");
            l.info("  --dev                 Start mock game view (if also started with the [--isAgent] flag, the agent mock view will be called instead).");
            l.info("  --isAgent             Start agent view.");
            l.info("  --addr <ADDR>         The address to auto connect to (if isAgent flag is set). Default is {}.", Types.EPort.DEFAULT.i);
            l.info("  --port <PORT>         The port number to auto connect to (if isAgent flag is set). Default is {}.", sep.EArgs.PREF_SERVER_PORT);
            l.info("  --sid <SID>           The session ID to auto connect to (if isAgent flag is set). Default is {}.", sep.Types.EProps.DESCRIPTION);
            l.info("  --name <NAME>         The name of the agent (if isAgent flag is set).");
            l.info("  --allowLegacyAgents   Allow legacy agent logic to be displayed in the client Graphical User Interface (the deprecated server agent logic will be used).");
            l.info("  --help                Print view help message.");
            l.info("");
            l.info("##################### SERVER HELP #####################");
            l.info("Valid server program arguments in descending order of precedence. Invalid arguments will be ignored.");
            l.info("Usage: java -cp {jar-name}.jar sep.server.Launcher [--port PORT] [--minRemotePlayers MIN_REMOTE_PLAYERS] [--minHumanPlayers MIN_HUMAN_PLAYERS] [--help]");
            l.info("  --port <PORT>                             The port number to listen on. Default is {}.", sep.Types.EPort.DEFAULT.i);
            l.info("  --minRemotePlayers <MIN_REMOTE_PLAYERS>   The minimum number of remote clients required to start a game. Default is {}. Only used in legacy games.", GameState.DEFAULT_MIN_REMOTE_PLAYER_COUNT_TO_START);
            l.info("  --minHumanPlayers <MIN_HUMAN_PLAYERS>     The minimum number of human players required to start a game. Default is {}.", GameState.DEFAULT_MIN_HUMAN_PLAYER_COUNT_TO_START);
            l.info("  --help                                    Print server help message.");
            l.info("");
            l.info("#######################################################");
            l.info("");

            l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
            System.exit(EArgs.OK);

            return;
        }
        else if (EArgs.getMode() == EArgs.EMode.AGENT)
        {
            l.info("Parsing agent configurations.");

            if (EArgs.getAllowClientStart())
            {
                l.info("Detected client start request.");
            }

            if (EArgs.getAllowServerStart())
            {
                l.info("Detected server start request.");
            }

            if (!EArgs.getAgentNames().isEmpty())
            {
                l.info("Detected {} agent starting request{}.", EArgs.getAgentNames().size(), EArgs.getAgentNames().size() > 1 ? "s" : "");
            }

            if (!Objects.equals(EArgs.getCustomServerIP(), ""))
            {
                l.info("Detected custom server ip request: {}.", EArgs.getCustomServerIP());
            }

            if (EArgs.getCustomServerPort() != sep.Types.EPort.INVALID.i)
            {
                l.info("Detected custom port request: {}.", EArgs.getCustomServerPort());
            }

            if (!Objects.equals(EArgs.getCustomSessionID(), ""))
            {
                l.info("Detected custom session id request: {}.", EArgs.getCustomSessionID());
            }

            if (EArgs.getCustomMinRemotePlayers() != EArgs.DEFAULT_MIN_REMOTE_PLAYERS)
            {
                l.info("Detected custom minimum remote players request: {}.", EArgs.getCustomMinRemotePlayers());
            }

            if (EArgs.getCustomMinHumanPlayers() != EArgs.DEFAULT_MIN_HUMAN_PLAYERS)
            {
                l.info("Detected custom minimum human players request: {}.", EArgs.getCustomMinRemotePlayers());
            }

            if (EArgs.getAllowClientStart())
            {
                l.info("Launching client.");
                try
                {
                    /* TODO We may want to give information to the process. Like a requested custom ip address, session id etc. */
                    new ProcessBuilder("java", "-cp", f, "sep.view.Launcher", String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cl")).toArray(String[]::new))).start();
                }
                catch (final IOException e)
                {
                    l.error("Failed to start client. Ignoring. Trying to start other requested processes.");
                }
            }

            if (EArgs.getAllowServerStart())
            {
                l.info("Launching server.");

                final ProcessBuilder pb =
                    Types.EOS.isWindows()
                    ?
                    new ProcessBuilder(
                        System.getenv("COMSPEC"), "/c", "start", "cmd", "/k",
                        String.format(
                            "java -cp %s sep.Launcher --sv --nocmd %s %s %s %s",
                            f,
                            EArgs.getCustomServerPort() != Types.EPort.INVALID.i ? String.format("--port %d", EArgs.getCustomServerPort()) : "",
                            EArgs.getCustomMinHumanPlayers() != EArgs.DEFAULT_MIN_HUMAN_PLAYERS ? String.format("--minRemotePlayers %d", EArgs.getCustomMinHumanPlayers()) : "",
                            String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd") && !s.equals("--sv") ).toArray(String[]::new)),
                            Arrays.asList(args).contains("--noclose") ? "" : "& exit"
                        )
                    )
                    :
                    Types.EOS.isOSX()
                    ?
                    new ProcessBuilder(
                        "osascript", "-e",
                        String.format(
                            "tell application \"Terminal\" to do script \"cd %s && java -cp %s sep.Launcher --sv --nocmd %s %s %s %s\"",
                            fp.substring(0, fp.lastIndexOf("/")),
                            f,
                            EArgs.getCustomServerPort() != Types.EPort.INVALID.i ? String.format("--port %d", EArgs.getCustomServerPort()) : "",
                            EArgs.getCustomMinHumanPlayers() != EArgs.DEFAULT_MIN_HUMAN_PLAYERS ? String.format("--minRemotePlayers %d", EArgs.getCustomMinHumanPlayers()) : "",
                            String.join(" ", Arrays.stream(args).filter(s -> !s.equals("--cmd")).toArray(String[]::new)),
                            EArgs.getNoClose() ? "" : Arrays.asList(args).contains("--noclose") ? "" : "& exit"
                        )
                    )
                    : null
                    ;

                if (pb == null)
                {
                    Launcher.stdoutForNoOSSupport(t0);
                    System.exit(EArgs.ERR);
                    return;
                }

                try
                {
                    pb.start();
                }
                catch (final IOException e)
                {
                    l.error("Failed to start server. Ignoring. Trying to start other requested processes.");
                }
            }

            for (int i = 0; i < EArgs.getAgentNames().size(); ++i)
            {
                l.info("Launching agent {} [{} of {}].", EArgs.getAgentNames().get(i), i + 1, EArgs.getAgentNames().size());

                final ProcessBuilder pb =
                    Types.EOS.isWindows()
                    ?
                    new ProcessBuilder(
                        System.getenv("COMSPEC"), "/c", "start", "cmd", "/k",
                        String.format(
                            "java -cp %s sep.Launcher --cl --nocmd %s %s %s %s %s %s",
                            f,
                            "--isAgent",
                            !Objects.equals(EArgs.getCustomServerIP(), "") ? String.format("--ip %s", EArgs.getCustomServerIP()) : "",
                            EArgs.getCustomServerPort() != Types.EPort.INVALID.i ? String.format("--port %d", EArgs.getCustomServerPort()) : "",
                            String.format("--name \"%s\"", EArgs.getAgentNames().get(i)),
                            !Objects.equals(EArgs.getCustomSessionID(), "") ? String.format("--sid %s", EArgs.getCustomSessionID()) : "",
                            EArgs.getNoClose() ? "" : Arrays.asList(args).contains("--noclose") ? "" : "& exit"
                        )
                    )
                    :
                    Types.EOS.isOSX()
                    ?
                    new ProcessBuilder(
                        "osascript", "-e",
                        String.format(
                            "tell application \"Terminal\" to do script \"cd %s && java -cp %s sep.Launcher --sv --nocmd %s %s %s %s %s %s\"",
                            fp.substring(0, fp.lastIndexOf("/")),
                            f,
                            "--isAgent",
                            !Objects.equals(EArgs.getCustomServerIP(), "") ? String.format("--ip %s", EArgs.getCustomServerIP()) : "",
                            EArgs.getCustomServerPort() != Types.EPort.INVALID.i ? String.format("--port %d", EArgs.getCustomServerPort()) : "",
                            String.format("--name \"%s\"", EArgs.getAgentNames().get(i)),
                            !Objects.equals(EArgs.getCustomSessionID(), "") ? String.format("--sid %s", EArgs.getCustomSessionID()) : "",
                            EArgs.getNoClose() ? "" : Arrays.asList(args).contains("--noclose") ? "" : "& exit"
                        )
                    )
                    : null
                    ;

                if (pb == null)
                {
                    Launcher.stdoutForNoOSSupport(t0);
                    System.exit(EArgs.ERR);
                    return;
                }

                try
                {
                    pb.start();
                }
                catch (final IOException e)
                {
                    l.error("Failed to start agent. Ignoring. Trying to start other requested processes.");
                }

                // TODO
                //      We to this to prevent a rare case of a Concurrent Modification Exception in the server.
                //      This is a bug on the server and not on the client and should properly be fixed ASAP.
                try
                {
                    //noinspection BusyWait
                    Thread.sleep(200); /* On slower end hardware, this delay must be increased. */
                }
                catch (final InterruptedException e)
                {
                    l.fatal("Wrapper was interrupted. This was probably unintentional. Shutting down.");
                    l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);
                    System.exit(EArgs.ERR);
                    return;
                }

                continue;
            }

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

    private static void stdoutForNoOSSupport(final double t0)
    {
        l.fatal("Unsupported operating system. Shutting down.");
        l.fatal(String.format("Detected operating system: %s", System.getProperty("os.name")));
        l.info("You may run this application with [--nocmd].");
        l.debug("The wrapper application took {} seconds to run.", (System.currentTimeMillis() - t0) / 1000);

        return;
    }

}
