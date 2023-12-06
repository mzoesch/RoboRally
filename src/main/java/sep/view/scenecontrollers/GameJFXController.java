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

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.image.ImageView;
import java.util.Objects;
import javafx.scene.control.ScrollPane;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

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
        this.courseScrollPane.setFitToWidth(true);
        this.courseScrollPane.setFitToHeight(true);
        this.courseScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.courseScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        this.courseScrollPane.widthProperty().addListener((obs, oldVal, newVal) ->
        {
            // TODO Only update translations not the whole course.
            this.renderCourse();
            return;
        });

        this.renderView();

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

        return;
    }

    private void initializeButtonActions()
    {

        {

        this.registerSlot1.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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

            EGameState.INSTANCE.setRegister(0, this.gotRegisterSlotClicked);

            this.gotRegisterSlotClicked = GameJFXController.INVALID_GOT_REGISTER_SLOT;
            this.renderHUDFooter();

            new SelectedCardModel(0, EGameState.INSTANCE.getRegister(0)).send();

            return;
        });

        this.registerSlot2.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.registerSlot3.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.registerSlot4.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.registerSlot5.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        }

        {

        this.gotRegisterCardSlot1.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.gotRegisterCardSlot2.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.gotRegisterCardSlot3.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.gotRegisterCardSlot4.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.gotRegisterCardSlot5.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.gotRegisterCardSlot6.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.gotRegisterCardSlot7.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.gotRegisterCardSlot8.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        this.gotRegisterCardSlot9.setOnMouseClicked(e ->
        {
            if (EGameState.INSTANCE.getCurrentPhase() != EGamePhase.PROGRAMMING)
            {
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
        });

        }

        return;
    }

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
        if (EGameState.INSTANCE.getCurrentPlayer() == null)
        {
            this.UIHeaderGameStateDescriptionLabel.setText("Waiting for server.");
            return;
        }

        switch (EGameState.INSTANCE.getCurrentPhase())
        {
            case REGISTRATION:
                this.UIHeaderGameStateDescriptionLabel.setText(String.format("Waiting for %s to set their starting position.", EGameState.INSTANCE.getCurrentPlayer().getPlayerName()));
                return;

            case UPGRADE:
                this.UIHeaderGameStateDescriptionLabel.setText("Upgrade Phase");
                return;

            case PROGRAMMING:
                this.UIHeaderGameStateDescriptionLabel.setText("Programming Phase");
                return;

            case ACTIVATION:
                this.UIHeaderGameStateDescriptionLabel.setText("Activation Phase");
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
                    ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                        ? EGameState.INSTANCE.getRegister(0) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : EGameState.INSTANCE.getRegister(0) == null
                            ? "register-slot-available"
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
                    ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                        ? EGameState.INSTANCE.getRegister(1) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : EGameState.INSTANCE.getRegister(1) == null
                            ? "register-slot-available"
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
                    ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                        ? EGameState.INSTANCE.getRegister(2) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : EGameState.INSTANCE.getRegister(2) == null
                            ? "register-slot-available"
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
                    ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                        ? EGameState.INSTANCE.getRegister(3) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : EGameState.INSTANCE.getRegister(3) == null
                            ? "register-slot-available"
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
                    ? this.gotRegisterSlotClicked == GameJFXController.INVALID_GOT_REGISTER_SLOT
                        ? EGameState.INSTANCE.getRegister(4) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : EGameState.INSTANCE.getRegister(4) == null
                            ? "register-slot-available"
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
                    ? this.gotRegisterSlotClicked != idx
                        ? EGameState.INSTANCE.getGotRegister(0) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : "register-slot-active"
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
                    ? this.gotRegisterSlotClicked != idx
                        ? EGameState.INSTANCE.getGotRegister(1) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : "register-slot-active"
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
                    ? this.gotRegisterSlotClicked != idx
                        ? EGameState.INSTANCE.getGotRegister(2) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : "register-slot-active"
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
                    ? this.gotRegisterSlotClicked != idx
                        ? EGameState.INSTANCE.getGotRegister(3) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : "register-slot-active"
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
                    ? this.gotRegisterSlotClicked != idx
                        ? EGameState.INSTANCE.getGotRegister(4) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : "register-slot-active"
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
                    ? this.gotRegisterSlotClicked != idx
                        ? EGameState.INSTANCE.getGotRegister(5) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : "register-slot-active"
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
                    ? this.gotRegisterSlotClicked != idx
                        ? EGameState.INSTANCE.getGotRegister(6) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : "register-slot-active"
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
                    ? this.gotRegisterSlotClicked != idx
                        ? EGameState.INSTANCE.getGotRegister(7) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : "register-slot-active"
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
                    ? this.gotRegisterSlotClicked != idx
                        ? EGameState.INSTANCE.getGotRegister(8) == null
                            ? "register-slot-disabled"
                            : "register-slot"
                        : "register-slot-active"
                    : "register-slot-disabled"
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
            playerName.getStyleClass().add("player-box-text");

            VBox v = new VBox(figureName, playerName);
            v.getStyleClass().add("player-box");
            v.getStyleClass().add(String.format("player-box-%s", rp == EGameState.INSTANCE.getCurrentPlayer() ? "active" : "inactive" ));


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

    public void renderOnPosition(AnchorPane AP, RCoordinate c)
    {
        AP.getStyleClass().add("tile");

        double xTranslation = c.x() * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2;
        AP.setTranslateX(this.centralXTranslation < 0 ? xTranslation : xTranslation + this.centralXTranslation);
        AP.setTranslateY(c.y() * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_VERTICAL / 2);

        if (!this.courseScrollPaneContent.getChildren().contains(AP))
        {
            this.courseScrollPaneContent.getChildren().add(AP);
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

    // endregion Update View Methods from outside

    // region Getters and Setters

    public int getCurrentTileDimensions()
    {
        return this.tileDimensions;
    }

    // endregion Getters and Setters

}
