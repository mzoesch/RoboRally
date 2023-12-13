package sep.view.scenecontrollers;

import sep.view.clientcontroller.EGameState;
import sep.view.clientcontroller.RemotePlayer;
import sep.view.viewcontroller.Tile;
import sep.view.viewcontroller.ViewSupervisor;
import sep.view.lib.RCoordinate;
import sep.view.json.game.SetStartingPointModel;
import sep.view.viewcontroller.TileModifier;
import sep.view.lib.EGamePhase;
import sep.view.json.game.SelectedCardModel;
import sep.view.json.ChatMsgModel;
import sep.view.clientcontroller.EClientInformation;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.image.ImageView;
import java.util.ArrayList;
import java.util.Objects;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import javafx.scene.layout.Priority;
import javafx.scene.input.KeyCode;

public class GameJFXController
{
    private static final Logger l = LogManager.getLogger(GameJFXController.class);

    @FXML private Label UIHeaderPhaseLabel;
    @FXML private Label UIHeaderGameStateDescriptionLabel;
    @FXML private AnchorPane masterContainer;
    @FXML private HBox playerContainer;
    @FXML private AnchorPane courseContainer;
    @FXML private ScrollPane courseScrollPane;
    @FXML private AnchorPane courseScrollPaneContent;
    @FXML private AnchorPane registerSlot1;
    @FXML private AnchorPane registerSlot2;
    @FXML private AnchorPane registerSlot3;
    @FXML private AnchorPane registerSlot4;
    @FXML private AnchorPane registerSlot5;
    @FXML private AnchorPane gotRegisterCardSlot1;
    @FXML private AnchorPane gotRegisterCardSlot2;
    @FXML private AnchorPane gotRegisterCardSlot3;
    @FXML private AnchorPane gotRegisterCardSlot4;
    @FXML private AnchorPane gotRegisterCardSlot5;
    @FXML private AnchorPane gotRegisterCardSlot6;
    @FXML private AnchorPane gotRegisterCardSlot7;
    @FXML private AnchorPane gotRegisterCardSlot8;
    @FXML private AnchorPane gotRegisterCardSlot9;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField chatInputTextField;

    @FXML private AnchorPane shopSlot1;
    @FXML private AnchorPane shopSlot2;
    @FXML private AnchorPane shopSlot3;
    @FXML private AnchorPane shopSlot4;
    @FXML private AnchorPane shopSlot5;
    @FXML private AnchorPane gotTemporaryUpgradeCardSlot1;
    @FXML private AnchorPane gotTemporaryUpgradeCardSlot2;
    @FXML private AnchorPane gotTemporaryUpgradeCardSlot3;
    @FXML private AnchorPane gotPermanentUpgradeCardSlot1;
    @FXML private AnchorPane gotPermanentUpgradeCardSlot2;
    @FXML private AnchorPane gotPermanentUpgradeCardSlot3;

    private VBox chatContainer;
    private boolean showServerInfo = true;

    private int tileDimensions;
    private static final int resizeAmount = 10;

    private boolean bClickedOnTile;
    private int gotRegisterSlotClicked;
    private static final int INVALID_GOT_REGISTER_SLOT = -1;

    private int files;
    private int ranks;
    private Tile[][] tiles;
    private double minXTranslation;
    private double maxXTranslation;
    private double centralXTranslation;

    public GameJFXController()
    {
        super();

        this.tileDimensions = ViewSupervisor.TILE_DIMENSIONS;
        this.bClickedOnTile = false;
        this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;

        this.files = 0;
        this.ranks = 0;
        this.tiles = null;
        this.minXTranslation = 0.0;
        this.maxXTranslation = 0.0;
        this.centralXTranslation = 0.0;

        return;
    }

    @FXML
    private void initialize()
    {
        VBox.setVgrow(this.chatScrollPane, Priority.ALWAYS);

        this.courseScrollPane.setFitToWidth(true);
        this.courseScrollPane.setFitToHeight(true);
        this.courseScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.courseScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        this.courseScrollPane.widthProperty().addListener((obs, val, t1) ->
        {
            // TODO Only update translations not the whole course.
            this.renderCourse();
            return;
        });

        this.chatInputTextField.lengthProperty().addListener(new ChangeListener<Number>()
        {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1)
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

        // TODO Highly sketchy. Needs some testing.
        PauseTransition p = new PauseTransition(new Duration(2_000));
        p.setOnFinished(e ->
        {
            l.info("Scrolling view to center.");
            this.courseScrollPane.setHvalue(0.5);
            this.courseScrollPane.setVvalue(0.5);
            return;
        });
        p.play();

        this.initializeButtonActions();

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
                    this.tileDimensions += GameJFXController.resizeAmount;
                    this.renderCourse();
                    break;

                /* Zoom out. */
                case S:
                    this.tileDimensions -= GameJFXController.resizeAmount;
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

    private void initializeButtonActions()
    {

        {

        this.registerSlot1.setOnMouseClicked(e ->
        {
            this.onRegisterSlot1Clicked();
            return;
        });

        this.registerSlot2.setOnMouseClicked(e ->
        {
            this.onRegisterSlot2Clicked();
            return;
        });

        this.registerSlot3.setOnMouseClicked(e ->
        {
            this.onRegisterSlot3Clicked();
            return;
        });

        this.registerSlot4.setOnMouseClicked(e ->
        {
            this.onRegisterSlot4Clicked();
            return;
        });

        this.registerSlot5.setOnMouseClicked(e ->
        {
            this.onRegisterSlot5Clicked();
            return;
        });

        }

        {

        this.gotRegisterCardSlot1.setOnMouseClicked(e ->
        {
            this.onGotRegisterSlot1Clicked();
            return;
        });

        this.gotRegisterCardSlot2.setOnMouseClicked(e ->
        {
            this.onGotRegisterSlot2Clicked();
            return;
        });

        this.gotRegisterCardSlot3.setOnMouseClicked(e ->
        {
            this.onGotRegisterSlot3Clicked();
            return;
        });

        this.gotRegisterCardSlot4.setOnMouseClicked(e ->
        {
            this.onGotRegisterSlot4Clicked();
            return;
        });

        this.gotRegisterCardSlot5.setOnMouseClicked(e ->
        {
            this.onGotRegisterSlot5Clicked();
            return;
        });

        this.gotRegisterCardSlot6.setOnMouseClicked(e ->
        {
            this.onGotRegisterSlot6Clicked();
            return;
        });

        this.gotRegisterCardSlot7.setOnMouseClicked(e ->
        {
            this.onGotRegisterSlot7Clicked();
            return;
        });

        this.gotRegisterCardSlot8.setOnMouseClicked(e ->
        {
            this.onGotRegisterSlot8Clicked();
            return;
        });

        this.gotRegisterCardSlot9.setOnMouseClicked(e ->
        {
            this.onGotRegisterSlot9Clicked();
            return;
        });

        }

        return;
    }

    // endregion Got Register Slot Action Methods

    // region Chat

    private void onSubmitChatMsg()
    {
        String token = this.getChatMsg();
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
                int idxBSBegin = token.indexOf("\"");
                String sub = token.substring(idxBSBegin + 1);
                if (!sub.contains("\""))
                {
                    this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }
                int idxBSEnd = sub.indexOf("\"");

                String targetPlayer = token.substring(idxBSBegin + 1, idxBSBegin + idxBSEnd + 1);
                if (targetPlayer.isEmpty() || targetPlayer.isBlank())
                {
                    this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "Invalid player name.", false);
                    return;
                }

                String msgToWhisper;
                try
                {
                    msgToWhisper = token.substring(idxBSBegin + idxBSEnd + 3);
                }
                catch (IndexOutOfBoundsException e)
                {
                    this.addChatMsgToView(ChatMsgModel.CLIENT_ID, "Invalid message.", false);
                    return;
                }
                if (msgToWhisper.isEmpty() || msgToWhisper.isBlank())
                {
                    return;
                }

                RemotePlayer target = EGameState.INSTANCE.getRemotePlayerByPlayerName(targetPlayer);
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
                ArrayList<Node> serverInfo = new ArrayList<>();
                for(Node node : chatContainer.getChildren()){
                    if(node instanceof Label label){
                        if(label.getStyleClass().contains("lobby-msg-server")){
                            node.setVisible(false);
                            serverInfo.add(node);
                            l.debug("Line added to serverInfo");
                        }
                    }
                }
                chatContainer.getChildren().removeAll(serverInfo);
                showServerInfo = false;
                return;
            }

            if (this.getChatCommand(token).equals("show"))
            {
                if(showServerInfo){
                    this.addChatMsgToView(ChatMsgModel.SERVER_ID, "ServerInfo is already shown", false);
                } else{
                    showServerInfo = true;
                    this.addChatMsgToView(ChatMsgModel.SERVER_ID, "ServerInfo is shown again", false);
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

    private void addChatMsgToView(int caller, String msg, boolean bIsPrivate)
    {
        if (caller == ChatMsgModel.SERVER_ID)
        {
            if(showServerInfo) {
                Label l = new Label(String.format("[%s] %s", ChatMsgModel.SERVER_NAME, msg));
                l.getStyleClass().add("lobby-msg-server");
                l.setWrapText(true);
                this.chatContainer.getChildren().add(l);

                /* Kinda sketchy. But is there a better way? */
                PauseTransition p = new PauseTransition(Duration.millis(15));
                p.setOnFinished(f -> this.chatScrollPane.setVvalue(1.0));
                p.play();
            }
            return;

        }

        if (caller == ChatMsgModel.CLIENT_ID)
        {
            Label l = new Label(String.format("[%s] %s", ChatMsgModel.CLIENT_NAME, msg));
            l.getStyleClass().add("lobby-msg-client");
            l.setWrapText(true);
            this.chatContainer.getChildren().add(l);

            /* Kinda sketchy. But is there a better way? */
            PauseTransition p = new PauseTransition(Duration.millis(15));
            p.setOnFinished(f -> this.chatScrollPane.setVvalue(1.0));
            p.play();

            return;
        }

        Label l = new Label(String.format("<%s>%s %s", Objects.requireNonNull(EGameState.INSTANCE.getRemotePlayerByPlayerID(caller)).getPlayerName(), bIsPrivate ? " whispers: " : "", msg));
        if (bIsPrivate)
        {
            l.getStyleClass().add("lobby-msg-whisper");
        }
        else
        {
            l.getStyleClass().add("lobby-msg");
        }
        l.setWrapText(true);
        this.chatContainer.getChildren().add(l);

        /* Kinda sketchy. But is there a better way? */
        PauseTransition p = new PauseTransition(Duration.millis(15));
        p.setOnFinished(f -> this.chatScrollPane.setVvalue(1.0));
        p.play();

        return;
    }

    // endregion Chat

    // region Rendering

    // region Head Up Display

    // region HUD Header

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

    /**
     * Updates the UI Game State Description in the Header.
     * What we are currently waiting for.
     */
    private void renderGameStateDescription()
    {
        switch (EGameState.INSTANCE.getCurrentPhase())
        {
            case REGISTRATION:
                this.UIHeaderGameStateDescriptionLabel.setText(String.format(": Waiting for %s to set their starting position.", EGameState.INSTANCE.getCurrentPlayer().getPlayerName()));
                return;

            case UPGRADE:
                this.UIHeaderGameStateDescriptionLabel.setText(": Select your upgradeCards");
                return;

            case PROGRAMMING:
                this.UIHeaderGameStateDescriptionLabel.setText(": Select your cards by clicking on them and then on an empty register. " +
                        "To empty a register, click on it without having selected a card.");
                return;

            case ACTIVATION:
                this.UIHeaderGameStateDescriptionLabel.setText(": Activation Phase. Just watch your robots move");
                return;
        }

        this.UIHeaderGameStateDescriptionLabel.setText("Unknown game state.");
        return;
    }

    // endregion HUD Header

    // region HUD Footer

    private ImageView getEmptyRegisterSlot(final int width, final int height)
    {
        final ImageView iv = new ImageView();
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        iv.getStyleClass().add("register-slot");
        iv.setImage(TileModifier.getImage("EmptyRegisterSlot"));
        return iv;
    }

    private ImageView getCardRegisterSlot(final int width, final int height, final String cardName, final int idx)
    {
        if (cardName == null)
        {
            return this.getEmptyRegisterSlot(width, height);
        }

        final ImageView iv = new ImageView();
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        iv.setImage(TileModifier.getImage(cardName));
        return iv;
    }

    /**
     * @param idx       Index of the register slot.
     * @param cardName  Name of the card to render. Pass null to render an empty slot.
     */
    private void renderRegisterSlot(int idx, String cardName)
    {
        final ImageView iv = this.getCardRegisterSlot(ViewSupervisor.REGISTER_SLOT_WIDTH, ViewSupervisor.REGISTER_SLOT_HEIGHT, cardName, idx);

        switch (idx)
        {
            case 0:
                this.registerSlot1.getChildren().clear();
                this.registerSlot1.getChildren().add(iv);
                this.registerSlot1.getStyleClass().clear();
                this.registerSlot1.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                            ? EGameState.INSTANCE.getRegister(0) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : EGameState.INSTANCE.getRegister(0) == null
                                ? "register-slot-available"
                                : "register-slot-disabled"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 1:
                this.registerSlot2.getChildren().clear();
                this.registerSlot2.getChildren().add(iv);
                this.registerSlot2.getStyleClass().clear();
                this.registerSlot2.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                            ? EGameState.INSTANCE.getRegister(1) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : EGameState.INSTANCE.getRegister(1) == null
                                ? "register-slot-available"
                                : "register-slot-disabled"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 2:
                this.registerSlot3.getChildren().clear();
                this.registerSlot3.getChildren().add(iv);
                this.registerSlot3.getStyleClass().clear();
                this.registerSlot3.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                            ? EGameState.INSTANCE.getRegister(2) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : EGameState.INSTANCE.getRegister(2) == null
                                ? "register-slot-available"
                                : "register-slot-disabled"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 3:
                this.registerSlot4.getChildren().clear();
                this.registerSlot4.getChildren().add(iv);
                this.registerSlot4.getStyleClass().clear();
                this.registerSlot4.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                            ? EGameState.INSTANCE.getRegister(3) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : EGameState.INSTANCE.getRegister(3) == null
                                ? "register-slot-available"
                                : "register-slot-disabled"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 4:
                this.registerSlot5.getChildren().clear();
                this.registerSlot5.getChildren().add(iv);
                this.registerSlot5.getStyleClass().clear();
                this.registerSlot5.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                            ? EGameState.INSTANCE.getRegister(4) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : EGameState.INSTANCE.getRegister(4) == null
                                ? "register-slot-available"
                                : "register-slot-disabled"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;
        }

        return;
    }

    /**
     * @param idx       Index of the got register slot.
     * @param cardName  Name of the card to render. Pass null to render an empty slot.
     */
    private void renderGotRegisterSlot(final int idx, final String cardName)
    {
        final ImageView iv = this.getCardRegisterSlot(ViewSupervisor.GOT_REGISTER_SLOT_WIDTH, ViewSupervisor.GOT_REGISTER_SLOT_HEIGHT, cardName, idx);

        switch (idx)
        {
            case 0:
                this.gotRegisterCardSlot1.getChildren().clear();
                this.gotRegisterCardSlot1.getChildren().add(iv);
                this.gotRegisterCardSlot1.getStyleClass().clear();
                this.gotRegisterCardSlot1.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked != idx
                            ? EGameState.INSTANCE.getGotRegister(0) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : "register-slot-active"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 1:
                this.gotRegisterCardSlot2.getChildren().clear();
                this.gotRegisterCardSlot2.getChildren().add(iv);
                this.gotRegisterCardSlot2.getStyleClass().clear();
                this.gotRegisterCardSlot2.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked != idx
                            ? EGameState.INSTANCE.getGotRegister(1) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : "register-slot-active"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 2:
                this.gotRegisterCardSlot3.getChildren().clear();
                this.gotRegisterCardSlot3.getChildren().add(iv);
                this.gotRegisterCardSlot3.getStyleClass().clear();
                this.gotRegisterCardSlot3.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked != idx
                            ? EGameState.INSTANCE.getGotRegister(2) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : "register-slot-active"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 3:
                this.gotRegisterCardSlot4.getChildren().clear();
                this.gotRegisterCardSlot4.getChildren().add(iv);
                this.gotRegisterCardSlot4.getStyleClass().clear();
                this.gotRegisterCardSlot4.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked != idx
                            ? EGameState.INSTANCE.getGotRegister(3) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : "register-slot-active"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 4:
                this.gotRegisterCardSlot5.getChildren().clear();
                this.gotRegisterCardSlot5.getChildren().add(iv);
                this.gotRegisterCardSlot5.getStyleClass().clear();
                this.gotRegisterCardSlot5.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked != idx
                            ? EGameState.INSTANCE.getGotRegister(4) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : "register-slot-active"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 5:
                this.gotRegisterCardSlot6.getChildren().clear();
                this.gotRegisterCardSlot6.getChildren().add(iv);
                this.gotRegisterCardSlot6.getStyleClass().clear();
                this.gotRegisterCardSlot6.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked != idx
                            ? EGameState.INSTANCE.getGotRegister(5) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : "register-slot-active"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 6:
                this.gotRegisterCardSlot7.getChildren().clear();
                this.gotRegisterCardSlot7.getChildren().add(iv);
                this.gotRegisterCardSlot7.getStyleClass().clear();
                this.gotRegisterCardSlot7.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked != idx
                            ? EGameState.INSTANCE.getGotRegister(6) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : "register-slot-active"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 7:
                this.gotRegisterCardSlot8.getChildren().clear();
                this.gotRegisterCardSlot8.getChildren().add(iv);
                this.gotRegisterCardSlot8.getStyleClass().clear();
                this.gotRegisterCardSlot8.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked != idx
                            ? EGameState.INSTANCE.getGotRegister(7) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : "register-slot-active"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;

            case 8:
                this.gotRegisterCardSlot9.getChildren().clear();
                this.gotRegisterCardSlot9.getChildren().add(iv);
                this.gotRegisterCardSlot9.getStyleClass().clear();
                this.gotRegisterCardSlot9.getStyleClass()
                .add(
                EGameState.INSTANCE.getCurrentPhase() == EGamePhase.PROGRAMMING
                    ? !EGameState.INSTANCE.areRegistersFull()
                        ? this.gotRegisterSlotClicked != idx
                            ? EGameState.INSTANCE.getGotRegister(8) == null
                                ? "register-slot-disabled"
                                : "register-slot"
                            : "register-slot-active"
                        : "register-slot-disabled"
                    : "register-slot-disabled"
                );
                break;
        }

        return;
    }

    private void renderGotTemporaryUpgradeCardSlot(final int idx, final String cardName)
    {
        final ImageView iv = this.getCardRegisterSlot(ViewSupervisor.GOT_REGISTER_SLOT_WIDTH, ViewSupervisor.GOT_REGISTER_SLOT_HEIGHT, cardName, idx);

        switch (idx) {
            case 0:
                this.gotTemporaryUpgradeCardSlot1.getChildren().clear();
                this.gotTemporaryUpgradeCardSlot1.getChildren().add(iv);
                this.gotTemporaryUpgradeCardSlot1.getStyleClass().clear();
                this.gotTemporaryUpgradeCardSlot1.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;

            case 1:
                this.gotTemporaryUpgradeCardSlot2.getChildren().clear();
                this.gotTemporaryUpgradeCardSlot2.getChildren().add(iv);
                this.gotTemporaryUpgradeCardSlot2.getStyleClass().clear();
                this.gotTemporaryUpgradeCardSlot2.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;

            case 2:
                this.gotTemporaryUpgradeCardSlot3.getChildren().clear();
                this.gotTemporaryUpgradeCardSlot3.getChildren().add(iv);
                this.gotTemporaryUpgradeCardSlot3.getStyleClass().clear();
                this.gotTemporaryUpgradeCardSlot3.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;
        }
        return;
    }

    private void renderGotPermanentUpgradeCardSlot(final int idx, final String cardName)
    {
        final ImageView iv = this.getCardRegisterSlot(ViewSupervisor.GOT_REGISTER_SLOT_WIDTH, ViewSupervisor.GOT_REGISTER_SLOT_HEIGHT, cardName, idx);

        switch (idx) {
            case 0:
                this.gotPermanentUpgradeCardSlot1.getChildren().clear();
                this.gotPermanentUpgradeCardSlot1.getChildren().add(iv);
                this.gotPermanentUpgradeCardSlot1.getStyleClass().clear();
                this.gotPermanentUpgradeCardSlot1.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;

            case 1:
                this.gotPermanentUpgradeCardSlot2.getChildren().clear();
                this.gotPermanentUpgradeCardSlot2.getChildren().add(iv);
                this.gotPermanentUpgradeCardSlot2.getStyleClass().clear();
                this.gotPermanentUpgradeCardSlot2.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;

            case 2:
                this.gotPermanentUpgradeCardSlot3.getChildren().clear();
                this.gotPermanentUpgradeCardSlot3.getChildren().add(iv);
                this.gotPermanentUpgradeCardSlot3.getStyleClass().clear();
                this.gotPermanentUpgradeCardSlot3.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;
        }
        return;
    }


    private void renderShopSlot(int idx, String cardName)
    {
        final ImageView iv = this.getCardRegisterSlot(ViewSupervisor.REGISTER_SLOT_WIDTH, ViewSupervisor.REGISTER_SLOT_HEIGHT, cardName, idx);

        switch (idx)
        {
            case 0:
                this.shopSlot1.getChildren().clear();
                this.shopSlot1.getChildren().add(iv);
                this.shopSlot1.getStyleClass().clear();
                this.shopSlot1.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;

            case 1:
                this.shopSlot2.getChildren().clear();
                this.shopSlot2.getChildren().add(iv);
                this.shopSlot2.getStyleClass().clear();
                this.shopSlot2.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;

            case 2:
                this.shopSlot3.getChildren().clear();
                this.shopSlot3.getChildren().add(iv);
                this.shopSlot3.getStyleClass().clear();
                this.shopSlot3.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;

            case 3:
                this.shopSlot4.getChildren().clear();
                this.shopSlot4.getChildren().add(iv);
                this.shopSlot4.getStyleClass().clear();
                this.shopSlot4.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;

            case 4:
                this.shopSlot5.getChildren().clear();
                this.shopSlot5.getChildren().add(iv);
                this.shopSlot5.getStyleClass().clear();
                this.shopSlot5.getStyleClass()
                        .add(
                                "register-slot-disabled"
                        );
                break;
        }

        return;
    }

    /**
     * Updates the register slots.
     * No re-renders must be done after this method.
     */
    private void renderRegisterSlots()
    {
        this.renderRegisterSlot(0, EGameState.INSTANCE.getRegister(0));
        this.renderRegisterSlot(1, EGameState.INSTANCE.getRegister(1));
        this.renderRegisterSlot(2, EGameState.INSTANCE.getRegister(2));
        this.renderRegisterSlot(3, EGameState.INSTANCE.getRegister(3));
        this.renderRegisterSlot(4, EGameState.INSTANCE.getRegister(4));

        return;
    }

    /**
     * Updates the got register card slots.
     * No re-renders must be done after this method.
     */
    private void renderGotRegisterCardSlots()
    {
        this.renderGotRegisterSlot(0, EGameState.INSTANCE.getGotRegister(0));
        this.renderGotRegisterSlot(1, EGameState.INSTANCE.getGotRegister(1));
        this.renderGotRegisterSlot(2, EGameState.INSTANCE.getGotRegister(2));
        this.renderGotRegisterSlot(3, EGameState.INSTANCE.getGotRegister(3));
        this.renderGotRegisterSlot(4, EGameState.INSTANCE.getGotRegister(4));
        this.renderGotRegisterSlot(5, EGameState.INSTANCE.getGotRegister(5));
        this.renderGotRegisterSlot(6, EGameState.INSTANCE.getGotRegister(6));
        this.renderGotRegisterSlot(7, EGameState.INSTANCE.getGotRegister(7));
        this.renderGotRegisterSlot(8, EGameState.INSTANCE.getGotRegister(8));

        return;
    }

    private void renderShopSlots(){
        this.renderShopSlot(0, null);
        this.renderShopSlot(1, null);
        this.renderShopSlot(2, null);
        this.renderShopSlot(3, null);
        this.renderShopSlot(4, null);

    }

    private void renderGotUpgradeCardSlots(){
       renderGotPermanentUpgradeCardSlots();
       renderGotTemporaryUpgradeCardSlots();
    }

    private void renderGotPermanentUpgradeCardSlots(){
        this.renderGotPermanentUpgradeCardSlot(0, null);
        this.renderGotPermanentUpgradeCardSlot(1, null);
        this.renderGotPermanentUpgradeCardSlot(2, null);
    }
    private void renderGotTemporaryUpgradeCardSlots(){
        this.renderGotTemporaryUpgradeCardSlot(0, null);
        this.renderGotTemporaryUpgradeCardSlot(1, null);
        this.renderGotTemporaryUpgradeCardSlot(2, null);
    }

    // endregion HUD Footer

    /**
     * Updates every dependency of the header.
     * No re-renders must be done after this method.
     * */
    private void renderHUDHeader()
    {
        this.renderPhaseTitle();
        this.renderGameStateDescription();

        return;
    }

    /**
     * Updates every dependency of the footer.
     * No re-renders must be done after this method.
     * */
    private void renderHUDFooter()
    {
        this.renderRegisterSlots();
        this.renderGotRegisterCardSlots();
        this.renderShopSlots();
        this.renderGotUpgradeCardSlots();
        return;
    }

    /**
     * Displays every player in the lobby with some stats.
     * No re-renders must be done after this method.
     * */
    private void renderPlayerInformationArea()
    {
        this.playerContainer.getChildren().clear();

        for (RemotePlayer rp : EGameState.INSTANCE.getRemotePlayers())
        {
            Label figureName = new Label(EGameState.FIGURE_NAMES[rp.getFigureID()]);
            figureName.getStyleClass().add("player-box-text");

            Label playerName = new Label(rp.getPlayerName());
            if (rp.getPlayerID() == EClientInformation.INSTANCE.getPlayerID())
            {
                playerName.setText(String.format("%s (You)", rp.getPlayerName()));
            }
            playerName.getStyleClass().add("player-box-text");

            Label energyCubes = new Label("Energy: " + rp.getEnergyCubes());
            energyCubes.getStyleClass().add("player-box-text");

            VBox v = new VBox(figureName, playerName, energyCubes);
            v.getStyleClass().add("player-box");
            v.getStyleClass().add(String.format("player-box-%s", rp == EGameState.INSTANCE.getCurrentPlayer() ? "active" : "inactive" ));
            if(EGameState.INSTANCE.getCurrentPhase().equals(EGamePhase.PROGRAMMING)){
                v.getStyleClass().add(String.format("player-box-%s", rp.hasSelectionFinished() ? "selected" : "inSelection"));
            }
            v.getStyleClass().add("player-box");

            this.playerContainer.getChildren().add(v);

            continue;
        }

        return;
    }

    /**
     * Super method for all HUD updates. Will rerender everything on the HUD.
     * No re-renders must be done after this method.
     * */
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

    /**
     * Updates global variable used to render the course. They may change based on
     * the current board and size of the client's window.
     * */
    private void updateGlobalVariables()
    {
        /* We can do this because even if there is no tile, it must always be annotated with a null json object. */
        this.files = EGameState.INSTANCE.getCurrentServerCourseJSON().toList().size();
        this.ranks = EGameState.INSTANCE.getCurrentServerCourseJSON().getJSONArray(0).toList().size();

        this.tiles = new Tile[this.files][this.ranks];
        for (int i = 0; i < this.files; i++)
        {
            for (int j = 0; j < this.ranks; j++)
            {
                Tile t = new Tile(EGameState.INSTANCE.getCurrentServerCourseJSON().getJSONArray(i).getJSONArray(j));
                t.setTranslateX(i);
                t.setTranslateY(j);
                tiles[i][j] = t;
                continue;
            }

            continue;
        }

        // TODO Only works with rectangle courses
        this.minXTranslation = this.tiles[0][0].getTranslateX() * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2;
        this.maxXTranslation = this.tiles[this.files - 1][0].getTranslateX() * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2;
        this.centralXTranslation = ((this.courseScrollPane.getWidth() - this.maxXTranslation) - this.minXTranslation) / 2 - (double) this.tileDimensions / 2;

        return;
    }

    private void setStyleOfCourseViewContent()
    {
        double viewWidth = this.files * this.tileDimensions + ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL;
        double viewHeight = this.ranks * this.tileDimensions + ViewSupervisor.VIRTUAL_SPACE_VERTICAL;
        this.courseScrollPaneContent.setStyle(String.format("-fx-background-color: #000000ff; -fx-min-width: %spx; -fx-min-height: %spx;", (int) viewWidth, (int) viewHeight));

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

    // endregion Helper Methods

    /**
     * Renders the actual course board. This method will take some time to execute. Do not call this method if you only
     * want to update a small part of the view. Use the specific update methods instead.
     *
     * <p>It Will render everything that is static on the course board.
     *
     * <p>You will have to rerender the whole course view after this method (like player positions).
     */
    private void renderCourseBoard()
    {
        this.courseScrollPaneContent.getChildren().clear();

        if (EGameState.INSTANCE.getCurrentServerCourseJSON() == null)
        {
            l.warn("No course data available.");
            return;
        }

        this.updateGlobalVariables();
        this.setStyleOfCourseViewContent();

        for (int i = 0; i < this.files; i++)
        {
            for (int j = 0; j < this.ranks; j++)
            {
                Tile t = this.tiles[i][j];
                AnchorPane AP = new AnchorPane();
                for (int k = t.getImageViews().length - 1; k >= 0; k--)
                {
                    ImageView iv = t.getImageViews()[k];
                    iv.setFitHeight(this.tileDimensions);
                    iv.setFitWidth(this.tileDimensions);
                    AP.getChildren().add(iv);
                    continue;
                }
                this.renderOnPosition(AP, new RCoordinate(i, j));

                if (t.isClickable() && EGameState.INSTANCE.getCurrentPhase() == EGamePhase.REGISTRATION)
                {
                    AP.setOnMouseClicked(e ->
                    {
                        l.info("User clicked on tile. Checking if valid move.");

                        if (EGameState.INSTANCE.getCurrentPhase() == EGamePhase.REGISTRATION)
                        {
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
                                return;
                            }

                            l.info("User wants to set starting position.");
                            // TODO Some kind of validation.
                            new SetStartingPointModel(t.getTranslateX(), t.getTranslateY()).send();
                            this.bClickedOnTile = true;

                            return;
                        }

                        return;
                    });

                    // Since there is AFAIK no "::before" or "::after" css
                    // support for JavaFX. We add a pseudo elem here.
                    AnchorPane after = new AnchorPane();

                    after.getStyleClass().add("tile-after");

                    after.setPrefWidth(this.tileDimensions);
                    after.setPrefHeight(this.tileDimensions);

                    AP.getChildren().add(after);
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
        for (RemotePlayer RP : EGameState.INSTANCE.getRemotePlayers())
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

            RP.getRobotView().setPosition(RP.getStartingPosition(), true, false);
            RP.getRobotView().renderPosition();

            continue;
        }

        return;
    }

    /**
     * Updates the whole course view. This is very costly. Do not call this method if you only want to update a small
     * part of the view. Use the specific update methods instead.
     * No re-renders must be done after this method.
     * */
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

    public void onChatMsgReceived(int sourceID, String msg, boolean bIsPrivate)
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

    private boolean isChatMsgValid(String token)
    {
        return !token.isEmpty() && token.length() <= EGameState.MAX_CHAT_MESSAGE_LENGTH;
    }

    private boolean isChatMsgACommand(String token)
    {
        return token.startsWith(ChatMsgModel.COMMAND_PREFIX);
    }

    private String getChatCommand(String token)
    {
        if (!token.contains(" "))
        {
            return token.substring(1);
        }

        return token.substring(1, token.indexOf(" "));
    }

    public double calcXTranslation(final int file)
    {
        return this.centralXTranslation < 0 ? file * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2 : file * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2 + this.centralXTranslation;
    }

    public double calcYTranslation(final int rank)
    {
        return rank * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_VERTICAL / 2;
    }

    // endregion Getters and Setters

}
