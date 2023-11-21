package sep.view.viewcontroller;

import sep.view.clientcontroller.GameInstance;

import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.util.ArrayList;
import javafx.scene.layout.Pane;
import java.io.IOException;

/**
 * Object that handles the overall flow in the Graphical User Interface. It manages the loading of FXML files and
 * the corresponding controllers, the activation of them and destruction.
 */
public final class SceneController
{
    public static final String WIN_TITLE = "CLIENT";
    public static final int PREF_WIDTH = 1_280;
    public static final int PREF_HEIGHT = 720;

    public static final String MAIN_MENU_ID = "main-menu";
    public static final String LOBBY_ID = "lobby";

    public static final String PATH_TO_MAIN_MENU = "main-menu.fxml";
    /** @deprecated */
    public static final String PATH_TO_LOBBY = "lobby.fxml";
    public static final String PATH_TO_LOBBY_V2 = "lobby_v2.fxml";

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

        System.err.printf("[CLIENT] Failed to activate screen with ID %s.%n", ID);
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
            System.err.printf("[CLIENT] Failed to load FXML file at %s.%n", path);
            System.err.printf("[CLIENT] %s%n", e.getMessage());
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
            System.err.println("[CLIENT] Fatal error. No fallback screen found.");
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
            System.err.printf("[CLIENT] Fatal error. No screens are loaded.%n");
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

        System.err.printf("[CLIENT] Fatal error. No screen with ID %s found.%n", ID);
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
