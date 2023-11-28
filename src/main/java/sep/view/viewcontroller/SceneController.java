package sep.view.viewcontroller;

import sep.view.clientcontroller.GameInstance;

import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.util.ArrayList;
import javafx.scene.layout.Pane;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Object that handles the overall flow in the Graphical User Interface. It manages the loading of FXML files and
 * the corresponding controllers, the activation of them and destruction.
 */
public final class SceneController
{
    private static final Logger l = LogManager.getLogger(SceneController.class);

    public static final String WIN_TITLE = "CLIENT";
    public static final int PREF_WIDTH = 1_280;
    public static final int PREF_HEIGHT = 720;

    public static final String MAIN_MENU_ID = "main-menu";
    public static final String LOBBY_ID = "lobby";
    public static final String GAME_ID = "game";

    public static final String PATH_TO_MAIN_MENU = "main-menu.fxml";
    /** @deprecated */
    public static final String PATH_TO_LOBBY = "lobby.fxml";
    public static final String PATH_TO_LOBBY_V2 = "lobby_v2.fxml";
    public static final String PATH_TO_GAME = "game.fxml";

    /** The scene where we apply different screens (panes in our case) (They are actually called "nodes".) to. */
    private final Scene masterScene;
    private String currentScreen;
    /** Rendered screens. */
    private final ArrayList<RGameScene<?>> screens;

    public SceneController(Scene masterScene)
    {
        super();

        this.masterScene = masterScene;
        this.currentScreen = "";
        this.screens = new ArrayList<RGameScene<?>>();

        return;
    }

    private <T> void addScreen(RGameScene<T> RGameScene)
    {
        this.screens.add(RGameScene);
        return;
    }

    private void activateScreen(String ID)
    {
        for (RGameScene<?> s : this.screens)
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
        FXMLLoader fxmlLoader = new FXMLLoader(SceneController.class.getResource(path));
        Pane p;
        try
        {
            p = fxmlLoader.load();
        }
        catch (IOException e)
        {
            l.fatal("Failed to load FXML file at {}.", path);
            l.fatal(e.getMessage());
            GameInstance.kill();
            return;
        }
        T ctrl = fxmlLoader.getController();

        RGameScene<T> RGameScene = new RGameScene<T>(ID, p, ctrl, bAutoKillAfterUse ? this.currentScreen : "");

        this.addScreen(RGameScene);
        this.activateScreen(ID);

        return;
    }

    public void killCurrentScreen()
    {
        RGameScene<?> currentScreen = this.getCurrentScreen();
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

    public RGameScene<?> getScreenByID(String ID)
    {
        if (this.screens.isEmpty())
        {
            l.fatal("No screens are loaded.");
            GameInstance.kill();
            return null;
        }

        for (RGameScene<?> s : this.screens)
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
