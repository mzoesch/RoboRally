package sep.view.viewcontroller;

import sep.view.clientcontroller.GameInstance;

import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import java.util.ArrayList;
import javafx.scene.layout.Pane;
import java.io.IOException;

public class SceneController
{
    public static final String WIN_TITLE = "CLIENT";
    public static final int PREF_WIDTH = 1_280;
    public static final int PREF_HEIGHT = 720;

    public static final String MAIN_MENU_ID = "main-menu";
    public static final String LOBBY_ID = "lobby";

    public static final String PATH_TO_MAIN_MENU = "main-menu.fxml";
    public static final String PATH_TO_LOBBY = "lobby.fxml";

    /** The scene where we apply different screens (panes). */
    private final Scene masterScene;
    private String currentScreen;
    private final ArrayList<GameScene<?>> screens;

    public SceneController(Scene masterScene)
    {
        super();

        this.masterScene = masterScene;
        this.currentScreen = "";
        this.screens = new ArrayList<GameScene<?>>();

        return;
    }

    private <T> void addScreen(GameScene<T> gameScene)
    {
        this.screens.add(gameScene);
        return;
    }

    private void activateScreen(String ID)
    {
        for (GameScene<?> s : this.screens)
        {
            if (s.ID().equals(ID))
            {
                GameScene<?> oldScreen = this.screens.size() > 1 ? this.getCurrentScreen() : null;

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
            System.err.printf("%s%n", e.getMessage());
            GameInstance.kill();
            return;
        }
        T ctrl = fxmlLoader.getController();

        GameScene<T> gameScene = new GameScene<T>(ID, p, ctrl, bAutoKillAfterUse ? this.currentScreen : "");

        this.addScreen(gameScene);
        this.activateScreen(ID);

        return;
    }

    public void killCurrentScreen()
    {
        GameScene<?> currentScreen = this.getCurrentScreen();
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

    public GameScene<?> getScreenByID(String ID)
    {
        if (this.screens.isEmpty())
        {
            System.err.printf("[CLIENT] Fatal error. No screens are loaded.%n");
            GameInstance.kill();
            return null;
        }

        for (GameScene<?> s : this.screens)
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

    public GameScene<?> getCurrentScreen()
    {
        return this.getScreenByID(this.currentScreen);
    }

    public <T> T getCurrentController()
    {
        //noinspection unchecked
        return (T) this.getCurrentScreen().controller();
    }

}
