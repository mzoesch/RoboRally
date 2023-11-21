package sep.view.viewcontroller;

import sep.view.json.DefaultServerRequestParser;
import sep.view.scenecontrollers.LobbyJFXController_v2;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewLauncher extends Application
{
    /** This instance is only valid on the JFX thread. */
    private static ViewLauncher INSTANCE;
    private SceneController sceneController;

    public ViewLauncher()
    {
        super();
        System.out.printf("[CLIENT] Creating View instance.%n");
        ViewLauncher.INSTANCE = this;
        return;
    }

    /**
     * While in lobby selection robot screen.
     */
    public static void updatePlayerSelection()
    {
        try
        {
            LobbyJFXController_v2 ctrl = (LobbyJFXController_v2) ViewLauncher.getSceneController().getCurrentController();
            ctrl.updatePlayerSelection();
        }
        catch (ClassCastException e)
        {
            System.err.printf("[CLIENT] Could not cast current controller to LobbyJFXController. Ignoring.%n");
            System.err.printf("[CLIENT] %s%n", e.getMessage());
            return;
        }

        return;
    }

    public static <T> void handleChatMessage (DefaultServerRequestParser dsrp)
    {
        T ctrl = ViewLauncher.getSceneController().getCurrentController();

        if (ctrl instanceof LobbyJFXController_v2 lobbyCtrl)
        {
            lobbyCtrl.handleChatMessage(dsrp);
            return;
        }

        System.err.printf("[CLIENT] Received chat message but could not find the correct controller to handle it.%n");
        return;
    }

    @Override
    public void start(Stage stage)
    {
        this.sceneController = new SceneController(new Scene(new Parent(){}, SceneController.PREF_WIDTH, SceneController.PREF_HEIGHT));
        stage.setScene(this.sceneController.getMasterScene());
        stage.setTitle(SceneController.WIN_TITLE);
        this.sceneController.renderNewScreen(SceneController.MAIN_MENU_ID, SceneController.PATH_TO_MAIN_MENU, false);

        stage.show();

        return;
    }

    public static void run()
    {
        Application.launch();
        return;
    }

    public static SceneController getSceneController()
    {
        return ViewLauncher.INSTANCE.sceneController;
    }

}
