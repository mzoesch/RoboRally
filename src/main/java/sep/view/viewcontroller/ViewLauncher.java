package sep.view.viewcontroller;

import sep.view.json.DefaultServerRequestParser;
import sep.view.clientcontroller.EClientInformation;
import sep.view.scenecontrollers.LobbyJFXController_v2;
import sep.view.clientcontroller.GameInstance;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.WindowEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class is responsible for launching the JavaFX application.
 * // TODO We may want to rename this class again :). Because it will handle more than just launching the view.
 *         It will also handle updating the view. This class should be the interface between the view and the JSON
 *         parser, {@link #updatePlayerSelection()} as an example.
 */
public final class ViewLauncher extends Application
{
    private static final Logger l = LogManager.getLogger(ViewLauncher.class);

    /** This instance is only valid on the JFX thread. */
    private static ViewLauncher INSTANCE;
    private SceneController sceneController;

    public ViewLauncher()
    {
        super();

        l.debug("Creating View Launcher instance.");
        ViewLauncher.INSTANCE = this;
        return;
    }

    @Override
    public void start(Stage s)
    {
        this.sceneController = new SceneController(new Scene(new Parent(){}, SceneController.PREF_WIDTH, SceneController.PREF_HEIGHT));
        s.setScene(this.sceneController.getMasterScene());

        if (EClientInformation.INSTANCE.getWrap())
        {
            s.setTitle(String.format("%s v%s", sep.EArgs.WIN_TITLE, EClientInformation.PROTOCOL_VERSION));
            this.sceneController.renderNewScreen(SceneController.WRAPPER_ID, SceneController.PATH_TO_WRAPPER, false);
            EClientInformation.INSTANCE.setStage(s);
        }
        else
        {
            s.setTitle(String.format("%s v%s", SceneController.WIN_TITLE, EClientInformation.PROTOCOL_VERSION));
            this.sceneController.renderNewScreen(SceneController.MAIN_MENU_ID, SceneController.PATH_TO_MAIN_MENU, false);
        }

        s.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> GameInstance.kill());

        s.show();

        return;
    }

    public static void run()
    {
        Application.launch();
        return;
    }

    /** Called when wrapping. */
    public static void loadInnerClient()
    {
        EClientInformation.INSTANCE.getStage().setTitle(String.format("%s v%s", SceneController.WIN_TITLE, EClientInformation.PROTOCOL_VERSION));
        ViewLauncher.getSceneController().renderNewScreen(SceneController.MAIN_MENU_ID, SceneController.PATH_TO_MAIN_MENU, false);
        return;
    }

    // region Updating methods

    public static <T> void handleChatMessage(DefaultServerRequestParser dsrp)
    {
        T ctrl = ViewLauncher.getSceneController().getCurrentController();

        // TODO Here than cast to the game ctrl etc.

        if (ctrl instanceof LobbyJFXController_v2 lCtrl)
        {
            lCtrl.handleChatMessage(dsrp);
            return;
        }

        l.error("Received chat message but could not find the correct controller to handle it.");
        return;
    }

    public static <T> void updatePlayerStatus(DefaultServerRequestParser dsrp)
    {
        T ctrl = ViewLauncher.getSceneController().getCurrentController();

        if (ctrl instanceof LobbyJFXController_v2 lCtrl)
        {
            lCtrl.handlePlayerStatusUpdate();
            return;
        }

        l.error("Received player status update but could not find the correct controller to handle it.");
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
            l.error("Could not cast current controller to LobbyJFXController. Ignoring.");
            l.error(e.getMessage());
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
