package sep.view.scenecontrollers;

import sep.view.clientcontroller.   EGameState;
import sep.view.clientcontroller.   RemotePlayer;
import sep.view.clientcontroller.   EClientInformation;
import sep.view.json.game.          SelectedCardModel;
import sep.view.json.game.          SetStartingPointModel;
import sep.view.json.               ChatMsgModel;
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

import javafx.application.          Platform;
import java.util.                   ArrayList;
import java.util.                   Objects;
import javafx.scene.layout.         HBox;
import javafx.scene.layout.         VBox;
import javafx.scene.layout.         AnchorPane;
import javafx.scene.layout.         Priority;
import javafx.scene.layout.         Region;
import javafx.scene.layout.         Pane;
import javafx.animation.            Animation;
import javafx.animation.            PauseTransition;
import javafx.animation.            Timeline;
import javafx.animation.            KeyFrame;
import javafx.animation.            KeyValue;
import javafx.animation.            FillTransition;
import javafx.fxml.                 FXML;
import javafx.beans.value.          ChangeListener;
import javafx.beans.value.          ObservableValue;
import javafx.scene.                Node;
import javafx.scene.image.          ImageView;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import javafx.scene.shape.          Rectangle;
import javafx.event.                ActionEvent;
import javafx.scene.input.          KeyCode;
import javafx.util.                 Duration;
import javafx.scene.control.        Label;
import javafx.scene.control.        TextField;
import javafx.scene.control.        ScrollPane;
import javafx.scene.control.        Button;
import javafx.scene.paint.          Color;

public final class GameJFXController
{
    private static final Logger l = LogManager.getLogger(GameJFXController.class);

    private static final String COLOR_HAMMER    = "#ff000033";
    private static final String COLOR_TRUNDLE   = "#0000ff33";
    private static final String COLOR_SQUASH    = "#ffc0cb33";
    private static final String COLOR_X90       = "#00ff0033";
    private static final String COLOR_SPIN      = "#00ffff33";
    private static final String COLOR_TWONKY    = "#ffff0033";
    private static final String COLOR_TWITCH    = "#aaaaaa33";

    private static final int    SHOOTING_ROBOT_LASER_DURATION   = 1_000 ;
    private static final int    SHOOTING_WALL_LASER_DURATION    = 1_000 ;
    private static final int    CHAT_SCROLL_TIMEOUT             = 15    ;
    private static final int    GEAR_ANIMATION_DURATION         = 1_000 ;
    private static final int    BLINK_DURATION                  = 800   ;

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
    @FXML private Button        chatBtn;

    private static final int RCARD_WIDTH                = 50;
    private static final int RCARD_HEIGHT               = 88;
    private static final int RCARD_TRANSLATION_DIFF_X   = 10;

    private VBox        chatContainer;
    private boolean     showServerInfo;

    private int                 tileDimensions;
    private static final int    RESIZE_AMOUNT = 10;

    private boolean             bClickedOnTile;
    private int                 gotRegisterSlotClicked;
    private static final int    INVALID_GOT_REGISTER_SLOT = -1;

    private boolean             bFooterCollapsed;

    private HBox                registerHBox;
    private static final int    FOOTER_PEEK_HEIGHT = 50;
    /** During initialization. */
    private static final int    NULL_FOOTER_HEIGHT = 200;

    private int                         files;
    private int                         ranks;
    private Tile[][]                    tiles;
    private final ArrayList<RGearMask>  gears;
    private double                      minXTranslation;
    private double                      maxXTranslation;
    private double                      centralXTranslation;

    public GameJFXController()
    {
        super();

        this.showServerInfo = false;

        this.tileDimensions = ViewSupervisor.TILE_DIMENSIONS;

        this.bClickedOnTile         = false;
        this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;

        this.bFooterCollapsed   = true;
        this.registerHBox       = null;

        this.files                  = 0;
        this.ranks                  = 0;
        this.tiles                  = null;
        this.gears                  = new ArrayList<>();
        this.minXTranslation        = 0.0;
        this.maxXTranslation        = 0.0;
        this.centralXTranslation    = 0.0;

        return;
    }

    @FXML
    private void initialize()
    {
        VBox.setVgrow(this.chatScrollPane, Priority.ALWAYS);

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
            // TODO Add min max
            switch (e.getCode())
            {
            /* Zoom in. */
            case W:
                this.tileDimensions += GameJFXController.RESIZE_AMOUNT;
                this.renderCourse();
                break;

            /* Zoom out. */
            case S:
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

    @FXML
    private void onChatBtn(final ActionEvent actionEvent)
    {
        this.showServerInfo = !this.showServerInfo;
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

    // region HUD Side Panel

    /** Updates the UI Phase Title in the Header. */
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

    /** @param idx Index of the register slot. */
    private void addRegisterSlot(final int idx, final boolean bIsGotRegister, final Pane p, final int w, final int h)
    {
        final ImageView iv = this.getCardRegisterSlot(w, h, bIsGotRegister ? EGameState.INSTANCE.getGotRegister(idx) : EGameState.INSTANCE.getRegister(idx));
        final AnchorPane ap = new AnchorPane();

        if (bIsGotRegister)
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
        }

        ap.setOnMouseClicked(e
        ->
        {
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

        this.registerContainer.getChildren().clear();
        this.registerContainer.getChildren().add(this.registerHBox);
        for (int i = 0; i < 5; i++)
        {
            this.addRegisterSlot(i, false, this.registerHBox, ViewSupervisor.REGISTER_SLOT_WIDTH, ViewSupervisor.REGISTER_SLOT_HEIGHT);
            continue;
        }

        this.registerHBox.getChildren().add(new VBox());
        for (int i = 0; i < 9; i++)
        {
            if (i % 3 == 0)
            {
                ( (Pane) this.registerHBox.getChildren().get(this.registerHBox.getChildren().size() - 1) ).getChildren().add(new HBox());
            }

            this.addRegisterSlot
            (
              i
            , true
            , (Pane) ( (Pane) this.registerHBox.getChildren().get(this.registerHBox.getChildren().size() - 1) ).getChildren().get( ( (Pane) this.registerHBox.getChildren().get(this.registerHBox.getChildren().size() - 1) ).getChildren().size() - 1)
            , ViewSupervisor.GOT_REGISTER_SLOT_WIDTH
            , ViewSupervisor.GOT_REGISTER_SLOT_HEIGHT
            )
            ;

            continue;
        }

        return;
    }

    private void setFooterBtnText()
    {
        this.footerBtn.setText(this.bFooterCollapsed ? "Show" : "Collapse");
        return;
    }

    private void setChatBtnText(){
        this.chatBtn.setText(this.showServerInfo ? "Hide Info" : "Show Info");
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
     * Updates every dependency of the header.
     * No re-renders must be done after this method.
     */
    private void renderHUDHeader()
    {
        this.renderPhaseTitle();
        this.renderGameStateDescription();

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
        this.setChatBtnText();
        this.translateFooter();

        return;
    }

    /**
     * Displays every player in the lobby with some stats.
     * No re-renders must be done after this method.
     */
    private void renderPlayerInformationArea()
    {
        this.playerContainer.getChildren().clear();

        for (int i = 0; i < EGameState.INSTANCE.getRemotePlayers().length; i++)
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

            final Label energyCubes = new Label(String.format("Energy: %d", rp.getEnergyCubes()));
            energyCubes.getStyleClass().add("text-sm");

            final VBox v = new VBox(figureName, ctrlName, energyCubes);
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
        this.renderHUDHeader();
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

    // endregion Getters and Setters

}
