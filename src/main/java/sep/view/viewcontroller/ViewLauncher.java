package sep.view.viewcontroller;

import sep.view.json.DefaultServerRequestParser;
import sep.view.clientcontroller.EClientInformation;
import sep.view.scenecontrollers.LobbyJFXController_v2;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * This class is responsible for launching the JavaFX application.
 * // TODO We may want to rename this class again :). Because it will handle more than just launching the view.
 *         It will also handle updating the view. This class should be the interface between the view and the JSON
 *         parser, {@link #updatePlayerSelection()} as an example.
 */
public final class ViewLauncher extends Application
{
    /** This instance is only valid on the JFX thread. */
    private static ViewLauncher INSTANCE;
    private SceneController sceneController;

    public ViewLauncher()
    {
        super();
        System.out.printf("[CLIENT] Creating View Launcher instance.%n");
        ViewLauncher.INSTANCE = this;
        return;
    }

    @Override
    public void start(Stage stage)
    {
        this.sceneController = new SceneController(new Scene(new Parent(){}, SceneController.PREF_WIDTH, SceneController.PREF_HEIGHT));
        stage.setScene(this.sceneController.getMasterScene());
        stage.setTitle(String.format("%s v%s", SceneController.WIN_TITLE, EClientInformation.PROTOCOL_VERSION));
        this.sceneController.renderNewScreen(SceneController.MAIN_MENU_ID, SceneController.PATH_TO_MAIN_MENU, false);

        stage.show();

        return;
    }

    public static void run()
    {
        Application.launch();
        return;
    }

    // region Updating methods

    public static <T> void handleChatMessage(DefaultServerRequestParser dsrp)
    {
        T ctrl = ViewLauncher.getSceneController().getCurrentController();

        // TODO Here than cast to the game ctrl etc.

        if (ctrl instanceof LobbyJFXController_v2 lobbyCtrl)
        {
            lobbyCtrl.handleChatMessage(dsrp);
            return;
        }

        System.err.printf("[CLIENT] Received chat message but could not find the correct controller to handle it.%n");
        return;
    }

    /** While in lobby selection robot screen. */
    public static void updatePlayerSelection()
    {
        try
        {
            LobbyJFXController_v2 ctrl = (LobbyJFXController_v2) ViewLauncher.getSceneController().getCurrentController();
            ctrl.updatePlayerSelection();
            return;
        }
        catch (ClassCastException e)
        {
            System.err.printf("[CLIENT] Could not cast current controller to LobbyJFXController. Ignoring.%n");
            System.err.printf("[CLIENT] %s%n", e.getMessage());
            return;
        }
    }

    // endregion Updating methods

    /** Only valid on the JFX thread. */
    public static SceneController getSceneController()
    {
        return ViewLauncher.INSTANCE.sceneController;
    }

}
