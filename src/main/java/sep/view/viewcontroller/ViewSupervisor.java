package sep.view.viewcontroller;

import sep.view.json.ChatMsgModel;
import sep.view.json.RDefaultServerRequestParser;
import sep.view.clientcontroller.EClientInformation;
import sep.view.scenecontrollers.LobbyJFXController_v2;
import sep.view.clientcontroller.GameInstance;
import sep.view.clientcontroller.EGameState;
import sep.view.scenecontrollers.GameJFXController;

import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.WindowEvent;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import javafx.application.Platform;

/**
 * This class is responsible for launching the JavaFX application and is
 * the gate-way from outside the JavaFX thread to the JavaFX thread.
 */
public final class ViewSupervisor extends Application
{
    private static final Logger l = LogManager.getLogger(ViewSupervisor.class);

    /** This instance is only valid on the JFX thread. */
    private static ViewSupervisor INSTANCE;
    private SceneController sceneController;

    public static final int TILE_DIMENSIONS = 96;
    public static final int VIRTUAL_SPACE_VERTICAL = 512;
    public static final int VIRTUAL_SPACE_HORIZONTAL = 512;
    public static final int REGISTER_SLOT_WIDTH = 102;
    public static final int REGISTER_SLOT_HEIGHT = 180;
    public static final int GOT_REGISTER_SLOT_WIDTH = 34;
    public static final int GOT_REGISTER_SLOT_HEIGHT = 58;

    public ViewSupervisor()
    {
        super();

        l.debug("Creating View Launcher instance.");
        ViewSupervisor.INSTANCE = this;
        return;
    }

    @Override
    public void start(Stage s)
    {
        this.sceneController = new SceneController(new Scene(new Parent(){}, SceneController.PREF_WIDTH, SceneController.PREF_HEIGHT));
        s.setScene(this.sceneController.getMasterScene());

        if (EClientInformation.INSTANCE.isMockView())
        {
            s.setTitle(String.format("%s v%s (Mock View)", SceneController.WIN_TITLE, EClientInformation.PROTOCOL_VERSION));
            this.sceneController.renderNewScreen(SceneController.GAME_ID, SceneController.PATH_TO_GAME, false);
            s.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> GameInstance.kill());
            s.show();
            return;
        }

        s.setTitle(String.format("%s v%s", SceneController.WIN_TITLE, EClientInformation.PROTOCOL_VERSION));
        this.sceneController.renderNewScreen(SceneController.MAIN_MENU_ID, SceneController.PATH_TO_MAIN_MENU, false);

        s.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> GameInstance.kill());

        s.show();

        return;
    }

    public static void run()
    {
        Application.launch();
        return;
    }

    // region Updating methods

    public static <T> void handleChatMessage(RDefaultServerRequestParser dsrp)
    {
        T ctrl = ViewSupervisor.getSceneController().getCurrentController();

        if (ctrl instanceof LobbyJFXController_v2 lCtrl)
        {
            lCtrl.handleChatMessage(dsrp.getChatMsgSourceID(), dsrp.getChatMsg(), dsrp.isChatMsgPrivate());
            return;
        }

        if (ctrl instanceof GameJFXController gCtrl)
        {
            gCtrl.onChatMsgReceived(dsrp.getChatMsgSourceID(), dsrp.getChatMsg(), dsrp.isChatMsgPrivate());
            return;
        }

        l.error("Received chat message but could not find the correct controller to handle it.");
        return;
    }

    public static <T> void handleChatInfo(String info)
    {
        T ctrl = ViewSupervisor.getSceneController().getCurrentController();

        if (ctrl instanceof LobbyJFXController_v2 lCtrl)
        {
            lCtrl.handleChatMessage(ChatMsgModel.SERVER_ID, info, false);
            return;
        }

        if (ctrl instanceof GameJFXController gCtrl)
        {
            gCtrl.onChatMsgReceived(ChatMsgModel.SERVER_ID, info, false);
            return;
        }

        l.error("Received chat info but could not find the correct controller to handle it.");
        return;
    }

    public static <T> void updatePlayerStatus()
    {
        T ctrl = ViewSupervisor.getSceneController().getCurrentController();

        if (ctrl instanceof LobbyJFXController_v2 lCtrl)
        {
            lCtrl.handlePlayerStatusUpdate();
            return;
        }

        l.error("Received player status update but could not find the correct controller to handle it.");
        return;
    }

    public static void startGame(final JSONArray course)
    {
        EGameState.INSTANCE.setCurrentServerCourseJSON(course);
        ViewSupervisor.INSTANCE.sceneController.renderNewScreen(SceneController.GAME_ID, SceneController.PATH_TO_GAME, false);
        return;
    }

    public static void startGameLater(final JSONArray course)
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.startGame(course);
            return;
        });

        return;
    }
    
    /** While in lobby selection robot screen. */
    public static void updatePlayerSelection()
    {
        try
        {
            if (ViewSupervisor.INSTANCE.sceneController.getCurrentController() instanceof LobbyJFXController_v2 ctrl)
            {
                ctrl.updatePlayerSelection();
                return;
            }

            if (ViewSupervisor.INSTANCE.sceneController.getCurrentController() instanceof GameJFXController ctrl)
            {
                ctrl.onPlayerAdded();
                return;
            }

            l.warn("Could not find a controller to update player selection.");

            return;
        }
        catch (ClassCastException e)
        {
            l.error("Could not cast current controller to LobbyJFXController. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    /** While in lobby screen. */
    public static void updateAvailableCourses(boolean bScrollToEnd)
    {
        try
        {
            LobbyJFXController_v2 ctrl = (LobbyJFXController_v2) ViewSupervisor.getSceneController().getCurrentController();
            ctrl.updateAvailableCourses(bScrollToEnd);
            return;
        }
        catch (ClassCastException e)
        {
            l.error("Could not cast current controller to LobbyJFXController. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    /** While in lobby screen. */
    public static void updateCourseSelected()
    {
        try
        {
            LobbyJFXController_v2 ctrl = (LobbyJFXController_v2) ViewSupervisor.getSceneController().getCurrentController();
            ctrl.updateCourseSelected();
            return;
        }
        catch (ClassCastException e)
        {
            l.error("Could not cast current controller to LobbyJFXController. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updatePlayerView()
    {
        try
        {
            GameJFXController ctrl = (GameJFXController) ViewSupervisor.getSceneController().getCurrentController();
            ctrl.onPlayerUpdate();
            return;
        }
        catch (ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updatePhase()
    {
        try
        {
            GameJFXController ctrl = (GameJFXController) ViewSupervisor.getSceneController().getCurrentController();
            ctrl.onPhaseUpdate();
            return;
        }
        catch (ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updatePlayerTransforms()
    {
        try
        {
            GameJFXController ctrl = (GameJFXController) ViewSupervisor.getSceneController().getCurrentController();
            ctrl.onPlayerTransformUpdate();
            return;
        }
        catch (ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updatePlayerInformationArea()
    {
        try
        {
            final GameJFXController ctrl = (GameJFXController) ViewSupervisor.getSceneController().getCurrentController();
            ctrl.onRPhase();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updateFooter()
    {
        try
        {
            GameJFXController ctrl = (GameJFXController) ViewSupervisor.getSceneController().getCurrentController();
            ctrl.onFooterUpdate();
            return;
        }
        catch (ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static <T> void onPlayerRemoved()
    {
        T ctrl = ViewSupervisor.getSceneController().getCurrentController();

        if (ctrl instanceof LobbyJFXController_v2 lCtrl)
        {
            lCtrl.onPlayerRemoved();
            return;
        }

        if (ctrl instanceof GameJFXController gCtrl)
        {
            gCtrl.onPlayerRemoved();
            return;
        }

        l.error("Wanted to remove player but could not find the correct controller to handle it.");
        return;
    }

    // region Game Events

    public static void updateCourseView()
    {
        try
        {
            GameJFXController ctrl = (GameJFXController) ViewSupervisor.getSceneController().getCurrentController();
            ctrl.onCourseUpdate();
            return;
        }
        catch (ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    // endregion Game Events

    // endregion Updating methods

    /** Only valid on the JFX thread. */
    public static SceneController getSceneController()
    {
        return ViewSupervisor.INSTANCE.sceneController;
    }

}
