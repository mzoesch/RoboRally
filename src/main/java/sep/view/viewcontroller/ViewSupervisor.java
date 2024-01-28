package sep.view.viewcontroller;

import sep.view.json.game.          ChooseRegisterModel;
import sep.view.json.game.          DiscardSomeModel;
import sep.view.json.game.          SelectedDamageModel;
import sep.view.json.game.          RebootDirectionModel;
import sep.view.json.game.          BuyUpgradeModel;
import sep.                         Types;
import sep.view.scenecontrollers.   LobbyJFXController_v2;
import sep.view.scenecontrollers.   GameJFXController;
import sep.view.json.               ChatMsgModel;
import sep.view.json.               RDefaultServerRequestParser;
import sep.view.clientcontroller.   EClientInformation;
import sep.view.clientcontroller.   GameInstance;
import sep.view.clientcontroller.   EGameState;
import sep.view.lib.                EGamePhase;
import sep.view.lib.                RPopUpMask;
import sep.view.lib.                EAnimation;
import sep.view.lib.                EShopAction;
import sep.view.lib.                RShopAction;
import sep.view.lib.                EUpgradeCard;

import javafx.scene.text.           TextFlow;
import javafx.scene.text.           Text;
import javafx.scene.control.        Label;
import javafx.scene.control.        Button;
import javafx.scene.control.        OverrunStyle;
import javafx.geometry.             Pos;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import org.json.                    JSONArray;
import javafx.application.          Platform;
import javafx.application.          Application;
import javafx.scene.                Node;
import javafx.scene.                Parent;
import javafx.scene.                Scene;
import javafx.stage.                WindowEvent;
import javafx.stage.                Stage;
import javafx.scene.layout.         Pane;
import javafx.scene.layout.         HBox;
import javafx.scene.layout.         VBox;
import javafx.scene.layout.         Region;
import javafx.scene.layout.         AnchorPane;
import javafx.scene.layout.         Priority;
import java.util.                   ArrayList;
import java.util.                   Arrays;
import java.util.                   List;
import java.util.                   Objects;
import java.util.concurrent.atomic. AtomicBoolean;
import java.util.concurrent.atomic. AtomicReference;
import javafx.scene.image.          ImageView;
import javafx.scene.effect.         GaussianBlur;
import javafx.scene.effect.         ColorAdjust;

@FunctionalInterface
interface ImageViewCreator
{
    public abstract ImageView create(final int idx);
}

/**
 * This class is responsible for launching the JavaFX Application Thread and is the gate-way object for all
 * communication to it. This Singleton class is valid during the entire lifetime of the JavaFX Application
 * and not destroyed until the Platform has been exited.
 * {@inheritDoc}
 */
public final class ViewSupervisor extends Application
{
    private static final Logger l = LogManager.getLogger(ViewSupervisor.class);

    /** Used for transitions between scenes. Issued requests will be notified by the post-load behavior of the scene. */
    private static final Object         lock                = new Object();
    private static final AtomicBoolean  bGameSceneLoaded    = new AtomicBoolean(false);

    /** This instance is only valid on the JFX thread. */
    private static ViewSupervisor   INSTANCE;
    private SceneController         sceneController;

    public static final int     TILE_DIMENSIONS             = 96;
    public static final int     VIRTUAL_SPACE_VERTICAL      = 512;
    public static final int     VIRTUAL_SPACE_HORIZONTAL    = 512;
    public static final int     REGISTER_SLOT_WIDTH         = 102;
    public static final int     REGISTER_SLOT_HEIGHT        = 180;
    public static final int     SHOP_DIALOG_SLOT_WIDTH      = 82;
    public static final int     SHOP_DIALOG_SLOT_HEIGHT     = 144;
    public static final int     GOT_REGISTER_SLOT_WIDTH     = 34;
    public static final int     GOT_REGISTER_SLOT_HEIGHT    = 58;
    public static final int     UPGRADE_SLOT_WIDTH          = 34;
    public static final int     UPGRADE_SLOT_HEIGHT         = 58;
    public static final int     PHASE_POPUP_TIME            = 5_000;

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
            s.setTitle(String.format("%s v%s (Mock View)", SceneController.WIN_TITLE, Types.EProps.VERSION.toString()));
            this.sceneController.renderNewScreen(SceneController.GAME_ID, SceneController.PATH_TO_GAME, false);
            s.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, e -> GameInstance.kill());
            s.show();

            return;
        }

        s.setTitle(String.format("%s v%s", SceneController.WIN_TITLE, Types.EProps.VERSION.toString()));
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
            lCtrl.onChatMsg(dsrp.getChatMsgSourceID(), dsrp.getChatMsg(), dsrp.isChatMsgPrivate());
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
            lCtrl.onChatMsg(ChatMsgModel.SERVER_ID, info, false);
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
            lCtrl.onPlayerStatusUpdate();
            return;
        }

        l.error("Received player status update but could not find the correct controller to handle it.");

        return;
    }

    private static void startGame(final JSONArray course)
    {
        EGameState.INSTANCE.setCurrentServerCourseJSON(course);
        ViewSupervisor.getSceneController().renderNewScreen(SceneController.GAME_ID, SceneController.PATH_TO_GAME, false);
        ViewSupervisor.createPhaseUpdatePopUpLater(EGameState.INSTANCE.getCurrentPhase());
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
            lCtrl.onPlayerSelectionUpdate();
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
            ( (LobbyJFXController_v2) ViewSupervisor.getSceneController().getCurrentController() ).onAvailableCourseUpdate(bScrollToEnd);
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
            ( (LobbyJFXController_v2) ViewSupervisor.getSceneController().getCurrentController() ).onCourseSelected();
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

    public static void updateCheckpoints()
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).onCheckpointMoved();
            return;
        }
        catch (final ClassCastException e) {
            l.error("Could not cast current controller to GameJFXController during checkpoint update. Ignoring.");
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

    public static void centerGameCourse()
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).onCenterCourse();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during centering. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void centerGameCourseLater()
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.centerGameCourse();
            return;
        });

        return;
    }

    // endregion Game Events

    public static void createPopUp(final RPopUpMask mask)
    {
        ViewSupervisor.getSceneController().renderPopUp(mask);
        return;
    }

    public static void createPopUpLater(final RPopUpMask mask)
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

    public static void createPopUpLater(final Pane p)
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.createPopUp(p);
            return;
        });

        return;
    }

    public static Thread createPopUp(final Pane p, final int autoDestroyDelay, final boolean bSoftDestroy)
    {
        ViewSupervisor.createPopUp(p);

        if (autoDestroyDelay <= 0)
        {
            return null;
        }

        final Thread t = new Thread(() ->
        {
            l.debug("Waiting {}ms before destroying pop up.", autoDestroyDelay);

            try
            {
                Thread.sleep(autoDestroyDelay);
            }
            catch (final InterruptedException e)
            {
                if (bSoftDestroy)
                {
                    l.debug("Pop up was soft destroyed before auto destroy delay was reached. Ok.");
                    return;
                }

                l.error("Failed to sleep auto popup destroy thread for {}ms.", autoDestroyDelay);
                l.error(e.getMessage());

                return;
            }

            ViewSupervisor.destroyPopUpLater(p, bSoftDestroy);

            return;
        });

        t.start();

        return t;
    }

    public static void destroyPopUpLater(final Pane target, final boolean bSoft)
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.getSceneController().destroyPopUp(target, bSoft);
            return;
        });
    }

    public static AtomicReference<Thread> createPopUpLater(final Pane p, final int autoDestroyDelay)
    {
        return ViewSupervisor.createPopUpLater(p, autoDestroyDelay, false);
    }

    public static AtomicReference<Thread> createPopUpLater(final Pane p, final int autoDestroyDelay, final boolean bSoftDestroy)
    {
        final AtomicReference<Thread> atom = new AtomicReference<Thread>();

        Platform.runLater(() ->
        {
            atom.set(ViewSupervisor.createPopUp(p, autoDestroyDelay, bSoftDestroy));
            return;
        });

        return atom;
    }

    public static void playAnimation(final EAnimation anim)
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

    //Pop-Up für RegisterAuswahl bei AdminPriviledge UpgradeCard
    public static void createRegisterDialog(int selectedRegister) {
        final HBox h = new HBox();
        h.setAlignment(Pos.CENTER);

        final Label header = new Label("Select a register (1-5):");
        header.getStyleClass().add("text-xl");
        header.setStyle("-fx-alignment: center;");

        AnchorPane.setLeftAnchor(header, 0.0);
        AnchorPane.setRightAnchor(header, 0.0);
        AnchorPane.setTopAnchor(header, 50.0);

        final HBox form = new HBox();
        form.setId("register-dialog-body");

        AnchorPane.setLeftAnchor(form, 0.0);
        AnchorPane.setRightAnchor(form, 0.0);
        AnchorPane.setBottomAnchor(form, 50.0);

        for (int i = 1; i <= 5; i++) {
            final Button registerButton = new Button(Integer.toString(i));
            registerButton.getStyleClass().add("secondary-btn");
            registerButton.getStyleClass().add("register-btn");
            form.getChildren().add(registerButton);

            final int selected = i;
            registerButton.setOnAction(e -> {
                new ChooseRegisterModel(selected).send();
                ViewSupervisor.getSceneController().destroyPopUp(h, false);
            });
        }

        final AnchorPane p = new AnchorPane(header, form);
        p.setId("register-dialog-container");

        h.getChildren().add(p);

        AnchorPane.setLeftAnchor(h, 0.0);
        AnchorPane.setRightAnchor(h, 0.0);
        AnchorPane.setTopAnchor(h, 0.0);
        AnchorPane.setBottomAnchor(h, 0.0);

        ViewSupervisor.createPopUp(h);
    }

    //Pop-Up für Kartenauswahl bei MemorySwap UgradeCard
    //TO-DO: Die Karten-Platzhalter durch die Karten auf Hand austauschen
    public static void createCardSelectionDialog() {

        final HBox h = new HBox();
        h.setAlignment(Pos.CENTER);

        final Label header = new Label("Select 3 cards from the List:");
        header.getStyleClass().add("text-xl");
        header.setStyle("-fx-alignment: center;");

        final HBox form = new HBox();
        form.setId("card-selection-dialog-body");

        List<String> availableCards = Arrays.asList("Card1", "Card2", "Card3", "Card4", "Card5", "Card6", "Card7", "Card8", "Card9");
        ArrayList<String> selectedCards = new ArrayList<>();

        for (String card : availableCards) {
            final Button cardButton = new Button(card);
            cardButton.getStyleClass().add("secondary-btn");
            cardButton.getStyleClass().add("card-btn");

            cardButton.setOnAction(e -> {
                if (selectedCards.contains(card)) {
                    selectedCards.remove(card);
                    cardButton.getStyleClass().remove("selected-btn");
                } else {
                    if (selectedCards.size() < 3) {
                        selectedCards.add(card);
                        cardButton.getStyleClass().add("selected-btn");
                    }
                }
            });

            form.getChildren().add(cardButton);
        }

        final Button submitButton = new Button("Submit");
        submitButton.getStyleClass().add("primary-btn");

        submitButton.setOnAction(e -> {
            if (selectedCards.size() == 3) {
                String[] cardsArray = selectedCards.toArray(new String[0]);
                new DiscardSomeModel(cardsArray).send();


                ViewSupervisor.getSceneController().destroyPopUp(h, false);
            } else {
                header.setText("Please select exactly 3 cards!");
            }
        });

        h.getChildren().addAll(header, form, submitButton);

        AnchorPane.setLeftAnchor(h, 0.0);
        AnchorPane.setRightAnchor(h, 0.0);
        AnchorPane.setTopAnchor(h, 0.0);
        AnchorPane.setBottomAnchor(h, 0.0);

        ViewSupervisor.createPopUp(h);
    }

    public static void createRebootDialog()
    {
        final HBox h = new HBox();
        h.setAlignment(Pos.CENTER);

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
            ViewSupervisor.getSceneController().destroyPopUp(h, false);
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
            ViewSupervisor.getSceneController().destroyPopUp(h, false);
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
            ViewSupervisor.getSceneController().destroyPopUp(h, false);
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
            ViewSupervisor.getSceneController().destroyPopUp(h, false);
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

    public static void createPhaseUpdatePopUpLater(final EGamePhase phase)
    {
        final HBox h = new HBox();
        h.setAlignment(Pos.CENTER);

        final Label header = new Label(phase.getDisplayName());
        header.getStyleClass().add("text-xl");
        header.setStyle("-fx-alignment: center;");

        AnchorPane.setLeftAnchor(       header, 0.0      );
        AnchorPane.setRightAnchor(      header, 0.0      );
        AnchorPane.setTopAnchor(        header, 0.0     );
        AnchorPane.setBottomAnchor(     header, 0.0     );

        final AnchorPane p = new AnchorPane(header);
        p.setMouseTransparent(true);
        p.setId("phase-update-container");

        h.getChildren().add(p);

        AnchorPane.setLeftAnchor(       h, 0.0      );
        AnchorPane.setRightAnchor(      h, 0.0      );
        AnchorPane.setTopAnchor(        h, 50.0      );

        ViewSupervisor.createPopUpLater(h, ViewSupervisor.PHASE_POPUP_TIME);

        return;
    }

    /**
     * method to create a DamageCardSelectionDialog. The player can select the cards by clicking on buttons.
     * PopUp auto-deletes after the right ammount of cards has been chosen
     * @param availableCards available piles of damageCards
     * @param countToDraw ammount of cards to draw
     */
    public static void createDamageCardSelectionDialog(String[] availableCards, final int countToDraw)
    {
        ArrayList<String> selectedCards = new ArrayList<>();
        final HBox h = new HBox();
        h.setAlignment(Pos.CENTER);

        final Label header = new Label(String.format("You have to select %s DamageCards to receive.", countToDraw));
        header.getStyleClass().add("text-xl");
        header.setStyle("-fx-alignment: center;");

        AnchorPane.setLeftAnchor(       header, 0.0      );
        AnchorPane.setRightAnchor(      header, 0.0      );
        AnchorPane.setTopAnchor(        header, 50.0     );

        final HBox form = new HBox();
        form.setId("damage-card-selection-dialog-body");

        AnchorPane.setLeftAnchor(       form, 0.0      );
        AnchorPane.setRightAnchor(      form, 0.0      );
        AnchorPane.setBottomAnchor(     form, 50.0      );

        for (String name : availableCards) {

                final Button b = new Button(name);
                b.getStyleClass().add("secondary-btn");
                b.getStyleClass().add("reboot-btn");
                form.getChildren().add(b);
                b.setOnAction(e ->
                        {
                            if ((selectedCards.size() + 1) == countToDraw) {
                                selectedCards.add(name);
                                new SelectedDamageModel(selectedCards);
                                l.debug("DamageCard JSON sent with contents: " + selectedCards.toString());
                                ViewSupervisor.getSceneController().destroyPopUp(h, false);
                                return;
                            } else {
                                selectedCards.add(name);
                                header.setText(String.format("You have to select %s DamageCards to recieve.", (countToDraw - selectedCards.size())));
                            }
                        }
                );
        }

        final AnchorPane p = new AnchorPane(header, form);
        p.setId("damage-card-selection-dialog-container");

        h.getChildren().add(p);

        AnchorPane.setLeftAnchor(       h, 0.0      );
        AnchorPane.setRightAnchor(      h, 0.0      );
        AnchorPane.setTopAnchor(        h, 0.0      );
        AnchorPane.setBottomAnchor(     h, 0.0      );

        ViewSupervisor.createPopUp(h);

        return;
    }

    /**
     * Gateway-method to create a DamageCardSelectionDialog from outside ViewSupervisor
     * @param availableCards available piles of damageCards
     * @param countToDraw ammount of cards to draw
     */
    public static void createDamageCardSelectionDialogLater(String[] availableCards, int countToDraw)
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.createDamageCardSelectionDialog(availableCards, countToDraw);
            return;
        });

        return;
    }

    /**
     * Gateway-method to create a drawDamagePopUp from outside ViewSupervisor
     * @param drawnCards all drawn damageCards as one String
     */
    public static void createDrawDamagePopUpLater(String drawnCards)
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.createDrawDamagePopUp(drawnCards);
            return;
        });

        return;
    }

    /**
     * method to create a drawDamagePopUp to inform the player about the damageCards drawn (auto-deletes after 2000 ms)
     * @param drawnCards all drawn damageCards as one String
     */
    public static void createDrawDamagePopUp(String drawnCards)
    {
        final HBox h = new HBox();
        h.setAlignment(Pos.CENTER);

        final Label header = new Label(String.format("You have drawn the following damage cards: %s.", drawnCards));
        header.setStyle("-fx-alignment: center; -fx-text-alignment: center;");
        header.getStyleClass().add("text-xl");
        header.setWrapText(true);

        AnchorPane.setRightAnchor(      header, 0.0      );
        AnchorPane.setTopAnchor(        header, 0.0      );
        AnchorPane.setBottomAnchor(     header, 0.0      );
        AnchorPane.setLeftAnchor(       header, 0.0      );

        final AnchorPane ap = new AnchorPane(header);
        ap.setId("draw-damage-container");
        ap.setMouseTransparent(true);

        h.getChildren().add(ap);

        AnchorPane.setLeftAnchor(       h, 0.0      );
        AnchorPane.setRightAnchor(      h, 0.0      );
        AnchorPane.setTopAnchor(        h, 50.0     );

        ViewSupervisor.createPopUp(h, 4_000, false);

        return;
    }

    public static void updateFooterState(final boolean bCollapsed)
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).onFooterStateUpdate(bCollapsed);
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during footer state update. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    public static void createShopDialog()
    {
        ViewSupervisor.clearPendingShopActions();

        final HBox h = new HBox();
        h.setAlignment(Pos.CENTER);

        final Label header = new Label("Upgrade Your Robot");
        header.getStyleClass().add("text-2xl");
        header.setStyle("-fx-alignment: center;");

        AnchorPane.setLeftAnchor(       header, 0.0      );
        AnchorPane.setRightAnchor(      header, 0.0      );
        AnchorPane.setTopAnchor(        header, 10.0     );

        final HBox availableCards = new HBox();
        availableCards.setId("upgrade-shop-available-cards");

        for (int i = 0; i < EGameState.INSTANCE.getUpgradeShop().size(); ++i)
        {
            final ImageView     iv  = ViewSupervisor.getUpgradeShopImageAtIndex(i);
            final AnchorPane    ap  = ViewSupervisor.getAnchorPaneForShopSlot(iv, i);

            availableCards.getChildren().add(ap);

            continue;
        }

        final Region LeftSplitLine = new Region();
        LeftSplitLine.getStyleClass().add("upgrade-shop-dialog-splitter");

        final Region RightSplitLine = new Region();
        RightSplitLine.getStyleClass().add("vertical-shop-dialog-splitter");

        final HBox verticalSplitLineBox = new HBox(LeftSplitLine, ViewSupervisor.createHSpacer(), RightSplitLine);

        final Region splitLineAvailableCards = new Region();
        splitLineAvailableCards.getStyleClass().add("upgrade-shop-dialog-splitter");

        final Region fiveGap = new Region();
        fiveGap.setPrefHeight(5);

        final VBox availableCardsWrapper = new VBox(availableCards, fiveGap, verticalSplitLineBox, splitLineAvailableCards);
        availableCardsWrapper.setId("upgrade-shop-available-cards-wrapper");

        final Label availableCardsLabel = new Label("SHOP CARDS");
        availableCardsLabel.getStyleClass().add("text-xl");
        availableCardsLabel.setStyle("-fx-alignment: center;");
        availableCardsLabel.setWrapText(true);

        final Label temporaryCardsLabel = new Label("TEMPORARY");
        temporaryCardsLabel.getStyleClass().add("text-xl");
        temporaryCardsLabel.setStyle("-fx-alignment: center;");
        temporaryCardsLabel.setWrapText(true);
        temporaryCardsLabel.setMaxWidth(Double.MAX_VALUE);

        final Label permanentCardsLabel = new Label("PERMANENT");
        permanentCardsLabel.getStyleClass().add("text-xl");
        permanentCardsLabel.setStyle("-fx-alignment: center;");
        permanentCardsLabel.setWrapText(true);
        permanentCardsLabel.setMaxWidth(Double.MAX_VALUE);

        HBox.setHgrow(temporaryCardsLabel, Priority.ALWAYS);
        HBox.setHgrow(permanentCardsLabel, Priority.ALWAYS);

        final HBox boughtCardsLabelWrapper = new HBox(temporaryCardsLabel, permanentCardsLabel);

        final Region temporaryBoughtCardsHorizontalSplitter = new Region();
        temporaryBoughtCardsHorizontalSplitter.getStyleClass().add("upgrade-shop-dialog-splitter");

        final Region permanentBoughtCardsHorizontalSplitter = new Region();
        permanentBoughtCardsHorizontalSplitter.getStyleClass().add("upgrade-shop-dialog-splitter");

        HBox.setHgrow(temporaryBoughtCardsHorizontalSplitter, Priority.ALWAYS);
        HBox.setHgrow(permanentBoughtCardsHorizontalSplitter, Priority.ALWAYS);

        final HBox boughtCardsHorizontalSplitterWrapper = new HBox(temporaryBoughtCardsHorizontalSplitter, permanentBoughtCardsHorizontalSplitter);
        boughtCardsHorizontalSplitterWrapper.setId("upgrade-shop-bought-cards-horizontal-splitter-wrapper");

        final Region temporaryLeftBoughtCardsVerticalSplitter = new Region();
        temporaryLeftBoughtCardsVerticalSplitter.getStyleClass().add("vertical-shop-dialog-splitter");

        final Region temporaryRightBoughtCardsVerticalSplitter = new Region();
        temporaryRightBoughtCardsVerticalSplitter.getStyleClass().add("vertical-shop-dialog-splitter");

        final Region permanentLeftBoughtCardsVerticalSplitter = new Region();
        permanentLeftBoughtCardsVerticalSplitter.getStyleClass().add("vertical-shop-dialog-splitter");

        final Region permanentRightBoughtCardsVerticalSplitter = new Region();
        permanentRightBoughtCardsVerticalSplitter.getStyleClass().add("vertical-shop-dialog-splitter");

        final HBox temporaryBoughtCardsVerticalSplitterWrapper = new HBox(temporaryLeftBoughtCardsVerticalSplitter, ViewSupervisor.createHSpacer(), temporaryRightBoughtCardsVerticalSplitter);

        final HBox permanentBoughtCardsVerticalSplitterWrapper = new HBox(permanentLeftBoughtCardsVerticalSplitter, ViewSupervisor.createHSpacer(), permanentRightBoughtCardsVerticalSplitter);

        HBox.setHgrow(temporaryBoughtCardsVerticalSplitterWrapper, Priority.ALWAYS);
        HBox.setHgrow(permanentBoughtCardsVerticalSplitterWrapper, Priority.ALWAYS);

        final HBox boughtCardsVerticalSplitterWrapper = new HBox(temporaryBoughtCardsVerticalSplitterWrapper, permanentBoughtCardsVerticalSplitterWrapper);
        boughtCardsVerticalSplitterWrapper.setId("upgrade-shop-bought-cards-vertical-splitter-wrapper");

        final HBox boughtCards = new HBox();
        boughtCards.setId("upgrade-shop-bought-cards");

        for (int i = 0; i < EGameState.INSTANCE.getBoughtUpgradeCard().length; ++i)
        {
            final ImageView iv = ViewSupervisor.getBoughtUpgradeImageAtIndex(i);

            final AnchorPane ap = new AnchorPane(iv);
            ap.setMinSize(ViewSupervisor.SHOP_DIALOG_SLOT_WIDTH, ViewSupervisor.SHOP_DIALOG_SLOT_HEIGHT);
            ap.setMaxSize(ViewSupervisor.SHOP_DIALOG_SLOT_WIDTH, ViewSupervisor.SHOP_DIALOG_SLOT_HEIGHT);

            /* TODO Are we allowed to discard purchased upgrade cards with protocol 2.0? */

            // final int finalI = i;
            iv.setOnMouseClicked(e ->
            {
                return;
            });

            iv.setOnMouseEntered(e ->
            {
            });

            iv.setOnMouseExited(e ->
            {
            });

            boughtCards.getChildren().add(ap);

            continue;
        }

        final Region boughtFiveGap = new Region();
        boughtFiveGap.setPrefHeight(5);

        final VBox boughtWrapper = new VBox(boughtCardsHorizontalSplitterWrapper, boughtCardsVerticalSplitterWrapper, boughtFiveGap, boughtCards);

        final Button b = new Button("Confirm");
        b.getStyleClass().add("secondary-btn");
        b.getStyleClass().add("upgrade-shop-confirm-btn");
        b.setOnAction(e ->
        {
            if (Objects.requireNonNull(ViewSupervisor.getPendingShopActions()).isEmpty())
            {
                l.debug("Client {} decided to not take any actions in this upgrade phase.", EClientInformation.INSTANCE.getPlayerID());
                new BuyUpgradeModel(false, null).send();

                ViewSupervisor.getSceneController().destroyPopUp(h, false);

                for (final Node n : ViewSupervisor.getSceneController().getRenderTarget().getChildren())
                {
                    n.setEffect(null);
                    continue;
                }

                return;
            }

            for (final RShopAction action : ViewSupervisor.getPendingShopActions())
            {
                l.debug("Client {} decided to take action {} in this upgrade phase.", EClientInformation.INSTANCE.getPlayerID(), action);

                if (action.action() == EShopAction.BUY)
                {
                    new BuyUpgradeModel(true, EGameState.INSTANCE.getUpgradeShop(action.idx())).send();
                    continue;
                }

                if (action.action() == EShopAction.SELL)
                {
                    /* TODO: Implement selling. */
                    l.error("Selling is not yet implemented. Ignoring.");
                    continue;
                }

                continue;
            }

            ViewSupervisor.getSceneController().destroyPopUp(h, false);

            for (final Node n : ViewSupervisor.getSceneController().getRenderTarget().getChildren())
            {
                n.setEffect(null);
                continue;
            }

            return;
        });

        final HBox confirmButtonWrapper = new HBox(ViewSupervisor.createHSpacer(), b);

        final VBox form = new VBox(availableCardsWrapper, availableCardsLabel, boughtCardsLabelWrapper, boughtWrapper, confirmButtonWrapper);
        form.setId("upgrade-shop-form");

        final HBox formWrapper = new HBox(form);
        formWrapper.setId("upgrade-shop-form-wrapper");

        AnchorPane.setLeftAnchor(       formWrapper, 0.0      );
        AnchorPane.setRightAnchor(      formWrapper, 0.0      );
        AnchorPane.setBottomAnchor(     formWrapper, 10.0      );

        final AnchorPane p = new AnchorPane(header, formWrapper);
        p.setId("upgrade-dialog-container");

        final GaussianBlur blur     = new GaussianBlur(7.0);
        final ColorAdjust adj       = new ColorAdjust(0.0, -0.9, -0.5, 0.0);
        adj.setInput(blur);
        for (final Node n : ViewSupervisor.getSceneController().getRenderTarget().getChildren())
        {
            n.setEffect(adj);
            continue;
        }

        h.getChildren().add(p);

        AnchorPane.setLeftAnchor(       h, 0.0      );
        AnchorPane.setRightAnchor(      h, 0.0      );
        AnchorPane.setTopAnchor(        h, 0.0      );
        AnchorPane.setBottomAnchor(     h, 0.0      );

        ViewSupervisor.createPopUp(h);

        return;
    }

    public static void createShopDialogLater()
    {
        Platform.runLater(() ->
        {
            ViewSupervisor.createShopDialog();
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

    public static boolean hasLoadedGameScene()
    {
        return Objects.equals(ViewSupervisor.getSceneController().getCurrentScreen().id(), SceneController.GAME_ID);
    }

    public synchronized static void setGameScenePostLoaded()
    {
        ViewSupervisor.bGameSceneLoaded.set(true);
        return;
    }

    public synchronized static boolean isGameScenePostLoaded()
    {
        return ViewSupervisor.bGameSceneLoaded.get();
    }

    public static Object getLoadGameSceneLock()
    {
        return ViewSupervisor.lock;
    }

    private static ImageView getUpgradeShopImageAtIndex(final int idx)
    {
        final ImageViewCreator ivc = (idxImp) ->
        {
            final ImageView iv = new ImageView();

            iv.setFitHeight(ViewSupervisor.SHOP_DIALOG_SLOT_HEIGHT);
            iv.setFitWidth(ViewSupervisor.SHOP_DIALOG_SLOT_WIDTH);
            iv.setImage(TileModifier.loadCachedImage(EGameState.INSTANCE.getUpgradeShop(idxImp) == null ? "EmptyRegisterSlot" : EGameState.INSTANCE.getUpgradeShop(idxImp)));

            return iv;
        };

        return ivc.create(idx);
    }

    private static ImageView getBoughtUpgradeImageAtIndex(final int idx)
    {
        final ImageViewCreator ivc = (idxImp) ->
        {
            final ImageView iv = new ImageView();

            iv.setFitHeight(ViewSupervisor.SHOP_DIALOG_SLOT_HEIGHT);
            iv.setFitWidth(ViewSupervisor.SHOP_DIALOG_SLOT_WIDTH);
            iv.setImage(TileModifier.loadCachedImage(EGameState.INSTANCE.getBoughtUpgradeCard(idxImp) == null ? "EmptyRegisterSlot" : EGameState.INSTANCE.getBoughtUpgradeCard(idxImp)));

            return iv;
        };

        return ivc.create(idx);
    }

    private static Node createHSpacer()
    {
        final Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        return s;
    }

    private static Node createVSpacer()
    {
        final Region s = new Region();
        VBox.setVgrow(s, Priority.ALWAYS);
        return s;
    }

    private static void clearPendingShopActions()
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getPendingShopActions().clear();
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during pending shop actions clear. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    private static void outputPendingShopActions()
    {
        try
        {
            l.debug("Pending shop actions: {}", ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getPendingShopActions().toString());
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during pending shop actions output. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    private static void addShopAction(final RShopAction action)
    {
        try
        {
            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getPendingShopActions().add(action);
            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during pending shop actions add. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    private static void removeShopAction(final RShopAction action)
    {
        try
        {
            if (!( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getPendingShopActions().contains(action))
            {
                l.fatal("Tried to remove a shop action but the pending shop action list does not contain the specified action:; {}", action);
                GameInstance.kill(GameInstance.EXIT_FATAL);
                return;
            }

            ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getPendingShopActions().remove(action);

            return;
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during pending shop actions remove. Ignoring.");
            l.error(e.getMessage());
            return;
        }
    }

    private static ArrayList<RShopAction> getPendingShopActions()
    {
        try
        {
            return ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getPendingShopActions();
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during pending shop actions get. Ignoring.");
            l.error(e.getMessage());
            return null;
        }
    }

    private static AnchorPane getAnchorPaneForShopSlot(final ImageView iv, final int i)
    {
        final AnchorPane ap = new AnchorPane(iv);

        ap.setMinSize(ViewSupervisor.SHOP_DIALOG_SLOT_WIDTH, ViewSupervisor.SHOP_DIALOG_SLOT_HEIGHT);
        ap.setMaxSize(ViewSupervisor.SHOP_DIALOG_SLOT_WIDTH, ViewSupervisor.SHOP_DIALOG_SLOT_HEIGHT);

        ap.setOnMouseEntered(e ->
        {
            if (EGameState.INSTANCE.getUpgradeShop(i) == null)
            {
                return;
            }

            /* A client may only purchase one card for each upgrade phase. */
            if (ViewSupervisor.containsPendingActions(EShopAction.BUY))
            {
                return;
            }

            ap.setStyle("-fx-border-color: #00ff007f; -fx-border-width: 2px;");

            if (Objects.requireNonNull(ViewSupervisor.getPendingShopActions()).contains(new RShopAction(EShopAction.BUY, i)))
            {
                return;
            }

            final Button b = new Button("Buy");
            b.getStyleClass().add("primary-btn-mini");
            b.setStyle("-fx-background-color: #0809d6ff;");

            b.setOnMouseEntered(btnEvent ->     {   b.setStyle("-fx-background-color: #0000ffff;");     return;     });
            b.setOnMouseExited(btnEvent ->      {   b.setStyle("-fx-background-color: #0809d6ff;");     return;     });

            b.setOnAction(onBuy ->
            {
                final RShopAction action = new RShopAction(EShopAction.BUY, i);

                ViewSupervisor.addShopAction(action);
                ViewSupervisor.outputPendingShopActions();

                for (final Node n : ap.getChildren())
                {
                    if (n instanceof final HBox wrapper)
                    {
                        ap.getChildren().remove(wrapper);
                        break;
                    }

                    continue;
                }

                final Button        discardBtn  = new Button();
                final ImageView     checkmark   = new ImageView(TileModifier.loadCachedImage("Checkmark"));
                checkmark.setFitHeight(20);
                checkmark.setFitWidth(20);
                discardBtn.setGraphic(checkmark);
                discardBtn.getStyleClass().add("primary-btn-mini");
                discardBtn.setStyle("-fx-background-color: #0809d6ff;");
                discardBtn.setTextOverrun(OverrunStyle.CLIP);

                discardBtn.setOnMouseEntered(btnEvent ->
                {
                    discardBtn.setStyle("-fx-background-color: #0000ffff;");
                    final ImageView cross = new ImageView(TileModifier.loadCachedImage("Cross"));
                    cross.setFitHeight(20);
                    cross.setFitWidth(20);
                    discardBtn.setGraphic(cross);
                    discardBtn.setTextOverrun(OverrunStyle.CLIP);
                    return;
                });

                discardBtn.setOnMouseExited(btnEvent ->
                {
                    discardBtn.setStyle("-fx-background-color: #0809d6ff;");
                    final ImageView eventCheckmark = new ImageView(TileModifier.loadCachedImage("Checkmark"));
                    eventCheckmark.setFitHeight(20);
                    eventCheckmark.setFitWidth(20);
                    discardBtn.setGraphic(eventCheckmark);
                    discardBtn.setTextOverrun(OverrunStyle.CLIP);
                    return;
                });

                discardBtn.setOnAction(onDiscard ->
                {
                    ViewSupervisor.removeShopAction(new RShopAction(EShopAction.BUY, i));
                    ViewSupervisor.outputPendingShopActions();

                    for (final Node n : ap.getChildren())
                    {
                        if (n instanceof final HBox wrapper)
                        {
                            ap.getChildren().remove(wrapper);
                            break;
                        }

                        continue;
                    }

                    ViewSupervisor.getAnchorPaneForShopSlot(ViewSupervisor.getUpgradeShopImageAtIndex(i), i);

                    ap.setStyle("-fx-border-color: transparent; -fx-border-width: 2px;");

                    return;
                });

                final HBox discardWrapper = new HBox(discardBtn);
                discardWrapper.setAlignment(Pos.CENTER);

                AnchorPane.setLeftAnchor(       discardWrapper, 0.0      );
                AnchorPane.setRightAnchor(      discardWrapper, 0.0      );
                AnchorPane.setTopAnchor(        discardWrapper, 0.0      );
                AnchorPane.setBottomAnchor(     discardWrapper, 0.0      );

                ap.getChildren().add(discardWrapper);

                return;
            });

            final HBox btnWrapper = new HBox(b);
            btnWrapper.setAlignment(Pos.CENTER);

            AnchorPane.setLeftAnchor(       btnWrapper, 0.0      );
            AnchorPane.setRightAnchor(      btnWrapper, 0.0      );
            AnchorPane.setBottomAnchor(     btnWrapper, 0.0      );
            AnchorPane.setTopAnchor(        btnWrapper, 0.0      );

            ap.getChildren().add(btnWrapper);

            return;
        });

        ap.setOnMouseExited(e ->
        {
            if (Objects.requireNonNull(ViewSupervisor.getPendingShopActions()).contains(new RShopAction(EShopAction.BUY, i)))
            {
                return;
            }

            ap.setStyle("-fx-border-color: transparent; -fx-border-width: 2px;");

            for (final Node n : ap.getChildren())
            {
                if (n instanceof final HBox wrapper)
                {
                    ap.getChildren().remove(wrapper);
                    break;
                }

                continue;
            }

            return;
        });

        return ap;
    }

    private static boolean containsPendingActions(final EShopAction action)
    {
        try
        {
            return ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getPendingShopActions().stream().anyMatch(a -> a.action() == action);
        }
        catch (final ClassCastException e)
        {
            l.error("Could not cast current controller to GameJFXController during pending {} shop actions check. Ignoring.", action.toString());
            l.error(e.getMessage());
            return false;
        }
    }

}
