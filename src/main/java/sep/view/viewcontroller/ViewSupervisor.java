package sep.view.viewcontroller;

import sep.view.json.game.ChooseRegisterModel;
import sep.view.json.game.DiscardSomeModel;
import sep.view.json.game.          SelectedDamageModel;
import sep.view.json.game.          RebootDirectionModel;
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

import javafx.scene.control.        Label;
import javafx.scene.control.        Button;
import javafx.geometry.             Pos;
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
import java.util.                   ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic. AtomicReference;

/**
 * This class is responsible for launching the JavaFX Application Thread and is the gate-way object for all
 * communication to it. This Singleton class is valid during the entire lifetime of the JavaFX Application
 * and not destroyed until the Platform has been exited.
 * {@inheritDoc}
 */
public final class ViewSupervisor extends Application
{
    private static final Logger l = LogManager.getLogger(ViewSupervisor.class);

    /** This instance is only valid on the JFX thread. */
    private static ViewSupervisor   INSTANCE;
    private SceneController         sceneController;

    public static final int     TILE_DIMENSIONS             = 96;
    public static final int     VIRTUAL_SPACE_VERTICAL      = 512;
    public static final int     VIRTUAL_SPACE_HORIZONTAL    = 512;
    public static final int     REGISTER_SLOT_WIDTH         = 102;
    public static final int     REGISTER_SLOT_HEIGHT        = 180;
    public static final int     GOT_REGISTER_SLOT_WIDTH     = 34;
    public static final int     GOT_REGISTER_SLOT_HEIGHT    = 58;
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

                l.error("Failed to sleep thread for {}ms.", autoDestroyDelay);
                l.error(e.getMessage());

                return;
            }

            Platform.runLater(() ->
            {
                ViewSupervisor.getSceneController().destroyPopUp(p, bSoftDestroy);
                return;
            });

            return;
        });

        t.start();

        return t;
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

        AnchorPane.setLeftAnchor(header, 0.0);
        AnchorPane.setRightAnchor(header, 0.0);
        AnchorPane.setTopAnchor(header, 50.0);

        final HBox form = new HBox();
        form.setId("card-selection-dialog-body");

        AnchorPane.setLeftAnchor(form, 0.0);
        AnchorPane.setRightAnchor(form, 0.0);
        AnchorPane.setBottomAnchor(form, 50.0);

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
                String[] selectedCardsArray = Arrays.copyOf(cardsArray, 3);
                String cards = Arrays.toString(selectedCardsArray);
                new DiscardSomeModel(cardsArray).send();

                ViewSupervisor.getSceneController().destroyPopUp(h, false);
            } else {
                header.setText("Please select exactly 3 cards!");
            }
        });
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

        final Label header = new Label("Damage Cards drawn");
        header.getStyleClass().add("text-xl");
        header.setStyle("-fx-alignment: center;");

        AnchorPane.setLeftAnchor(       header, 0.0      );
        AnchorPane.setRightAnchor(      header, 0.0      );
        AnchorPane.setTopAnchor(        header, 50.0     );

        final Label form = new Label("You have drawn following damage cards: " + drawnCards);
        form.getStyleClass().add("text-base");
        form.setStyle("-fx-alignment: center;");

        AnchorPane.setLeftAnchor(       form, 0.0      );
        AnchorPane.setRightAnchor(      form, 0.0      );
        AnchorPane.setBottomAnchor(     form, 50.0      );

        final AnchorPane p = new AnchorPane(header, form);
        p.setId("phase-update-container");

        h.getChildren().add(p);

        AnchorPane.setLeftAnchor(       h, 0.0      );
        AnchorPane.setRightAnchor(      h, 0.0      );
        AnchorPane.setTopAnchor(        h, 0.0      );
        AnchorPane.setBottomAnchor(     h, 0.0      );

        ViewSupervisor.createPopUp(h, 2000, false);

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

}
