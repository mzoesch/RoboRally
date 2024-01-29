package sep.view.scenecontrollers;

import sep.view.clientcontroller.   EGameState;
import sep.view.clientcontroller.   RemotePlayer;
import sep.view.clientcontroller.   EClientInformation;
import sep.view.clientcontroller.   GameInstance;
import sep.view.json.               ChatMsgModel;
import sep.view.json.game.          SelectedCardModel;
import sep.view.json.game.          SetStartingPointModel;
import sep.view.json.game.          DiscardSomeModel;
import sep.view.json.game.          PlayCardModel;
import sep.view.viewcontroller.     Tile;
import sep.view.viewcontroller.     ViewSupervisor;
import sep.view.viewcontroller.     TileModifier;
import sep.view.lib.                RCoordinate;
import sep.view.lib.                EGamePhase;
import sep.view.lib.                RLaserMask;
import sep.view.lib.                EAnimation;
import sep.view.lib.                RGearMask;
import sep.view.lib.                EFigure;
import sep.view.lib.                EModifier;
import sep.view.lib.                RCheckpointMask;
import sep.view.lib.                RShopAction;

import javafx.application.          Platform;
import java.util.                   ArrayList;
import java.util.                   Objects;
import java.util.                   Locale;
import javafx.scene.layout.         HBox;
import javafx.scene.layout.         VBox;
import javafx.scene.layout.         AnchorPane;
import javafx.scene.layout.         Priority;
import javafx.scene.layout.         Region;
import javafx.scene.layout.         Pane;
import javafx.scene.layout.         Background;
import javafx.scene.layout.         BackgroundFill;
import javafx.scene.layout.         CornerRadii;
import javafx.animation.            Animation;
import javafx.animation.            PauseTransition;
import javafx.animation.            Timeline;
import javafx.animation.            KeyFrame;
import javafx.animation.            KeyValue;
import javafx.animation.            FillTransition;
import javafx.animation.            Transition;
import javafx.animation.            Interpolator;
import javafx.fxml.                 FXML;
import javafx.beans.value.          ChangeListener;
import javafx.beans.value.          ObservableValue;
import javafx.scene.                Node;
import javafx.scene.                Cursor;
import javafx.scene.image.          ImageView;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import javafx.scene.shape.          Rectangle;
import javafx.event.                ActionEvent;
import javafx.scene.input.          KeyCode;
import javafx.util.                 Duration;
import javafx.scene.control.        OverrunStyle;
import javafx.scene.control.        Label;
import javafx.scene.control.        TextField;
import javafx.scene.control.        ScrollPane;
import javafx.scene.control.        Button;
import javafx.geometry.             Insets;
import javafx.geometry.             Pos;
import javafx.scene.paint.          Color;
import java.util.concurrent.atomic. AtomicReference;

public final class GameJFXController
{
    private static final Logger l = LogManager.getLogger(GameJFXController.class);

    private static final String     COLOR_HAMMER                = "#ff000033";
    private static final String     COLOR_TRUNDLE               = "#0078ff33";
    private static final String     COLOR_SQUASH                = "#ffc0cb33";
    private static final String     COLOR_X90                   = "#00ff0033";
    private static final String     COLOR_SPIN                  = "#00ffff33";
    private static final String     COLOR_TWONKY                = "#ffff0033";
    private static final String     COLOR_TWITCH                = "#aaaaaa33";
    private static final String     COLOR_RCARD_PREVIEW_BG      = "#707070e5";

    private static final int    SHOOTING_ROBOT_LASER_DURATION   = 1_000 ;
    private static final int    SHOOTING_WALL_LASER_DURATION    = 1_000 ;
    private static final int    CHAT_SCROLL_TIMEOUT             = 15    ;
    private static final int    GEAR_ANIMATION_DURATION         = 1_000 ;
    private static final int    BLINK_DURATION                  = 800   ;
    private static final int    RCARDS_TRANSLATION_DURATION     = 130   ;
    private static final int    QUICK_TIP_DURATION              = 60_000;

    @FXML private AnchorPane    memorySwapContainer;
    @FXML private AnchorPane    upgradeSlotContainer;
    @FXML private AnchorPane    gotRegisterContainer;
    @FXML private Label         programmingTimerLabel;
    @FXML private Label         UIHeaderPhaseLabel;
    @FXML private AnchorPane    masterContainer;
    @FXML private VBox          playerContainer;
    @FXML private ScrollPane    courseScrollPane;
    @FXML private AnchorPane    courseScrollPaneContent;
    @FXML private AnchorPane    registerContainer;
    @FXML private ScrollPane    chatScrollPane;
    @FXML private TextField     chatInputTextField;
    @FXML private Button        footerBtn;
    @FXML private AnchorPane    footerContainer;

    private static final int    RCARD_WIDTH                             = 50;
    private static final int    RCARD_HEIGHT                            = 88;
    private static final int    REBOOTED_RCARD_WIDTH                    = 100;
    private static final int    REBOOTED_RCARD_HEIGHT                   = 88;
    private static final int    RCARD_TRANSLATION_DIFF_X                = 10;
    private static final int    RCARD_PREVIEW_TRANSLATION_X             = 50;
    private static final int    RCARD_PREVIEW_TRANSLATION_X_CLEANUP     = 5;
    private static final int    RCARD_PREVIEW_TRANSLATION_X_ALPHA       = 2;
    private static final int    UPGRADE_PREVIEW_WIDTH                   = 15;
    private static final int    UPGRADE_PREVIEW_HEIGHT                  = 15;
    private static final int    MIN_ALLOW_ZOOM                          = 30;
    private static final int    MAX_ALLOW_ZOOM                          = 120;

    private VBox                chatContainer;
    private boolean             showServerInfo;

    private int                 tileDimensions;
    private static final int    RESIZE_AMOUNT   = 10;

    private boolean             bClickedOnTile;
    private int                 gotRegisterSlotClicked;
    private static final int    INVALID_GOT_REGISTER_SLOT   = -1;

    private boolean             bFooterCollapsed;

    private HBox                registerHBox;
    private HBox                gotRegisterHBox;
    private HBox                upgradeSlotHBox;
    private static final int    FOOTER_PEEK_HEIGHT  = 50;
    private static final int    NULL_FOOTER_HEIGHT  = 265;

    private int                         files;
    private int                         ranks;
    private Tile[][]                    tiles;
    private final ArrayList<RGearMask>  gears;
    private final ArrayList<AnchorPane> checkpoints;
    private double                      minXTranslation;
    private double                      maxXTranslation;
    private double                      centralXTranslation;

    private final ArrayList<RShopAction>    pendingShopActions;
    private static final int                PROGRAMMING_TIMER_DURATION      = 30_000;
    private Timeline                        programmingTimeline;

    /** The new three cards a player gets after playing the memory swap card. */
    private final ArrayList<String>         memorySwapCards;
    /** The three cards a player has to discard from his hand. */
    private final ArrayList<Integer>        memorySwapDiscardedCards;
    private HBox                            memorySwapHBox;

    public GameJFXController()
    {
        super();

        this.showServerInfo             = false;

        this.tileDimensions             = ViewSupervisor.TILE_DIMENSIONS;

        this.bClickedOnTile             = false;
        this.gotRegisterSlotClicked     = GameJFXController.INVALID_GOT_REGISTER_SLOT;

        this.bFooterCollapsed           = true;
        this.registerHBox               = null;

        this.files                      = 0;
        this.ranks                      = 0;
        this.tiles                      = null;
        this.gears                      = new ArrayList<RGearMask>();
        this.checkpoints                = new ArrayList<AnchorPane>();
        this.minXTranslation            = 0.0;
        this.maxXTranslation            = 0.0;
        this.centralXTranslation        = 0.0;

        this.pendingShopActions         = new ArrayList<RShopAction>();
        this.programmingTimeline        = null;

        this.memorySwapCards            = new ArrayList<String>();
        this.memorySwapDiscardedCards   = new ArrayList<Integer>();

        this.memorySwapHBox             = null;

        return;
    }

    @FXML
    private void initialize()
    {
        VBox.setVgrow(  this.chatScrollPane,            Priority.ALWAYS         );
        HBox.setMargin( this.programmingTimerLabel,     new Insets(0, 0, 0, 10) );
        HBox.setMargin( this.UIHeaderPhaseLabel,        new Insets(0, 10, 0, 0) );

        this.courseScrollPane.setFitToWidth(    true    );
        this.courseScrollPane.setFitToHeight(   true    );
        this.courseScrollPane.setHbarPolicy(    ScrollPane.ScrollBarPolicy.NEVER    );
        this.courseScrollPane.setVbarPolicy(    ScrollPane.ScrollBarPolicy.NEVER    );

        this.courseScrollPane.widthProperty().addListener((obs, val, t1) ->
        {
            /* TODO Only update translations not the whole course. */
            this.renderCourse();
            return;
        });

        this.chatInputTextField.lengthProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(final ObservableValue<? extends Number> observableValue, final Number number, final Number t1)
            {
                if (t1.intValue() > EGameState.MAX_CHAT_MESSAGE_LENGTH)
                {
                    chatInputTextField.setText(chatInputTextField.getText().substring(0, EGameState.MAX_CHAT_MESSAGE_LENGTH));
                }

                return;
            }
        });

        this.setupInputActions();

        this.renderView();

        this.onCenterCourse();

        this.chatContainer = new VBox();
        this.chatContainer.setId("chat-scroll-pane-inner");
        this.chatScrollPane.setContent(this.chatContainer);

        this.createQuickTip();

        synchronized (ViewSupervisor.getLoadGameSceneLock())
        {
            l.info("Finished loading game scene. Notifying post load tasks to continue.");
            ViewSupervisor.setGameScenePostLoaded();
            ViewSupervisor.getLoadGameSceneLock().notifyAll();
        }

        return;
    }

    private void createQuickTip()
    {
        if (EClientInformation.INSTANCE.getQuickTipCreated())
        {
            return;
        }
        EClientInformation.INSTANCE.setQuickTipCreated(true);

        final Label     l       = new Label("Quick Tips:");
        final Label     lQKey   = new Label("Q:");
        final Label     lQDesc  = new Label("To toggle the footer.");
        final Label     lZKey   = new Label("W/S:");
        final Label     lZDesc  = new Label("To zoom in and out.");
        final Label     lVKey   = new Label("MW:");
        final Label     lVDesc  = new Label("To move vertically.");
        final Label     lHKey   = new Label("SHIFT + MW:");
        final Label     lHDesc  = new Label("To move horizontally.");
        final Label     lCKey   = new Label("C:");
        final Label     lCDesc  = new Label("To center the course.");

        l.getStyleClass().add("text-base");
        l.setStyle("-fx-underline: true;");
        lQKey   .getStyleClass().add(   "text-base" );
        lQDesc  .getStyleClass().add(   "text-base" );
        lZKey   .getStyleClass().add(   "text-base" );
        lZDesc  .getStyleClass().add(   "text-base" );
        lVKey   .getStyleClass().add(   "text-base" );
        lVDesc  .getStyleClass().add(   "text-base" );
        lHKey   .getStyleClass().add(   "text-base" );
        lHDesc  .getStyleClass().add(   "text-base" );
        lCKey   .getStyleClass().add(   "text-base" );
        lCDesc  .getStyleClass().add(   "text-base" );

        final VBox      hKey        = new VBox( lQKey, lZKey, lVKey, lHKey, lCKey       );
        final VBox      hDesc       = new VBox( lQDesc, lZDesc, lVDesc, lHDesc, lCDesc  );
        final HBox      list        = new HBox( hKey, hDesc                             );
        final VBox      labels      = new VBox( l, list                                 );

        final Button        b       = new Button("X");
        final AnchorPane    p       = new AnchorPane(labels, b);

        list.setSpacing(20.0);
        b.getStyleClass().add("danger-btn-tiny");
        p.setId("quick-tip-popup");

        AnchorPane.setLeftAnchor(   labels, 0.0 );
        AnchorPane.setRightAnchor(  labels, 0.0 );
        AnchorPane.setTopAnchor(    labels, 0.0 );
        AnchorPane.setBottomAnchor( labels, 0.0 );

        AnchorPane.setRightAnchor(  b,      0.0 );
        AnchorPane.setTopAnchor(    b,      0.0 );

        AnchorPane.setLeftAnchor(   p,      10.0 );
        AnchorPane.setTopAnchor(    p,      10.0 );

        final AtomicReference<Thread> atom =  ViewSupervisor.createPopUpLater(p, GameJFXController.QUICK_TIP_DURATION, true);

        b.setOnAction(e ->
        {
            ViewSupervisor.getSceneController().destroyPopUp(p, false);

            if (atom.get() != null)
            {
                GameJFXController.l.debug("Interrupting quick tip thread.");
                atom.get().interrupt();
                return;
            }

            GameJFXController.l.fatal("Tried to interrupt quick tip thread but something went wrong. The thread reference was null.");
            GameInstance.kill(GameInstance.EXIT_FATAL);

            return;
        });

        return;
    }

    private void setupInputActions()
    {
        this.chatInputTextField.setOnKeyPressed(
        (keyEvent ->
        {
            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.ENTER)
            {
                this.onSubmitChatMsg();
            }

            return;
        })
        );

        this.masterContainer.setOnKeyPressed(e ->
        {
            switch (e.getCode())
            {

            /* Zoom in. */
            case W:
                if (this.tileDimensions + GameJFXController.RESIZE_AMOUNT > GameJFXController.MAX_ALLOW_ZOOM)
                {
                    l.debug("User tried to zoom in but the zoom level is already above the max pixel threshold [{}].", this.tileDimensions);
                    return;
                }

                this.tileDimensions += GameJFXController.RESIZE_AMOUNT;
                this.renderCourse();

                break;

            /* Zoom out. */
            case S:
                if (this.tileDimensions - GameJFXController.RESIZE_AMOUNT < GameJFXController.MIN_ALLOW_ZOOM)
                {
                    l.debug("User tried to zoom out but the zoom level is already below the min pixel threshold [{}].", this.tileDimensions);
                    return;
                }

                this.tileDimensions -= GameJFXController.RESIZE_AMOUNT;
                this.renderCourse();

                break;
            }

            return;
        });

        /* TODO We of course have to del these event listeners when we switch scenes. */
        ViewSupervisor.getSceneController().getMasterScene().setOnKeyPressed(
        (keyEvent ->
        {
            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.Q)
            {
                this.bFooterCollapsed = !this.bFooterCollapsed;
                this.renderHUDFooter();
                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.C)
            {
                this.onCenterCourse();
                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.DIGIT1 || Objects.requireNonNull(keyEvent.getCode()) == KeyCode.NUMPAD1)
            {
                if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
                {
                    this.onGotRegisterSlot1Clicked();
                }
                else
                {
                    this.onRegisterSlot1Clicked();
                }

                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.DIGIT2 || Objects.requireNonNull(keyEvent.getCode()) == KeyCode.NUMPAD2)
            {
                if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
                {
                    this.onGotRegisterSlot2Clicked();
                }
                else
                {
                    this.onRegisterSlot2Clicked();
                }

                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.DIGIT3 || Objects.requireNonNull(keyEvent.getCode()) == KeyCode.NUMPAD3)
            {
                if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
                {
                    this.onGotRegisterSlot3Clicked();
                }
                else
                {
                    this.onRegisterSlot3Clicked();
                }

                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.DIGIT4 || Objects.requireNonNull(keyEvent.getCode()) == KeyCode.NUMPAD4)
            {
                if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
                {
                    this.onGotRegisterSlot4Clicked();
                }
                else
                {
                    this.onRegisterSlot4Clicked();
                }

                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.DIGIT5 || Objects.requireNonNull(keyEvent.getCode()) == KeyCode.NUMPAD5)
            {
                if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
                {
                    this.onGotRegisterSlot5Clicked();
                }
                else
                {
                    this.onRegisterSlot5Clicked();
                }

                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.DIGIT6 || Objects.requireNonNull(keyEvent.getCode()) == KeyCode.NUMPAD6)
            {
                this.onGotRegisterSlot6Clicked();
                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.DIGIT7 || Objects.requireNonNull(keyEvent.getCode()) == KeyCode.NUMPAD7)
            {
                this.onGotRegisterSlot7Clicked();
                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.DIGIT8 || Objects.requireNonNull(keyEvent.getCode()) == KeyCode.NUMPAD8)
            {
                this.onGotRegisterSlot8Clicked();
                return;
            }

            if (Objects.requireNonNull(keyEvent.getCode()) == KeyCode.DIGIT9 || Objects.requireNonNull(keyEvent.getCode()) == KeyCode.NUMPAD9)
            {
                this.onGotRegisterSlot9Clicked();
                return;
            }

            return;
        })
        );


        return;
    }

    @FXML
    private void onFooterBtn(final ActionEvent actionEvent)
    {
        this.bFooterCollapsed = !this.bFooterCollapsed;
        this.renderHUDFooter();
        return;
    }

    // region Slot Action Methods

    // region Register Slot Action Methods

    private void onRegisterSlot1Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            return;
        }

        if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
        {
            if (EGameState.INSTANCE.getRegister(0) == null)
            {
                return;
            }

            EGameState.INSTANCE.undoRegister(0);
            this.renderHUDFooter();

            new SelectedCardModel(0, EGameState.INSTANCE.getRegister(0)).send();

            return;
        }

        if (EGameState.INSTANCE.getRegister(0) != null)
        {
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(this.gotRegisterSlotClicked) == null)
        {
            return;
        }

        if (Objects.equals(EGameState.INSTANCE.getGotRegister(this.gotRegisterSlotClicked), "Again")){
            l.info("Player tried setting AGAIN-card in first register slot. Refused.");
            ViewSupervisor.handleChatInfo("You cant place a AGAIN-programmingcard in Register 1. Try another register or card.");
            return;
        }

        EGameState.INSTANCE.setRegister(0, this.gotRegisterSlotClicked);

        this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
        this.renderHUDFooter();

        new SelectedCardModel(0, EGameState.INSTANCE.getRegister(0)).send();

        return;
    }

    private void onRegisterSlot2Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            return;
        }

        if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
        {
            if (EGameState.INSTANCE.getRegister(1) == null)
            {
                return;
            }

            EGameState.INSTANCE.undoRegister(1);
            this.renderHUDFooter();

            new SelectedCardModel(1, EGameState.INSTANCE.getRegister(1)).send();

            return;
        }

        if (EGameState.INSTANCE.getRegister(1) != null)
        {
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(this.gotRegisterSlotClicked) == null)
        {
            return;
        }

        EGameState.INSTANCE.setRegister(1, this.gotRegisterSlotClicked);

        this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
        this.renderHUDFooter();

        new SelectedCardModel(1, EGameState.INSTANCE.getRegister(1)).send();

        return;
    }

    private void onRegisterSlot3Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            return;
        }

        if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
        {
            if (EGameState.INSTANCE.getRegister(2) == null)
            {
                return;
            }

            EGameState.INSTANCE.undoRegister(2);
            this.renderHUDFooter();

            new SelectedCardModel(2, EGameState.INSTANCE.getRegister(2)).send();

            return;
        }

        if (EGameState.INSTANCE.getRegister(2) != null)
        {
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(this.gotRegisterSlotClicked) == null)
        {
            return;
        }

        EGameState.INSTANCE.setRegister(2, this.gotRegisterSlotClicked);

        this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
        this.renderHUDFooter();

        new SelectedCardModel(2, EGameState.INSTANCE.getRegister(2)).send();

        return;
    }

    private void onRegisterSlot4Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            return;
        }

        if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
        {
            if (EGameState.INSTANCE.getRegister(3) == null)
            {
                return;
            }

            EGameState.INSTANCE.undoRegister(3);
            this.renderHUDFooter();

            new SelectedCardModel(3, EGameState.INSTANCE.getRegister(3)).send();

            return;
        }

        if (EGameState.INSTANCE.getRegister(3) != null)
        {
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(this.gotRegisterSlotClicked) == null)
        {
            return;
        }

        EGameState.INSTANCE.setRegister(3, this.gotRegisterSlotClicked);

        this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
        this.renderHUDFooter();

        new SelectedCardModel(3, EGameState.INSTANCE.getRegister(3)).send();

        return;
    }

    private void onRegisterSlot5Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            return;
        }

        if (this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT)
        {
            if (EGameState.INSTANCE.getRegister(4) == null)
            {
                return;
            }

            EGameState.INSTANCE.undoRegister(4);
            this.renderHUDFooter();

            new SelectedCardModel(4, EGameState.INSTANCE.getRegister(4)).send();

            return;
        }

        if (EGameState.INSTANCE.getRegister(4) != null)
        {
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(this.gotRegisterSlotClicked) == null)
        {
            return;
        }

        EGameState.INSTANCE.setRegister(4, this.gotRegisterSlotClicked);

        this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
        this.renderHUDFooter();

        new SelectedCardModel(4, EGameState.INSTANCE.getRegister(4)).send();

        return;
    }

    // endregion Register Slot Action Methods

    // region Got Register Slot Action Methods

    private void onGotRegisterSlot1Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            if (this.memorySwapDiscardedCards.size() >= 3)
            {
                l.debug("User tried to change one of their programming memory swap discarded cards but they are already full: {}.", this.memorySwapDiscardedCards);
                return;
            }

            this.memorySwapDiscardedCards.add(0);
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            return;
        }

        if (this.gotRegisterSlotClicked == 0)
        {
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(0) == null)
        {
            return;
        }

        this.gotRegisterSlotClicked = 0;
        this.renderHUDFooter();

        return;
    }

    private void onGotRegisterSlot2Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            if (this.memorySwapDiscardedCards.size() >= 3)
            {
                l.debug("User tried to change one of their programming memory swap discarded cards but they are already full: {}.", this.memorySwapDiscardedCards);
                return;
            }

            this.memorySwapDiscardedCards.add(1);
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            return;
        }

        if (this.gotRegisterSlotClicked == 1)
        {
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;

            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(1) == null)
        {
            return;
        }

        this.gotRegisterSlotClicked = 1;
        this.renderHUDFooter();

        return;
    }

    private void onGotRegisterSlot3Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            if (this.memorySwapDiscardedCards.size() >= 3)
            {
                l.debug("User tried to change one of their programming memory swap discarded cards but they are already full: {}.", this.memorySwapDiscardedCards);
                return;
            }

            this.memorySwapDiscardedCards.add(2);
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            return;
        }

        if (this.gotRegisterSlotClicked == 2)
        {
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(2) == null)
        {
            return;
        }

        this.gotRegisterSlotClicked = 2;
        this.renderHUDFooter();

        return;
    }

    private void onGotRegisterSlot4Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            if (this.memorySwapDiscardedCards.size() >= 3)
            {
                l.debug("User tried to change one of their programming memory swap discarded cards but they are already full: {}.", this.memorySwapDiscardedCards);
                return;
            }

            this.memorySwapDiscardedCards.add(3);
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            return;
        }

        if (this.gotRegisterSlotClicked == 3)
        {
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(3) == null)
        {
            return;
        }

        this.gotRegisterSlotClicked = 3;
        this.renderHUDFooter();

        return;
    }

    private void onGotRegisterSlot5Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            if (this.memorySwapDiscardedCards.size() >= 3)
            {
                l.debug("User tried to change one of their programming memory swap discarded cards but they are already full: {}.", this.memorySwapDiscardedCards);
                return;
            }

            this.memorySwapDiscardedCards.add(4);
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            return;
        }

        if (this.gotRegisterSlotClicked == 4)
        {
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(4) == null)
        {
            return;
        }

        this.gotRegisterSlotClicked = 4;
        this.renderHUDFooter();

        return;
    }

    private void onGotRegisterSlot6Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            if (this.memorySwapDiscardedCards.size() >= 3)
            {
                l.debug("User tried to change one of their programming memory swap discarded cards but they are already full: {}.", this.memorySwapDiscardedCards);
                return;
            }

            this.memorySwapDiscardedCards.add(5);
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            return;
        }

        if (this.gotRegisterSlotClicked == 5)
        {
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(5) == null)
        {
            return;
        }

        this.gotRegisterSlotClicked = 5;
        this.renderHUDFooter();

        return;
    }

    private void onGotRegisterSlot7Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            if (this.memorySwapDiscardedCards.size() >= 3)
            {
                l.debug("User tried to change one of their programming memory swap discarded cards but they are already full: {}.", this.memorySwapDiscardedCards);
                return;
            }

            this.memorySwapDiscardedCards.add(6);
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            return;
        }

        if (this.gotRegisterSlotClicked == 6)
        {
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(6) == null)
        {
            return;
        }

        this.gotRegisterSlotClicked = 6;
        this.renderHUDFooter();

        return;
    }

    private void onGotRegisterSlot8Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            if (this.memorySwapDiscardedCards.size() >= 3)
            {
                l.debug("User tried to change one of their programming memory swap discarded cards but they are already full: {}.", this.memorySwapDiscardedCards);
                return;
            }

            this.memorySwapDiscardedCards.add(7);
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            return;
        }

        if (this.gotRegisterSlotClicked == 7)
        {
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(7) == null)
        {
            return;
        }

        this.gotRegisterSlotClicked = 7;
        this.renderHUDFooter();

        return;
    }

    private void onGotRegisterSlot9Clicked()
    {
        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
        {
            return;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            if (this.memorySwapDiscardedCards.size() >= 3)
            {
                l.debug("User tried to change one of their programming memory swap discarded cards but they are already full: {}.", this.memorySwapDiscardedCards);
                return;
            }

            this.memorySwapDiscardedCards.add(8);
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.areRegistersFull())
        {
            l.debug("User tried to change one of their programming register slots, but they are already finalized.");
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            return;
        }

        if (this.gotRegisterSlotClicked == 8)
        {
            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            this.renderHUDFooter();
            return;
        }

        if (EGameState.INSTANCE.getGotRegister(8) == null)
        {
            return;
        }

        this.gotRegisterSlotClicked = 8;
        this.renderHUDFooter();

        return;
    }

    // endregion Slot Action Methods

    // endregion Got Register Slot Action Methods

    // region Chat

    private void scrollChatToEnd()
    {
        this.chatScrollPane.setVvalue(1.0);
        return;
    }

    /** Kinda sketchy. But is there a better way? */
    private void scrollChatToEndLater()
    {
        final PauseTransition p = new PauseTransition(Duration.millis(GameJFXController.CHAT_SCROLL_TIMEOUT));
        p.setOnFinished(e -> this.scrollChatToEnd());
        p.play();

        return;
    }

    private void onSubmitChatMsg()
    {
        final String token = this.getChatMsg();
        this.chatInputTextField.clear();

        if (this.isChatMsgACommand(token))
        {
            if (this.getChatCommand(token).isEmpty() || this.getChatCommand(token).isBlank())
            {
                this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "Type /h for help on commands.", false);
                return;
            }

            if (this.getChatCommand(token).equals("w"))
            {
                if (!token.contains("\""))
                {
                    this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }
                final int idxBSBegin = token.indexOf("\"");
                final String sub = token.substring(idxBSBegin + 1);
                if (!sub.contains("\""))
                {
                    this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }
                final int idxBSEnd = sub.indexOf("\"");

                final String targetPlayer = token.substring(idxBSBegin + 1, idxBSBegin + idxBSEnd + 1);
                if (targetPlayer.isEmpty() || targetPlayer.isBlank())
                {
                    this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }

                final String msgToWhisper;
                try
                {
                    msgToWhisper = token.substring(idxBSBegin + idxBSEnd + 3);
                }
                catch (final IndexOutOfBoundsException e)
                {
                    this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "Invalid message.", false);
                    return;
                }
                if (msgToWhisper.isEmpty() || msgToWhisper.isBlank())
                {
                    return;
                }

                final RemotePlayer target = EGameState.INSTANCE.getRemotePlayerByPlayerName(targetPlayer);
                if (target == null)
                {
                    this.addChatMsgToView(ChatMsgModel.CLIENT_ID, String.format("Player %s not found.", targetPlayer), false);
                    return;
                }

                new ChatMsgModel(msgToWhisper, target.getPlayerID()).send();
                if (EClientInformation.INSTANCE.getPlayerID() != target.getPlayerID())
                {
                    this.addChatMsgToView(EClientInformation.INSTANCE.getPlayerID(), msgToWhisper, true);
                }

                l.debug("Whispering to {}.", targetPlayer);

                return;
            }

            if (this.getChatCommand(token).equals("h"))
            {
                this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "Commands:", false);
                this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "/h - Show this help.", false);
                this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "/w [\"player name\"] [msg] - Whisper to a player.", false);
                this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "/hide - Hide ServerInfos", false);
                this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "/show - Show ServerInfos after again hide-command.", false);

                return;
            }

            if (this.getChatCommand(token).equals("hide"))
            {
                final ArrayList<Node> serverInfo = new ArrayList<>();
                for (final Node node : chatContainer.getChildren())
                {
                    if (node instanceof final Label label)
                    {
                        if (label.getStyleClass().contains("lobby-msg-server"))
                        {
                            node.setVisible(false);
                            serverInfo.add(node);
                            l.debug("Line added to serverInfo.");
                            continue;
                        }
                    }

                    continue;
                }

                this.chatContainer.getChildren().removeAll(serverInfo);
                this.addChatMsgToView(ChatMsgModel.SERVER_ID, "ServerInfo is now hidden.", false);
                this.showServerInfo = false;

                return;
            }

            if (this.getChatCommand(token).equals("show"))
            {
                if (this.showServerInfo)
                {
                    this.addChatMsgToView(ChatMsgModel.SERVER_ID, "ServerInfo is already shown.", false);
                }
                else
                {
                    this.showServerInfo = true;
                    this.addChatMsgToView(ChatMsgModel.SERVER_ID, "ServerInfo is now shown.", false);
                }

                return;
            }

            this.addChatMsgToView(ChatMsgModel.CLIENT_ID, String.format("Unknown command: %s", this.getChatCommand(token)), false);

            return;
        }

        if (!this.isChatMsgValid(token))
        {
            return;
        }

        new ChatMsgModel(token, ChatMsgModel.CHAT_MSG_BROADCAST).send();

        return;
    }

    private void addChatMsgToView(final int caller, final String msg, final boolean bIsPrivate)
    {
        if (caller == ChatMsgModel.SERVER_ID)
        {
            if (this.showServerInfo)
            {
                final Label l = new Label(String.format("[%s] %s", ChatMsgModel.SERVER_NAME, msg));
                l.getStyleClass().add("lobby-msg-server");
                l.setWrapText(true);
                this.chatContainer.getChildren().add(l);

                this.scrollChatToEndLater();
            }

            return;
        }

        if (caller == ChatMsgModel.CLIENT_ID)
        {
            final Label l = new Label(String.format("[%s] %s", ChatMsgModel.CLIENT_NAME, msg));
            l.getStyleClass().add("lobby-msg-client");
            l.setWrapText(true);
            this.chatContainer.getChildren().add(l);

            this.scrollChatToEndLater();

            return;
        }

        final Label l = new Label(String.format("<%s>%s %s", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(caller)).getPlayerName(), bIsPrivate ? " whispers: " : "", msg));
        l.getStyleClass().add(bIsPrivate ? "lobby-msg-whisper" : "lobby-msg");
        l.setWrapText(true);
        this.chatContainer.getChildren().add(l);

        this.scrollChatToEndLater();

        return;
    }

    // endregion Chat

    // region Rendering

    // region Head Up Display

    // region MISC

    private void renderMemorySwapDialog()
    {
        if (this.memorySwapHBox != null)
        {
            ViewSupervisor.getSceneController().getRenderTarget().getChildren().remove(this.memorySwapHBox);
            this.memorySwapHBox = null;
        }

        final Label l = new Label("Select three cards to discard.");
        l.getStyleClass().add("text-xl");

        final HBox cards = new HBox();
        cards.setSpacing(10.0);

        for (int i = 0; i < 3; ++i)
        {
            final int gotIdx;
            if (this.memorySwapDiscardedCards.size() > i)
            {
                gotIdx = this.memorySwapDiscardedCards.get(i);
            }
            else
            {
                gotIdx = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            }

            final ImageView iv = new ImageView();
            iv.setFitWidth(ViewSupervisor.REGISTER_SLOT_WIDTH);
            iv.setFitHeight(ViewSupervisor.REGISTER_SLOT_HEIGHT);
            iv.setImage(gotIdx == GameJFXController.INVALID_GOT_REGISTER_SLOT ? TileModifier.loadCachedImage("EmptyRegisterSlot") : TileModifier.loadCachedImage(EGameState.INSTANCE.getGotRegister(gotIdx)));
            iv.setOnMouseClicked(e ->
            {
                this.memorySwapDiscardedCards.remove( (Integer) gotIdx );
                this.renderHUDFooter();
                return;
            });

            if (gotIdx != GameJFXController.INVALID_GOT_REGISTER_SLOT)
            {
                iv.setCursor(Cursor.HAND);
            }

            cards.getChildren().add(iv);

            continue;
        }

        final HBox cardsWrapper = new HBox(GameJFXController.createHSpacer(), cards, GameJFXController.createHSpacer());

        final VBox v = new VBox(l, cardsWrapper);
        v.setSpacing(30.0);
        v.setAlignment(Pos.CENTER);

        final Button b = new Button("Confirm");
        b.getStyleClass().add("secondary-btn");

        if (this.memorySwapDiscardedCards.size() < 3)
        {
            b.setDisable(true);
        }

        b.setOnAction(e ->
        {
            final ArrayList<String> discardedCards = new ArrayList<String>();

            for (final int idx : this.memorySwapDiscardedCards)
            {
                discardedCards.add(EGameState.INSTANCE.getGotRegister(idx));
                continue;
            }

            GameJFXController.l.info("MemorySwapDialog: User wants to discard: {} -> {}.", this.memorySwapDiscardedCards, discardedCards);

            new DiscardSomeModel(discardedCards.toArray(new String[0])).send();

            for (final int gotIdx : this.memorySwapDiscardedCards)
            {
                EGameState.INSTANCE.overrideGotRegister(gotIdx, this.memorySwapCards.remove(0));
                continue;
            }

            if (this.memorySwapCards.isEmpty())
            {
                ViewSupervisor.getSceneController().getRenderTarget().getChildren().remove(this.memorySwapHBox);
                this.memorySwapHBox = null;
                this.memorySwapDiscardedCards.clear();
                this.memorySwapContainer.getChildren().clear();

                GameJFXController.l.debug("MemorySwapDialog: Successfully sent server discard request.");

                this.renderHUDFooter();

                return;
            }

            GameJFXController.l.fatal("MemorySwapDialog: MemorySwapCards is not empty after discarding: {}.", this.memorySwapCards);
            GameInstance.kill(GameInstance.EXIT_FATAL);

            return;
        });

        AnchorPane.setLeftAnchor(   v, 0.0  );
        AnchorPane.setRightAnchor(  v, 0.0  );
        AnchorPane.setTopAnchor(    v, 25.0 );

        AnchorPane.setRightAnchor(  b, 25.0 );
        AnchorPane.setBottomAnchor( b, 25.0 );

        final AnchorPane p = new AnchorPane(v, b);
        p.setId("memory-swap-dialog-container");

        this.memorySwapHBox = new HBox(p);
        this.memorySwapHBox.setAlignment(Pos.CENTER);

        AnchorPane.setLeftAnchor(   this.memorySwapHBox, 0.0 );
        AnchorPane.setRightAnchor(  this.memorySwapHBox, 0.0 );
        AnchorPane.setTopAnchor(    this.memorySwapHBox, 20.0 );

        ViewSupervisor.createPopUp(this.memorySwapHBox);

        return;
    }

    // endregion MISC

    // region HUD Side Panel

    private void renderPhaseTitle()
    {
        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.INVALID)
        {
            this.UIHeaderPhaseLabel.setText("Waiting for server.");
            return;
        }

        this.UIHeaderPhaseLabel.setText(EGameState.PHASE_NAMES[EGameState.INSTANCE.getCurrentPhase().i]);
        return;
    }

    private void renderInfoTitle()
    {
        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.INVALID)
        {
            this.programmingTimerLabel.setText("");
            this.programmingTimerLabel.setStyle("");
            return;
        }

        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.REGISTRATION)
        {
            this.programmingTimerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffffff; -fx-alignment: center-left;");

            if (EGameState.INSTANCE.getCurrentPlayer().getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
            {
                this.programmingTimerLabel.setText("Set Your Starting Position.");
                return;
            }

            this.programmingTimerLabel.setText(String.format("Waiting for %s ...", EGameState.INSTANCE.getCurrentPlayer().getPlayerName()));

            return;
        }

        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.UPGRADE)
        {
            if (EGameState.INSTANCE.getCurrentPlayer() == null)
            {
                this.programmingTimerLabel.setText("Waiting for server.");
                this.programmingTimerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffffff; -fx-alignment: center-left;");
                return;
            }

            if (EGameState.INSTANCE.getCurrentPlayer().getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
            {
                this.programmingTimerLabel.setText("");
                this.programmingTimerLabel.setStyle("");
                return;
            }

            this.programmingTimerLabel.setText(String.format("Waiting for %s ...", EGameState.INSTANCE.getCurrentPlayer().getPlayerName()));
            this.programmingTimerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffffff; -fx-alignment: center-left;");

            return;
        }

        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING)
        {
            if (Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).hasSelectionFinished())
            {
                this.programmingTimerLabel.setText("Waiting for others ...");
                this.programmingTimerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffffff; -fx-alignment: center-left;");

                if (this.programmingTimeline != null)
                {
                    this.programmingTimeline.stop();
                    this.programmingTimeline = null;
                }

                return;
            }

            if (this.programmingTimeline == null && EGameState.INSTANCE.isProgrammingTimerRunning())
            {
                final long startTime = System.currentTimeMillis();
                this.programmingTimerLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #ffffffff; -fx-alignment: center-left;");

                this.programmingTimeline = new Timeline(new KeyFrame(Duration.millis(100), e ->
                {
                    final long timeLeft = GameJFXController.PROGRAMMING_TIMER_DURATION - (System.currentTimeMillis() - startTime);
                    this.programmingTimerLabel.setText(String.format(Locale.US, "%.2fs", ( (double) timeLeft ) / 1_000));
                    return;
                }
                ));

                this.programmingTimeline.setCycleCount(Animation.INDEFINITE);
                this.programmingTimeline.play();

                return;
            }

            return;
        }

        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.ACTIVATION)
        {
            this.programmingTimerLabel.setText("");
            this.programmingTimerLabel.setStyle("");

            if (programmingTimeline != null)
            {
                this.programmingTimeline.stop();
                this.programmingTimeline = null;
            }

            return;
        }


        return;
    }

    // endregion HUD Side Panel

    // region HUD Footer

    private ImageView getEmptyRegisterSlot(final int width, final int height)
    {
        final ImageView iv = new ImageView();
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        iv.getStyleClass().add("register-slot");
        iv.setImage(TileModifier.loadCachedImage("EmptyRegisterSlot"));
        return iv;
    }

    private ImageView getCardRegisterSlot(final int width, final int height, final String cardName)
    {
        if (cardName == null)
        {
            return this.getEmptyRegisterSlot(width, height);
        }

        final ImageView iv = new ImageView();
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        iv.setImage(TileModifier.loadCachedImage(cardName));

        return iv;
    }

    private ImageView getUpgradeCardSlot(final int idx)
    {
        if (EGameState.INSTANCE.getBoughtUpgradeCard(idx) == null)
        {
            return this.getEmptyRegisterSlot(ViewSupervisor.UPGRADE_SLOT_WIDTH, ViewSupervisor.UPGRADE_SLOT_HEIGHT);
        }

        final ImageView iv = new ImageView();
        iv.setFitWidth(ViewSupervisor.UPGRADE_SLOT_WIDTH);
        iv.setFitHeight(ViewSupervisor.UPGRADE_SLOT_HEIGHT);
        iv.setImage(TileModifier.loadCachedImage(EGameState.INSTANCE.getBoughtUpgradeCard(idx)));

        /* Kinda sketchy. But we have a 1px border. */
        iv.setTranslateX(1);
        iv.setTranslateY(1);

        return iv;
    }

    /** @param idx Index of the register slot. */
    private void addRegisterSlot(final int idx, final boolean bIsGotRegister, final Pane p, final int w, final int h)
    {
        final ImageView     iv  = this.getCardRegisterSlot(w, h, bIsGotRegister ? EGameState.INSTANCE.getGotRegister(idx) : EGameState.INSTANCE.getRegister(idx));
        final AnchorPane    ap  = new AnchorPane();

        if (bIsGotRegister && this.memorySwapDiscardedCards.contains(idx))
        {
            iv.setImage(TileModifier.loadCachedImage("EmptyRegisterSlot"));
            ap.getStyleClass().add("register-slot-disabled");
        }
        else if (bIsGotRegister)
        {
            ap.getStyleClass().add(
               EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
            ?! EGameState.INSTANCE.areRegistersFull()
            ?  this.gotRegisterSlotClicked != idx
            ?  EGameState.INSTANCE.getGotRegister(idx) == null
            ?  "register-slot-disabled"
            :  "register-slot"
            :  "register-slot-active"
            :  "register-slot-disabled"
            :  "register-slot-disabled"
            )
            ;

            iv.setTranslateX(1);
            iv.setTranslateY(1);
        }
        else
        {
            ap.getStyleClass().add(
               EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
            ?! EGameState.INSTANCE.areRegistersFull()
            ?  this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
            ?  EGameState.INSTANCE.getRegister(idx) == null
            ?  "register-slot-disabled"
            :  "register-slot"
            :  EGameState.INSTANCE.getRegister(idx) == null
            ?  "register-slot-available"
            :  "register-slot-disabled"
            :  "register-slot-disabled"
            :  "register-slot-disabled"
            )
            ;

            /* We may not place an Again card in the first register. */
            if (this.gotRegisterSlotClicked != GameJFXController.INVALID_GOT_REGISTER_SLOT && idx == 0 && Objects.equals(EGameState.INSTANCE.getGotRegister(this.gotRegisterSlotClicked), "Again"))
            {
                ap.getStyleClass().clear();
                ap.getStyleClass().add("register-slot-disabled");
            }
        }

        ap.setOnMouseClicked(e
        ->
        {
            if (bIsGotRegister && this.memorySwapDiscardedCards.contains(idx))
            {
                return;
            }

            if (bIsGotRegister)
            {
                switch (idx)
                {
                case 0x0:
                    this.onGotRegisterSlot1Clicked();
                    return;
                case 0x1:
                    this.onGotRegisterSlot2Clicked();
                    return;
                case 0x2:
                    this.onGotRegisterSlot3Clicked();
                    return;
                case 0x3:
                    this.onGotRegisterSlot4Clicked();
                    return;
                case 0x4:
                    this.onGotRegisterSlot5Clicked();
                    return;
                case 0x5:
                    this.onGotRegisterSlot6Clicked();
                    return;
                case 0x6:
                    this.onGotRegisterSlot7Clicked();
                    return;
                case 0x7:
                    this.onGotRegisterSlot8Clicked();
                    return;
                case 0x8:
                    this.onGotRegisterSlot9Clicked();
                    return;
                default:
                    l.error("Invalid got register slot index: {}", idx);
                    break;
                }

                return;
            }

            switch (idx)
            {
            case 0b0:
                this.onRegisterSlot1Clicked();
                return;
            case 0b1:
                this.onRegisterSlot2Clicked();
                return;
            case 0b10:
                this.onRegisterSlot3Clicked();
                return;
            case 0b11:
                this.onRegisterSlot4Clicked();
                return;
            case 0b100:
                this.onRegisterSlot5Clicked();
                return;
            default:
                l.error("Invalid register slot index: {}", idx);
                break;
            }

            return;
        }
        )
        ;

        ap.getChildren().clear();
        ap.getChildren().add(iv);

        p.getChildren().add(ap);

        return;
    }

    private void applyPlayableUpgradeEffect(final AnchorPane target)
    {
        target.setStyle("-fx-cursor: hand; -fx-background-color: rgba(0, 0, 0, 0);");

        final AnchorPane effectTarget = new AnchorPane();

        final Animation anim = new Transition()
        {
            {
                this.setCycleDuration(Duration.millis(800));
                this.setInterpolator(Interpolator.EASE_OUT);
            }

            @Override
            protected void interpolate(double frac)
            {
                Color vColor = new Color(0.5, 0.5, 0.5, 1 - frac);
                effectTarget.setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));

                return;
            }
        };

        effectTarget.setTranslateX(1);
        effectTarget.setTranslateY(1);

        effectTarget.setPrefWidth(  ViewSupervisor.UPGRADE_SLOT_WIDTH   );
        effectTarget.setPrefHeight( ViewSupervisor.UPGRADE_SLOT_HEIGHT  );

        anim.setCycleCount(Animation.INDEFINITE);
        anim.setAutoReverse(true);
        anim.play();

        target.getChildren().add(0, effectTarget);

        return;
    }

    private void addUpgradeSlot(final int idx, final Pane p)
    {
        // We have to be a little bit cheeky here because the idx-es are not in order.
        //      0 1             0 3
        //      2 3     -->     1 4
        //      4 5             2 5

        /* We can do this because Integers are always floored. */
        final int realIdx = idx % 2 == 0 ? idx / 2 : idx / 2 + 3;

        final ImageView     iv  = this.getUpgradeCardSlot(realIdx);
        final AnchorPane    ap  = new AnchorPane();

        ap.getStyleClass().clear();

        ap.getStyleClass().add("register-slot-disabled");
        ap.setPrefWidth(ViewSupervisor.UPGRADE_SLOT_WIDTH);
        ap.setPrefHeight(ViewSupervisor.UPGRADE_SLOT_HEIGHT);

        ap.getChildren().clear();
        ap.getChildren().add(iv);

        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING && !Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).hasSelectionFinished() && Objects.equals(EGameState.INSTANCE.getBoughtUpgradeCard(idx), "MemorySwap") && !EGameState.INSTANCE.isMemorySwapPlayed())
        {
            this.applyPlayableUpgradeEffect(ap);

            ap.setOnMouseClicked(e ->
            {
                l.debug("User clicked on MemorySwap upgrade card.");


                EGameState.INSTANCE.setMemorySwapPlayed(true);

                ap.setOnMouseClicked(null);
                ap.setStyle("");
                ap.getChildren().remove(0, 1);

                new PlayCardModel(EGameState.INSTANCE.getBoughtUpgradeCard(idx)).send();

                return;
            });
        }

        p.getChildren().add(ap);

        return;
    }

    private void renderRegisterSlots()
    {
        if (this.registerHBox == null)
        {
            this.registerHBox = new HBox();
            this.registerHBox.setId("register-hbox");
        }
        else
        {
            this.registerHBox.getChildren().clear();
        }

        if (this.gotRegisterHBox == null)
        {
            this.gotRegisterHBox = new HBox();
            this.gotRegisterHBox.setId("got-register-hbox");
        }
        else
        {
            this.gotRegisterHBox.getChildren().clear();
        }

        if (this.upgradeSlotHBox == null)
        {
            this.upgradeSlotHBox = new HBox();
            this.upgradeSlotHBox.setId("upgrade-slot-hbox");
        }
        else
        {
            this.upgradeSlotHBox.getChildren().clear();
        }

        this.registerContainer.getChildren().clear();
        this.registerContainer.getChildren().add(this.registerHBox);
        for (int i = 0; i < 5; ++i)
        {
            this.addRegisterSlot(i, false, this.registerHBox, ViewSupervisor.REGISTER_SLOT_WIDTH, ViewSupervisor.REGISTER_SLOT_HEIGHT);
            continue;
        }

        this.gotRegisterContainer.getChildren().clear();
        this.gotRegisterContainer.getChildren().add(this.gotRegisterHBox);
        this.gotRegisterHBox.getChildren().add(new VBox());
        for (int i = 0; i < 9; ++i)
        {
            if (i % 3 == 0)
            {
                ( (Pane) this.gotRegisterHBox.getChildren().get(this.gotRegisterHBox.getChildren().size() - 1) ).getChildren().add(new HBox());
            }

            this.addRegisterSlot
            (
              i
            , true
            , (Pane) ( (Pane) this.gotRegisterHBox.getChildren().get(this.gotRegisterHBox.getChildren().size() - 1) ).getChildren().get( ( (Pane) this.gotRegisterHBox.getChildren().get(this.gotRegisterHBox.getChildren().size() - 1) ).getChildren().size() - 1)
            , ViewSupervisor.GOT_REGISTER_SLOT_WIDTH
            , ViewSupervisor.GOT_REGISTER_SLOT_HEIGHT
            )
            ;

            continue;
        }

        if (!this.memorySwapCards.isEmpty())
        {
            this.memorySwapContainer.getChildren().clear();

            final ImageView arrow = new ImageView();
            arrow.setFitWidth(  35.0  );
            arrow.setFitHeight( 35.0  );
            /* Extremely sketchy. Please center and not translate. */
            arrow.setTranslateY( 75.0);
            arrow.setImage(TileModifier.loadCachedImage("MemorySwapPreview"));
            final VBox v = new VBox();
            v.setAlignment(Pos.CENTER);

            for (final String memorySwapCard : this.memorySwapCards)
            {
                final ImageView iv = new ImageView();
                iv.setFitWidth(ViewSupervisor.GOT_REGISTER_SLOT_WIDTH);
                iv.setFitHeight(ViewSupervisor.GOT_REGISTER_SLOT_HEIGHT);
                iv.setImage(TileModifier.loadCachedImage(memorySwapCard));

                iv.setTranslateX(1);
                iv.setTranslateY(1);

                final AnchorPane ap = new AnchorPane(iv);
                ap.getStyleClass().add("register-slot");

                v.getChildren().add(ap);

                continue;
            }

            final HBox h = new HBox(arrow, v);
            h.setSpacing(8);
            this.memorySwapContainer.getChildren().add(h);
        }

        this.upgradeSlotContainer.getChildren().clear();
        this.upgradeSlotContainer.getChildren().add(this.upgradeSlotHBox);
        this.upgradeSlotHBox.getChildren().add(new VBox());
        for (int i = 0; i < 6; ++i)
        {
            if (i % 2 == 0)
            {
                ( (Pane) this.upgradeSlotHBox.getChildren().get(this.upgradeSlotHBox.getChildren().size() - 1) ).getChildren().add(new HBox());
            }

            this.addUpgradeSlot(i, (Pane) ( (Pane) this.upgradeSlotHBox.getChildren().get(this.upgradeSlotHBox.getChildren().size() - 1) ).getChildren().get( ( (Pane) this.upgradeSlotHBox.getChildren().get(this.upgradeSlotHBox.getChildren().size() - 1) ).getChildren().size() - 1) );

            continue;
        }

        return;
    }

    private void setFooterBtnText()
    {
        this.footerBtn.setText(this.bFooterCollapsed ? "Show" : "Collapse");
        return;
    }

    private void translateFooter()
    {
        /* TODO Maybe with a timeline? */
        this.footerContainer.setTranslateY(
              this.bFooterCollapsed
            ? this.footerContainer.getHeight() < 10
            ? GameJFXController.NULL_FOOTER_HEIGHT - GameJFXController.FOOTER_PEEK_HEIGHT
            : this.footerContainer.getHeight() - GameJFXController.FOOTER_PEEK_HEIGHT
            : 0
        );

        return;
    }

    // endregion HUD Footer

    /**
     * Updates every dependency of the side panel.
     * No re-renders must be done after this method.
     */
    private void renderHUDHeader()
    {
        this.renderPhaseTitle();
        this.renderInfoTitle();
        return;
    }

    /**
     * Updates every dependency of the footer.
     * No re-renders must be done after this method.
     */
    private void renderHUDFooter()
    {
        this.renderRegisterSlots();
        this.setFooterBtnText();
        this.translateFooter();

        return;
    }

    /**
     * Displays every player in the lobby with some stats.
     * No re-renders must be done after this method.
     */
    private void renderPlayerInformationArea()
    {
        this.renderHUDHeader();

        this.playerContainer.getChildren().clear();

        for (int i = 0; i < EGameState.INSTANCE.getRemotePlayers().length; ++i)
        {
            final RemotePlayer rp = EGameState.INSTANCE.getRemotePlayers()[i];

            if (i % 2 == 0)
            {
                this.playerContainer.getChildren().add(new HBox());
            }

            if (i % 2 == 1)
            {
                ( (Pane) this.playerContainer.getChildren().get(this.playerContainer.getChildren().size() - 1)).getChildren().add(GameJFXController.createHSpacer());
            }

            final Label figureName = new Label(rp.getFigure().toString());
            figureName.getStyleClass().add("text-sm");

            final Label ctrlName = new Label(rp.getPlayerName());
            ctrlName.getStyleClass().add("text-sm");
            ctrlName.setTextOverrun(OverrunStyle.CLIP);

            final ImageView ivEnergy = new ImageView(TileModifier.loadCachedImage("Energy"));
            ivEnergy.setFitWidth(15);
            ivEnergy.setFitHeight(15);

            final Label lEnergy = new Label(String.format("%d", rp.getEnergy()));
            lEnergy.getStyleClass().add("text-sm");

            final HBox hEnergy = new HBox(ivEnergy, lEnergy);
            hEnergy.setSpacing(5);
            hEnergy.setAlignment(Pos.CENTER);

            final ImageView ivCheckpoint = new ImageView(TileModifier.loadCachedImage("CheckPointIcon"));
            ivCheckpoint.setFitWidth(15);
            ivCheckpoint.setFitHeight(15);

            final Label lCheckpoint = new Label(String.format("%d", rp.getCheckPointsReached()));
            lCheckpoint.getStyleClass().add("text-sm");

            final HBox hCheckpoint = new HBox(ivCheckpoint, lCheckpoint);
            hCheckpoint.setSpacing(5);
            hCheckpoint.setAlignment(Pos.CENTER);

            final HBox hEnergyCheckpoint = new HBox(hEnergy, hCheckpoint);
            hEnergyCheckpoint.setSpacing(10);
            hEnergyCheckpoint.setAlignment(Pos.CENTER);

            final HBox boughtUpgrades = new HBox();
            boughtUpgrades.setSpacing(5);
            boughtUpgrades.setAlignment(Pos.CENTER);
            for (int j = 0; j < rp.getBoughtUpgradeCards().size(); ++j)
            {
                final ImageView iv = new ImageView();
                iv.setFitWidth(     GameJFXController.UPGRADE_PREVIEW_WIDTH   );
                iv.setFitHeight(    GameJFXController.UPGRADE_PREVIEW_HEIGHT  );
                iv.setImage(TileModifier.loadUpgradePreview(rp.getBoughtUpgradeCards().get(j)));

                boughtUpgrades.getChildren().add(iv);
                continue;
            }

            final VBox v = new VBox(figureName, ctrlName, hEnergyCheckpoint, boughtUpgrades);
            v.getStyleClass().add("player-box");
            v.getStyleClass().add(String.format("player-box-%s", rp == EGameState.INSTANCE.getCurrentPlayer() ? "active" : "inactive" ));
            if (EGameState.INSTANCE.getCurrentPhase().equals(EGamePhase.PROGRAMMING))
            {
                v.getStyleClass().add(String.format("player-box-%s", rp.hasSelectionFinished() ? "selected" : "inSelection"));
            }
            v.setStyle(String.format("-fx-background-color: %s;",
                  rp.getFigure() == EFigure.  HAMMER  ? GameJFXController.    COLOR_HAMMER
                : rp.getFigure() == EFigure.  TRUNDLE ? GameJFXController.    COLOR_TRUNDLE
                : rp.getFigure() == EFigure.  SQUASH  ? GameJFXController.    COLOR_SQUASH
                : rp.getFigure() == EFigure.  X90     ? GameJFXController.    COLOR_X90
                : rp.getFigure() == EFigure.  SPIN    ? GameJFXController.    COLOR_SPIN
                : rp.getFigure() == EFigure.  TWONKY  ? GameJFXController.    COLOR_TWONKY
                : rp.getFigure() == EFigure.  TWITCH  ? GameJFXController.    COLOR_TWITCH
                : ""
            ));

            final AnchorPane ap = new AnchorPane();
            HBox.setHgrow(ap, Priority.ALWAYS);

            if (rp.hasRebooted())
            {
                final ImageView iv = new ImageView();

                iv.setFitWidth(     GameJFXController.REBOOTED_RCARD_WIDTH   );
                iv.setFitHeight(    GameJFXController.REBOOTED_RCARD_HEIGHT  );
                iv.setImage(TileModifier.loadCachedImage("RebootedCard"));

                if (i % 2 == 0)
                {
                    AnchorPane.setLeftAnchor(iv, 10.0);
                }
                else
                {
                    AnchorPane.setRightAnchor(iv, 10.0);
                }

                AnchorPane.setTopAnchor(iv, 6.0);

                ap.getChildren().add(iv);
            }
            else
            {
                for (int j = 0; j < rp.getPlayedRCards().length; ++j)
                {
                    final ImageView iv = new ImageView();

                    iv.setFitWidth(     GameJFXController.RCARD_WIDTH   );
                    iv.setFitHeight(    GameJFXController.RCARD_HEIGHT  );
                    iv.setTranslateX(j * GameJFXController.RCARD_TRANSLATION_DIFF_X * (i % 2 == 0 ? 1 : -1));
                    iv.setImage(TileModifier.loadCachedImage(rp.getPlayedRCards()[j]));

                    if (i % 2 == 0)
                    {
                        AnchorPane.setLeftAnchor(iv, 10.0);
                    }
                    else
                    {
                        AnchorPane.setRightAnchor(iv, 10.0);
                    }

                    AnchorPane.setTopAnchor(iv, 6.0);

                    ap.getChildren().add(iv);

                    continue;
                }
            }

            final int finalI = i;

            if (ap.getWidth() > 0)
            {
                /* Legacy */
                ap.setMaxWidth(ap.getWidth());
            }

            if (!rp.hasRebooted())
            {
                ap.setOnMouseEntered(e ->
                {
                    final ArrayList<Integer> newTranslations = new ArrayList<Integer>();

                    for (int j = ap.getChildren().size() - 1; j >= 0; --j)
                    {
                        newTranslations.add( (int) ( (-GameJFXController.RCARD_PREVIEW_TRANSLATION_X * (ap.getChildren().size() - 1 - j)) + Math.abs(ap.getChildren().get(j).getTranslateX())) );
                        continue;
                    }

                    /* East translation cleanup. */
                    if (finalI % 2 == 1)
                    {
                        for (int j = 0; j < ap.getChildren().size(); j++)
                        {
                            newTranslations.set(j, newTranslations.get(j) - (GameJFXController.RCARD_PREVIEW_TRANSLATION_X - 2 * GameJFXController.RCARD_PREVIEW_TRANSLATION_X_CLEANUP));
                            continue;
                        }
                    }

                    for (int j = 0; j < ap.getChildren().size(); j++)
                    {
                        final Timeline t    = new Timeline();
                        final KeyFrame kf   = new KeyFrame(Duration.millis(GameJFXController.RCARDS_TRANSLATION_DURATION), new KeyValue(ap.getChildren().get(j).translateXProperty(), newTranslations.get(j)));
                        t.getKeyFrames().add(kf);
                        t.play();

                        continue;
                    }

                    for (final Rectangle r : GameJFXController.getHoverPCardBackgrounds(ap, newTranslations, finalI))
                    {
                        ap.getChildren().add(0, r);
                        continue;
                    }

                    return;
                }
                );

                ap.setOnMouseExited(e ->
                {
                    ap.getChildren().clear();
                    ap.setStyle("");

                    for (int j = 0; j < rp.getPlayedRCards().length; ++j)
                    {
                        final ImageView iv = new ImageView();

                        iv.setFitWidth(GameJFXController.RCARD_WIDTH);
                        iv.setFitHeight(GameJFXController.RCARD_HEIGHT);
                        iv.setImage(TileModifier.loadCachedImage(rp.getPlayedRCards()[j]));
                        iv.setTranslateX(j * GameJFXController.RCARD_TRANSLATION_DIFF_X * (finalI % 2 == 0 ? 1 : -1));

                        if (finalI % 2 == 0)
                        {
                            AnchorPane.setLeftAnchor(iv, 10.0);
                        }
                        else
                        {
                            AnchorPane.setRightAnchor(iv, 10.0);
                        }

                        AnchorPane.setTopAnchor(iv, 6.0);

                        ap.getChildren().add(iv);

                        continue;
                    }

                    return;
                }
                );
            }

            final HBox h = new HBox();
            h.getStyleClass().add("player-information-container");
            h.setStyle(EGameState.INSTANCE.getClientRemotePlayer() == null ? String.format("-%sx-border-color: #282828ff", "f") : EGameState.INSTANCE.getClientRemotePlayer().getPlayerID() == rp.getPlayerID() ? String.format("-%sx-border-color: #ffffffff", "f") : String.format("-%sx-border-color: #282828ff", "f"));
            if (i % 2 != 0)
            {
                h.getChildren().add(ap);
            }
            h.getChildren().add(v);
            if (i % 2 == 0)
            {
                h.getChildren().add(ap);
            }

            ( (Pane) this.playerContainer.getChildren().get(this.playerContainer.getChildren().size() - 1)).getChildren().add(h);

            continue;
        }

        return;
    }

    /**
     * Super method for all HUD updates. Will rerender everything on the HUD.
     * No re-renders must be done after this method.
     */
    public void renderHUD()
    {
        this.renderHUDFooter();
        this.renderPlayerInformationArea();

        return;
    }

    // endregion Head Up Display

    // region Course View

    // region Helper Methods

    private void centerCourse()
    {
        this.courseScrollPane.setHvalue(0.5);
        this.courseScrollPane.setVvalue(0.5);
        return;
    }

    private void updateGlobalVariables()
    {
        /* We can do this because even if there is no tile, it must always be annotated with a null JSON Object. */
        this.files = EGameState.INSTANCE.getCurrentServerCourseJSON().toList().size();
        this.ranks = EGameState.INSTANCE.getCurrentServerCourseJSON().getJSONArray(0).toList().size();

        this.tiles = new Tile[this.files][this.ranks];
        for (int i = 0; i < this.files; i++)
        {
            for (int j = 0; j < this.ranks; j++)
            {
                final Tile t = new Tile(EGameState.INSTANCE.getCurrentServerCourseJSON().getJSONArray(i).getJSONArray(j));
                t.setXTranslation(i);
                t.setYTranslation(j);
                tiles[i][j] = t;
                continue;
            }

            continue;
        }

        this.minXTranslation        = this.tiles[0][0].getXTranslation() * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2;
        this.maxXTranslation        = this.tiles[this.files - 1][0].getXTranslation() * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2;
        this.centralXTranslation    = ((this.courseScrollPane.getWidth() - this.maxXTranslation) - this.minXTranslation) / 2 - (double) this.tileDimensions / 2;

        return;
    }

    private void setStyleOfCourseViewContent()
    {
        this.courseScrollPaneContent.setStyle(String.format("-fx-background-color: #000000ff; -fx-min-width: %spx; -fx-min-height: %spx;", this.files * this.tileDimensions + ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL, this.ranks * this.tileDimensions + ViewSupervisor.VIRTUAL_SPACE_VERTICAL));
        return;
    }

    public void renderOnPosition(final AnchorPane ap, final RCoordinate c)
    {
        ap.getStyleClass().add("tile");

        ap.setTranslateX(this.calcXTranslation(c.x()));
        ap.setTranslateY(this.calcYTranslation(c.y()));

        if (!this.courseScrollPaneContent.getChildren().contains(ap))
        {
            this.courseScrollPaneContent.getChildren().add(ap);
        }

        return;
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

    // endregion Helper Methods

    /**
     * Renders the actual course board. This method will take some time to execute. Do not call this method if you only
     * want to update a small part of the view. Use the specific update methods instead.
     *
     * <p>It will render everything that is static on the course board.
     *
     * <p>You will have to re-render movable parts of the course view after this method (like player positions).
     */
    private void renderCourseBoard()
    {
        this.courseScrollPaneContent.getChildren().clear();
        this.gears.clear();

        if (EGameState.INSTANCE.getCurrentServerCourseJSON() == null)
        {
            l.error("Tried to render game scene but no course data is available.");
            return;
        }

        this.updateGlobalVariables();
        this.setStyleOfCourseViewContent();

        for (int i = 0; i < this.files; i++)
        {
            for (int j = 0; j < this.ranks; j++)
            {
                final Tile t            = this.tiles[i][j];

                /* Check for checkpoints and add them. */
                if (t.hasModifier(EModifier.CHECK_POINT))
                {
                    if (t.getCheckpointID() == -1)
                    {
                        l.fatal("Assumed checkpoint on state {}, but the checkpoint was missing.", t.getTileLocation());
                        GameInstance.kill(GameInstance.EXIT_FATAL);
                        return;
                    }

                    boolean bAddToArray = true;
                    for (final RCheckpointMask mask : Objects.requireNonNull(EGameState.INSTANCE.getCurrentCheckpointLocations()))
                    {
                        if (Objects.equals(mask.id(), t.getCheckpointID()))
                        {
                            bAddToArray = false;
                            break;
                        }

                        continue;
                    }

                    if (bAddToArray)
                    {
                        l.info("Found checkpoint {} on state {}.", t.getCheckpointID(), t.getTileLocation());
                        Objects.requireNonNull(EGameState.INSTANCE.getCurrentCheckpointLocations()).add(new RCheckpointMask(t.getTileLocation(), t.getCheckpointID()));
                    }
                }

                final AnchorPane AP     = new AnchorPane();
                /* Warning: This is not commutative. Do not change the order here. */
                for (int k = t.getImageViews().length - 1; k >= 0; --k)
                {
                    final ImageView iv = t.getImageViews()[k];
                    iv.setFitHeight(    this.tileDimensions );
                    iv.setFitWidth(     this.tileDimensions );
                    AP.getChildren().add(iv);

                    if (TileModifier.isGear(iv.getImage()))
                    {
                        this.gears.add(TileModifier.generateGearMask(iv));
                    }

                    continue;
                }
                this.renderOnPosition(AP, t.getTileLocation());

                if (t.isClickable() && EGameState.INSTANCE.getCurrentPhase() == EGamePhase.REGISTRATION && EGameState.INSTANCE.getCurrentPlayer().getPlayerID() == Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getPlayerID())
                {
                    boolean bIsTaken = false;

                    for (final RemotePlayer rp : EGameState.INSTANCE.getRemotePlayers())
                    {
                        if (rp.hasStartingPosition())
                        {
                            if (Objects.equals(rp.getStartingPosition(), t.getTileLocation()))
                            {
                                bIsTaken = true;
                                break;
                            }
                        }

                        continue;
                    }

                    if (bIsTaken)
                    {
                        continue;
                    }

                    AP.setOnMouseClicked(e ->
                    {
                        l.info("User clicked on tile. Checking if valid move.");

                        if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.REGISTRATION)
                        {
                            l.warn("User clicked on tile but it is not the registration phase.");
                            return;
                        }

                        if (Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).hasStartingPosition())
                        {
                            l.warn("User already has a starting position.");
                            return;
                        }

                        if (EGameState.INSTANCE.getClientRemotePlayer() != EGameState.INSTANCE.getCurrentPlayer())
                        {
                            l.warn("Player can not set starting position because it is not their turn.");
                            return;
                        }

                        /* To prevent spamming the server with requests. */
                        if (this.bClickedOnTile)
                        {
                            l.warn("Already waiting for server response.");
                            return;
                        }

                        l.info("User wants to set starting position.");
                        /* TODO Some kind of validation. */
                        new SetStartingPointModel(t.getXTranslation(), t.getYTranslation()).send();
                        this.bClickedOnTile = true;

                        return;
                    });

                    final AnchorPane ap = new AnchorPane();
                    ap.setPrefWidth(this.tileDimensions);
                    ap.setPrefHeight(this.tileDimensions);
                    ap.getStyleClass().add("tile-after");

                    final Rectangle rec = new Rectangle();
                    rec.setWidth(this.tileDimensions);
                    rec.setHeight(this.tileDimensions);

                    ap.getChildren().add(rec);

                    final FillTransition ft = new FillTransition(Duration.millis(GameJFXController.BLINK_DURATION), rec, Color.valueOf("#ffffff3c"), Color.valueOf("#0000003c"));
                    ft.setCycleCount(Animation.INDEFINITE);
                    ft.setAutoReverse(true);

                    /* Kinda sketchy. */
                    ft.play();
                    ap.setOnMouseEntered(   e ->    ft.stop()   );
                    ap.setOnMouseExited(    e ->    ft.play()   );

                    AP.getChildren().add(ap);
                }

                continue;
            }

            continue;
        }

        return;
    }

    private void renderCheckpoints()
    {
        if (EGameState.INSTANCE.getCurrentServerCourseJSON() == null)
        {
            l.error("Tried to render checkpoints on game scene but no course data is available.");
            return;
        }

        if (Objects.requireNonNull(EGameState.INSTANCE.getCurrentCheckpointLocations()).isEmpty())
        {
            l.fatal("No checkpoints found. Every course must have at least one checkpoint.");
            GameInstance.kill(GameInstance.EXIT_FATAL);
            return;
        }

        for (final AnchorPane ap : this.checkpoints)
        {
            this.courseScrollPaneContent.getChildren().remove(ap);
            continue;
        }

        this.checkpoints.clear();

        for (final RCheckpointMask mask : Objects.requireNonNull(EGameState.INSTANCE.getCurrentCheckpointLocations()))
        {
            l.trace("Rendering checkpoint {} on state {}.", mask.id(), mask.location());

            final ImageView iv = Tile.getFormattedImageView(mask);
            iv.setFitHeight(    this.tileDimensions );
            iv.setFitWidth(     this.tileDimensions );

            final AnchorPane ap = new AnchorPane(iv);

            this.renderOnPosition(ap, mask.location());

            this.checkpoints.add(ap);

            continue;
        }

        return;
    }

    /**
     * Updates player positions on the course view.
     * No re-renders must be done after this method.
     */
    private void renderPlayerTransforms()
    {
        for (final RemotePlayer RP : EGameState.INSTANCE.getRemotePlayers())
        {
            if (!RP.hasStartingPosition())
            {
                continue;
            }

            /* Updating the current view. */
            if (RP.getRobotView().hasPosition())
            {
                RP.getRobotView().renderPosition();
                continue;
            }

            RP.getRobotView().setPosition(RP.getStartingPosition(), false, false);
            RP.getRobotView().renderPosition();

            continue;
        }

        return;
    }

    /**
     * Updates the whole course view. This is very costly. Do not call this method if you only want to update a small
     * part of the view. Use the specific update methods instead.
     * No re-renders must be done after this method.
     */
    private void renderCourse()
    {
        this.renderCourseBoard();
        this.renderCheckpoints();
        this.renderPlayerTransforms();

        return;
    }

    // endregion Course View

    /**
     * This is the top level rendering method. It will rerender the whole view. This is very costly.
     * Do not call this method if you only want to update a small part of the view.
     */
    private void renderView()
    {
        this.renderHUD();
        this.renderCourse();
        return;
    }

    // region Animation Rendering

    private void renderGearAnim()
    {
        for (int i = 0; i < this.gears.size(); i++)
        {
            /* TODO Depending on the length of the game the rotation will increase drastically. We may want to reset it. */
            final int newRot    = this.gears.get(i).rotation() + (this.gears.get(i).bClockwise() ? 90 : -90);
            final Timeline t    = new Timeline();
            final KeyFrame kf   = new KeyFrame(Duration.millis(GameJFXController.GEAR_ANIMATION_DURATION), new KeyValue(this.gears.get(i).iv().rotateProperty(), newRot));
            t.getKeyFrames().add(kf);

            t.play();

            this.gears.set(i, new RGearMask(this.gears.get(i).iv(), this.gears.get(i).bClockwise(), newRot));

            continue;
        }

        return;
    }

    private void renderRobotShootingAnim()
    {
        for (final RemotePlayer rp : EGameState.INSTANCE.getRemotePlayers())
        {
            if (!rp.getRobotView().hasPosition())
            {
                continue;
            }

            if (rp.hasRebooted())
            {
                continue;
            }

            for (final RLaserMask mask : rp.getRobotView().getLaserAffectedTiles(this.tiles, 1))
            {
                final ImageView iv = Tile.getFormattedImageView(mask);
                iv.setFitHeight(    this.tileDimensions );
                iv.setFitWidth(     this.tileDimensions );

                final AnchorPane ap = new AnchorPane(iv);

                this.renderOnPosition(ap, mask.t().getTileLocation());

                final PauseTransition p = new PauseTransition(Duration.millis(GameJFXController.SHOOTING_ROBOT_LASER_DURATION));
                p.setOnFinished(e -> this.courseScrollPaneContent.getChildren().remove(ap));
                p.play();

                continue;
            }

            continue;
        }

        return;
    }

    private void renderWallShootingAnim()
    {
        for (final Tile t : this.getWallLasers())
        {
            for (final RLaserMask mask : t.getLaserAffectedTiles(this.tiles, 1))
            {
                final ImageView iv = Tile.getFormattedImageView(mask);
                iv.setFitHeight(    this.tileDimensions );
                iv.setFitWidth(     this.tileDimensions );

                final AnchorPane ap = new AnchorPane(iv);

                this.renderOnPosition(ap, mask.t().getTileLocation());

                final PauseTransition p = new PauseTransition(Duration.millis(GameJFXController.SHOOTING_WALL_LASER_DURATION));
                p.setOnFinished(e -> this.courseScrollPaneContent.getChildren().remove(ap));
                p.play();

                continue;
            }

            continue;
        }

        return;
    }

    // endregion Animation Rendering

    private void renderAnimation(final EAnimation anim)
    {
        l.debug("Rendering animation: {}", anim);
        switch (anim)
        {
        case BLUE_CONVEYOR_BELT ->
        {
            l.warn("Server requested to render blue conveyor belt animation. Not implemented yet.");
            break;
        }
        case GREEN_CONVEYOR_BELT ->
        {
            l.warn("Server requested to render green conveyor belt animation. Not implemented yet.");
            break;
        }
        case PUSH_PANEL ->
        {
            l.warn("Server requested to render push panel animation. Not implemented yet.");
            break;
        }
        case GEAR ->
        {
            this.renderGearAnim();
            break;
        }
        case CHECK_POINT ->
        {
            l.warn("Server requested to render check point animation. Not implemented yet.");
            break;
        }
        case PLAYER_SHOOTING ->
        {
            this.renderRobotShootingAnim();
            break;
        }
        case WALL_SHOOTING ->
        {
            this.renderWallShootingAnim();
            break;
        }
        case ENERGY_SPACE ->
        {
            l.warn("Server requested to render energy space animation. Not implemented yet.");
            break;
        }
        }

        return;
    }

    // endregion Rendering

    // region Update View Methods from outside

    public void onPhaseUpdate()
    {
        Platform.runLater(() ->
        {
            this.renderHUD();
            return;
        });

        return;
    }

    public void onPlayerUpdate()
    {
        Platform.runLater(() ->
        {
            this.renderHUD();
            return;
        });

        return;
    }

    public void onPlayerAdded()
    {
        Platform.runLater(() ->
        {
            this.renderPlayerInformationArea();
            return;
        });

        return;
    }

    public void onRPhase()
    {
        Platform.runLater(() ->
        {
            this.renderPlayerInformationArea();
            return;
        });

        return;
    }

    public void onCourseUpdate()
    {
        Platform.runLater(() ->
        {
            this.renderView();
            return;
        });

        return;
    }

    public void onPlayerTransformUpdate()
    {
        Platform.runLater(() ->
        {
            this.renderPlayerTransforms();
            return;
        });

        return;
    }

    public void onFooterUpdate()
    {
        Platform.runLater(() ->
        {
            this.renderHUDFooter();
            return;
        });

        return;
    }

    public void onChatMsgReceived(final int sourceID, final String msg, final boolean bIsPrivate)
    {
        Platform.runLater(() ->
        {
            this.addChatMsgToView(sourceID, msg, bIsPrivate);
            return;
        });

        return;
    }

    public void onPlayerRemoved()
    {
        Platform.runLater(() ->
        {
            this.renderView();
            return;
        });

        return;
    }

    public void playAnimation(final EAnimation anim)
    {
        Platform.runLater(() ->
        {
            this.renderAnimation(anim);
            return;
        });
    }

    public void onFooterStateUpdate(final boolean bCollapsed)
    {
        Platform.runLater(() ->
        {
            this.bFooterCollapsed = bCollapsed;
            this.renderHUDFooter();
            return;
        });

        return;
    }

    public void onCenterCourse()
    {
        Platform.runLater(() ->
        {
            this.centerCourse();
            return;
        });

        return;
    }

    public void onCheckpointMoved()
    {
        Platform.runLater(() ->
        {
            this.renderCheckpoints();
            return;
        });

        return;
    }

    public void onMemoryCardsReceived(final ArrayList<String> cards)
    {
        Platform.runLater(() ->
        {
            this.memorySwapCards.clear();
            this.memorySwapDiscardedCards.clear();
            this.memorySwapCards.addAll(cards);
            this.renderHUDFooter();
            return;
        });

        return;
    }

    // endregion Update View Methods from outside

    // region Getters and Setters

    public int getCurrentTileDimensions()
    {
        return this.tileDimensions;
    }

    private String getChatMsg()
    {
        return this.chatInputTextField.getText();
    }

    private boolean isChatMsgValid(final String token)
    {
        return !token.isEmpty() && token.length() <= EGameState.MAX_CHAT_MESSAGE_LENGTH;
    }

    private boolean isChatMsgACommand(final String token)
    {
        return token.startsWith(ChatMsgModel.COMMAND_PREFIX);
    }

    private String getChatCommand(final String token)
    {
        return !token.contains(" ") ? token.substring(1) : token.substring(1, token.indexOf(" "));
    }

    public double calcXTranslation(final int file)
    {
        return this.centralXTranslation < 0 ? file * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2 : file * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2 + this.centralXTranslation;
    }

    public double calcYTranslation(final int rank)
    {
        return rank * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_VERTICAL / 2;
    }

    public Tile[] getWallLasers()
    {
        final ArrayList<Tile> wallLasers = new ArrayList<>();

        for (int file = 0; file < this.files; file++)
        {
            for (int rank = 0; rank < this.ranks; rank++)
            {
                if (this.tiles[file][rank].hasModifier(EModifier.LASER))
                {
                    wallLasers.add(this.tiles[file][rank]);
                    continue;
                }

                continue;
            }

            continue;
        }

        return wallLasers.toArray(new Tile[0]);
    }

    private static ArrayList<Rectangle> getHoverPCardBackgrounds(final AnchorPane target, final ArrayList<Integer> newTranslations, final int idx)
    {
        final ArrayList<Rectangle> rs = new ArrayList<Rectangle>();

        for (int j = 0; j < target.getChildren().size(); j++)
        {
            final Rectangle r = new Rectangle();

            r.setWidth(GameJFXController.RCARD_PREVIEW_TRANSLATION_X + 2 * GameJFXController.RCARD_PREVIEW_TRANSLATION_X_CLEANUP);
            r.setHeight(target.getHeight());
            r.setStyle(String.format("-fx-fill: %s", GameJFXController.COLOR_RCARD_PREVIEW_BG));
            r.setTranslateX(newTranslations.get(j) + (idx % 2 == 0 ? GameJFXController.RCARD_PREVIEW_TRANSLATION_X_CLEANUP : GameJFXController.RCARD_PREVIEW_TRANSLATION_X - RCARD_PREVIEW_TRANSLATION_X_CLEANUP - GameJFXController.RCARD_PREVIEW_TRANSLATION_X_ALPHA));

            rs.add(r);

            continue;
        }

        return rs;
    }

    public ArrayList<RShopAction> getPendingShopActions()
    {
        return this.pendingShopActions;
    }

    public void resetProgrammingTimeline()
    {
        if (this.programmingTimeline == null)
        {
            return;
        }

        this.programmingTimeline.stop();
        this.programmingTimeline = null;

        return;
    }

    // endregion Getters and Setters

}
