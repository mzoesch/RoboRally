package sep.view.viewcontroller;

import sep.view.clientcontroller.   GameInstance    ;
import sep.view.lib.                Types           ;

import javafx.scene.                Scene           ;
import java.util.                   Objects         ;
import java.util.                   ArrayList       ;
import javafx.scene.layout.         Pane            ;
import javafx.scene.layout.         HBox            ;
import javafx.scene.layout.         AnchorPane      ;
import javafx.scene.layout.         BorderPane      ;
import javafx.fxml.                 FXMLLoader      ;
import javafx.scene.control.        Button          ;
import javafx.scene.control.        Label           ;
import java.io.                     IOException     ;
import org.apache.logging.log4j.    LogManager      ;
import org.apache.logging.log4j.    Logger          ;

/**
 * Singleton object that implements high-level methods relevant for the Graphical User Interface and handles the
 * overall flow it. It is spawned by the {@link ViewSupervisor} at JFX startup and not destroyed until the Platform
 * has been exited.
 *
 * <p> It manages the loading of FXML files and the corresponding controllers, the activation of them and destruction.
 */
public final class SceneController
{
    private static final Logger l = LogManager.getLogger(SceneController.class);

    public static final String  WIN_TITLE       = "CLIENT";
    public static final int     PREF_WIDTH      = 1_280;
    public static final int     PREF_HEIGHT     = 720;

    public static final String  MAIN_MENU_ID    = "main-menu";
    public static final String  LOBBY_ID        = "lobby";
    public static final String  GAME_ID         = "game";
    public static final String  END_SCENE_ID    = "end-scene";

    public static final String  PATH_TO_MAIN_MENU   = "main-menu.fxml";
    /** @deprecated */
    public static final String  PATH_TO_LOBBY       = "lobby.fxml";
    public static final String  PATH_TO_LOBBY_V2    = "lobby_v2.fxml";
    public static final String  PATH_TO_GAME        = "game.fxml";
    public static final String  PATH_TO_END_SCENE   = "end-scene.fxml";

    /** The scene where we apply different screens (panes in our case) to. */
    private final Scene                     masterScene;
    private String                          currentScreen;
    private final ArrayList<RGameScene<?>>  screens;

    public SceneController(final Scene masterScene)
    {
        super();

        this.masterScene    = masterScene;
        this.currentScreen  = "";
        this.screens        = new ArrayList<RGameScene<?>>();

        return;
    }

    private <T> void addScreen(final RGameScene<T> RGameScene)
    {
        this.screens.add(RGameScene);
        return;
    }

    private void activateScreen(final String ID)
    {
        for (final RGameScene<?> s : this.screens)
        {
            if (s.ID().equals(ID))
            {
                RGameScene<?> oldScreen = this.screens.size() > 1 ? this.getCurrentScreen() : null;

                this.currentScreen = ID;
                this.masterScene.setRoot(s.screen());

                if (oldScreen != null && oldScreen.hasFallback())
                {
                    this.screens.remove(oldScreen);
                }

                return;
            }
        }

        l.fatal("Failed to activate screen with ID {}.", ID);
        GameInstance.kill();

        return;
    }

    public <T> void renderNewScreen(String ID, String path, boolean bAutoKillAfterUse)
    {
        final FXMLLoader ldr = new FXMLLoader(SceneController.class.getResource(path));
        final Pane p;
        try
        {
            p = ldr.load();
        }
        catch (final IOException e)
        {
            l.fatal("Failed to load FXML file at {}.", path);
            l.fatal(e.getMessage());
            GameInstance.kill();
            return;
        }

        final T ctrl = ldr.getController();

        final RGameScene<T> rgs = new RGameScene<T>(ID, p, ctrl, bAutoKillAfterUse ? this.currentScreen : "");

        this.addScreen(rgs);
        this.activateScreen(ID);

        return;
    }

    public void renderExistingScreen(final String ID)
    {
        this.activateScreen(ID);
        return;
    }

    public void killCurrentScreen()
    {
        final RGameScene<?> currentScreen = this.getCurrentScreen();
        if (!currentScreen.hasFallback())
        {
            l.fatal("No fallback screen found.");
            GameInstance.kill();
            return;
        }

        this.screens.remove(currentScreen);
        this.activateScreen(currentScreen.fallback());

        return;
    }

    public Scene getMasterScene()
    {
        return this.masterScene;
    }

    public RGameScene<?> getScreenByID(final String ID)
    {
        if (this.screens.isEmpty())
        {
            l.fatal("No screens are loaded.");
            GameInstance.kill();
            return null;
        }

        for (final RGameScene<?> s : this.screens)
        {
            if (s.ID().equals(ID))
            {
                return s;
            }
        }

        l.fatal("No screen with ID {} found.", ID);
        GameInstance.kill();

        return null;
    }

    public RGameScene<?> getCurrentScreen()
    {
        return this.getScreenByID(this.currentScreen);
    }

    public <T> T getCurrentController()
    {
        //noinspection unchecked
        return (T) this.getCurrentScreen().controller();
    }

}
