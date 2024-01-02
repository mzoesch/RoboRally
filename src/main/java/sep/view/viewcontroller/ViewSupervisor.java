package sep.view.viewcontroller;

import sep.view.scenecontrollers.   LobbyJFXController_v2;
import sep.view.scenecontrollers.   GameJFXController;
import sep.view.json.               ChatMsgModel;
import sep.view.json.               RDefaultServerRequestParser;
import sep.view.json.game.          RebootDirectionModel;
import sep.view.clientcontroller.   EClientInformation;
import sep.view.clientcontroller.   GameInstance;
import sep.view.clientcontroller.   EGameState;
import sep.view.lib.                Types;

import javafx.scene.control.        Label;
import javafx.scene.control.        Button;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import org.json.                    JSONArray;
import javafx.application.          Platform;
import javafx.application.          Application;
import javafx.scene.                Parent;
import javafx.scene.                Scene;
import javafx.stage.                WindowEvent;
import javafx.stage.                Stage;
import javafx.scene.layout.         Pane;
import javafx.scene.layout.         HBox;
import javafx.scene.layout.         AnchorPane;

/**
 * This class is responsible for launching the JavaFX Application Thread and is the gate-way object for all
 * communication to it. This Singleton class is valid during the entire lifetime of the JavaFX Application
 * and not destroyed until the Platform has been exited.
 */
public final class ViewSupervisor extends Application
{
    private static final Logger l = LogManager.getLogger(ViewSupervisor.class);

    /** This instance is only valid on the JFX thread. */
    private static ViewSupervisor   INSTANCE;
    private SceneController         sceneController;

    public static final int TILE_DIMENSIONS             = 96;
    public static final int VIRTUAL_SPACE_VERTICAL      = 512;
    public static final int VIRTUAL_SPACE_HORIZONTAL    = 512;
    public static final int REGISTER_SLOT_WIDTH         = 102;
    public static final int REGISTER_SLOT_HEIGHT        = 180;
    public static final int GOT_REGISTER_SLOT_WIDTH     = 34;
    public static final int GOT_REGISTER_SLOT_HEIGHT    = 58;

    public ViewSupervisor()
    {
        super();

        l.debug("Creating View Launcher instance.");
        ViewSupervisor.INSTANCE = this;

        return;
    }

    @Override
    public void start(final Stage s)
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

    public static <T> void handleChatMessage(final RDefaultServerRequestParser dsrp)
    {
        final T ctrl = ViewSupervisor.getSceneController().getCurrentController();

        if (ctrl instanceof final LobbyJFXController_v2 lCtrl)
        {
            lCtrl.handleChatMessage(dsrp.getChatMsgSourceID(), dsrp.getChatMsg(), dsrp.isChatMsgPrivate());
            return;
        }

        if (ctrl instanceof final GameJFXController gCtrl)
        {
            gCtrl.onChatMsgReceived(dsrp.getChatMsgSourceID(), dsrp.getChatMsg(), dsrp.isChatMsgPrivate());
            return;
        }

        l.error("Received chat message but could not find the correct controller to handle it.");

        return;
    }

    public static <T> void handleChatInfo(final String info)
    {
        final T ctrl = ViewSupervisor.getSceneController().getCurrentController();

        if (ctrl instanceof final LobbyJFXController_v2 lCtrl)
        {
            lCtrl.handleChatMessage(ChatMsgModel.SERVER_ID, info, false);
            return;
        }

        if (ctrl instanceof final GameJFXController gCtrl)
        {
            gCtrl.onChatMsgReceived(ChatMsgModel.SERVER_ID, info, false);
            return;
        }

        l.error("Received chat info but could not find the correct controller to handle it.");

        return;
    }

    public static <T> void updatePlayerStatus()
    {
        final T ctrl = ViewSupervisor.getSceneController().getCurrentController();

        if (ctrl instanceof final LobbyJFXController_v2 lCtrl)
        {
            lCtrl.handlePlayerStatusUpdate();
            return;
        }

        l.error("Received player status update but could not find the correct controller to handle it.");

        return;
    }

    private static void startGame(final JSONArray course)
    {
        EGameState.INSTANCE.setCurrentServerCourseJSON(course);
        ViewSupervisor.getSceneController().renderNewScreen(SceneController.GAME_ID, SceneController.PATH_TO_GAME, false);
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
    public static <T> void updatePlayerSelection()
    {
        final T ctrl = ViewSupervisor.getSceneController().getCurrentController();

        if (ctrl instanceof final LobbyJFXController_v2 lCtrl)
        {
            lCtrl.updatePlayerSelection();
            return;
        }

        if (ctrl instanceof final GameJFXController gCtrl)
        {
            gCtrl.onPlayerAdded();
            return;
        }

        l.warn("Could not find a controller to update player selection.");

        return;
    }

    /** While in lobby screen. */
    public static void updateAvailableCourses(final boolean bScrollToEnd)
    {
        try
        {
            ( (LobbyJFXController_v2) ViewSupervisor.getSceneController().getCurrentController() ).updateAvailableCourses(bScrollToEnd);
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to LobbyJFXController during available course update. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    /** While in lobby screen. */
    public static void updateCourseSelected()
    {
        try
        {
            ( (LobbyJFXController_v2) ViewSupervisor.getSceneController().getCurrentController() ).updateCourseSelected();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to LobbyJFXController during course selected update. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updatePlayerView()
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).onPlayerUpdate();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during game view update. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updatePhase()
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).onPhaseUpdate();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during game phase update. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updatePlayerTransforms()
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).onPlayerTransformUpdate();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during player transform update. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updatePlayerInformationArea()
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).onRPhase();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during player information update. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void updateFooter()
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).onFooterUpdate();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during general footer re-render. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static <T> void onPlayerRemoved()
    {
        final T ctrl = ViewSupervisor.getSceneController().getCurrentController();

        if (ctrl instanceof final LobbyJFXController_v2 lCtrl)
        {
            lCtrl.onPlayerRemoved();
            return;
        }

        if (ctrl instanceof final GameJFXController gCtrl)
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
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).onCourseUpdate();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during view re-render. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    // endregion Game Events


    public static void createPopUp(final Types.RPopUpMask mask)
    {
        ViewSupervisor.getSceneController().renderPopUp(mask);
        return;
    }

    public static void createPopUpLater(final Types.RPopUpMask mask)
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.createPopUp(mask);
            return;
        });

        return;
    }

    public static void createPopUp(final Pane p)
    {
        ViewSupervisor.getSceneController().renderPopUp(p);
        return;
    }

    public static void createPopupLater(final Pane p)
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.createPopUp(p);
            return;
        });

        return;
    }

    public static void createPopUp(final Pane p, final int autoDestroyDelay)
    {
        ViewSupervisor.createPopUp(p);

        if (autoDestroyDelay > 0)
        {
            new Thread(() ->
            {
                try
                {
                    l.debug("Waiting {}ms before destroying pop up.", autoDestroyDelay);
                    Thread.sleep(autoDestroyDelay);
                }
                catch (final InterruptedException e)
                {
                    l.error("Failed to sleep thread for {}ms.", autoDestroyDelay);
                    l.error(e.getMessage());
                    return;
                }

                Platform.runLater(() ->
                {
                    ViewSupervisor.getSceneController().destroyPopUp(p);
                    return;
                });

                return;
            }).start();
        }

        return;
    }

    public static void createPopUpLater(final Pane p, final int autoDestroyDelay)
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.createPopUp(p, autoDestroyDelay);
            return;
        });

        return;
    }

    public static void playAnimation(final sep.Types.Animation anim)
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).playAnimation(anim);
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during anim event. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void createRebootDialog()
    {
        final HBox h = new HBox();
        h.setAlignment(javafx.geometry.Pos.CENTER);

        final Label header = new Label("You robot has been rebooted. Select a direction to continue.");
        header.getStyleClass().add("text-xl");
        header.setStyle("-fx-alignment: center;");

        AnchorPane.setLeftAnchor(       header, 0.0      );
        AnchorPane.setRightAnchor(      header, 0.0      );
        AnchorPane.setTopAnchor(        header, 50.0     );

        final HBox form = new HBox();
        form.setId("reboot-dialog-body");

        AnchorPane.setLeftAnchor(       form, 0.0      );
        AnchorPane.setRightAnchor(      form, 0.0      );
        AnchorPane.setBottomAnchor(     form, 50.0      );

        final Button bW = new Button("West");
        bW.getStyleClass().add("secondary-btn");
        bW.getStyleClass().add("reboot-btn");
        form.getChildren().add(bW);
        bW.setOnAction(e ->
        {
            new RebootDirectionModel("right").send();
            ViewSupervisor.getSceneController().destroyPopUp(h);
            return;
        }
        );

        final Button bN = new Button("North");
        bN.getStyleClass().add("secondary-btn");
        bN.getStyleClass().add("reboot-btn");
        form.getChildren().add(bN);
        bN.setOnAction(e ->
        {
            new RebootDirectionModel("top").send();
            ViewSupervisor.getSceneController().destroyPopUp(h);
            return;
        }
        );

        final Button bS = new Button("South");
        bS.getStyleClass().add("secondary-btn");
        bS.getStyleClass().add("reboot-btn");
        form.getChildren().add(bS);
        bS.setOnAction(e ->
        {
            new RebootDirectionModel("bottom").send();
            ViewSupervisor.getSceneController().destroyPopUp(h);
            return;
        }
        );

        final Button bE = new Button("East");
        bE.getStyleClass().add("secondary-btn");
        bE.getStyleClass().add("reboot-btn");
        form.getChildren().add(bE);
        bE.setOnAction(e ->
        {
            new RebootDirectionModel("left").send();
            ViewSupervisor.getSceneController().destroyPopUp(h);
            return;
        }
        );

        final AnchorPane p = new AnchorPane(header, form);
        p.setId("reboot-dialog-container");

        h.getChildren().add(p);

        AnchorPane.setLeftAnchor(       h, 0.0      );
        AnchorPane.setRightAnchor(      h, 0.0      );
        AnchorPane.setTopAnchor(        h, 0.0      );
        AnchorPane.setBottomAnchor(     h, 0.0      );

        ViewSupervisor.createPopUp(h);

        return;
    }

    public static void createRebootDialogLater()
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.createRebootDialog();
            return;
        });

        return;
    }

    // endregion Updating methods

    /** Only valid on the JFX thread. */
    public static SceneController getSceneController()
    {
        return ViewSupervisor.INSTANCE.sceneController;
    }

}
