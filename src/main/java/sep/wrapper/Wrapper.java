package sep.wrapper;

import sep. EArgs;

import javafx.scene.layout.         Pane;
import javafx.application.          Application;
import javafx.stage.                Stage;
import javafx.stage.                WindowEvent;
import javafx.application.          Platform;
import javafx.scene.                Scene;
import javafx.scene.                Parent;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import java.io.                     IOException;
import javafx.fxml.                 FXMLLoader;

public final class Wrapper extends Application
{
    private static final Logger l = LogManager.getLogger(Wrapper.class);

    private static Wrapper INSTANCE;

    private static final String     WIN_TITLE               = "WRAPPER";
    private static final String     PATH_TP_WRAPPER_MENU    = "wrapper.fxml";
    private static final String     PATH_TP_SERVER_CONFIG   = "server-config.fxml";
    private static final String     PATH_TO_AGENT_CONFIG    = "agent-config.fxml";
    private static final int        PREF_WIDTH              = 1_280;
    private static final int        PREF_HEIGHT             = 720;

    private Stage stage;

    public Wrapper()
    {
        super();
        Wrapper.INSTANCE = this;
        return;
    }

    @Override
    public void start(final Stage s) throws IOException
    {
        l.info("Constructing wrapper window.");

        this.stage = s;

        this.stage.setScene(new Scene(new Parent() {}, Wrapper.PREF_WIDTH, Wrapper.PREF_HEIGHT));
        this.stage.setTitle(String.format("%s v-", Wrapper.WIN_TITLE));

        Wrapper.loadWrapperMenu();

        this.stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e ->
        {
            l.info("Window close request detected.");
            sep.EArgs.setMode(sep.EArgs.EMode.EXIT);
            Wrapper.exitWrapper();
            return;
        });

        this.stage.show();

        return;
    }

    public static void run()
    {
        Application.launch();
        return;
    }

    private static boolean loadFXML(final String path)
    {
        final FXMLLoader ldr = new FXMLLoader(Wrapper.class.getResource(path));

        final Pane p;
        try
        {
            p = ldr.load();
        }
        catch (final IOException e)
        {
            l.fatal("Failed to load wrapper menu.");
            l.fatal(e);
            return false;
        }

        Wrapper.INSTANCE.stage.getScene().setRoot(p);

        return true;
    }

    public static void loadWrapperMenu()
    {
        final boolean bSuccess = Wrapper.loadFXML(Wrapper.PATH_TP_WRAPPER_MENU);
        if (!bSuccess)
        {
            EArgs.setMode(EArgs.EMode.EXIT);
            Wrapper.exitWrapper();
            return;
        }

        l.info("Wrapper menu scene loaded.");

        return;
    }

    public static void loadServerConfig()
    {
        final boolean bSuccess = Wrapper.loadFXML(Wrapper.PATH_TP_SERVER_CONFIG);
        if (!bSuccess)
        {
            EArgs.setMode(EArgs.EMode.EXIT);
            Wrapper.exitWrapper();
            return;
        }

        l.info("Server config scene loaded.");

        return;
    }

    public static void loadAgentConfig()
    {
        final boolean bSuccess = Wrapper.loadFXML(Wrapper.PATH_TO_AGENT_CONFIG);
        if (!bSuccess)
        {
            EArgs.setMode(EArgs.EMode.EXIT);
            Wrapper.exitWrapper();
            return;
        }

        l.info("Agent config scene loaded.");

        return;
    }

    public static void exitWrapper()
    {
        Platform.exit();
        l.debug("Platform exited.");
        return;
    }

}
