package sep.view.scenecontrollers;

import sep.view.clientcontroller.EGameState;
import sep.view.clientcontroller.RemotePlayer;
import sep.view.viewcontroller.Tile;
import sep.view.viewcontroller.ViewSupervisor;
import sep.view.lib.Coordinate;
import sep.view.json.game.SetStartingPointModel;
import sep.view.viewcontroller.TileModifier;

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

    private int tileDimensions;
    private static final int resizeAmount = 10;

    private boolean bClickedOnTile;

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
            l.info("Scrolling to center.");
            this.courseScrollPane.setHvalue(0.5);
            this.courseScrollPane.setVvalue(0.5);
            return;
        });
        p.play();

        return;
    }

    // region Rendering

    // region Head Up Display

    // region HUD Header

    /** Updates the UI Phase Title in the Header. */
    private void renderPhaseTitle()
    {
        this.UIHeaderPhaseLabel.setText(EGameState.PHASE_NAMES[EGameState.INSTANCE.getCurrentPhase()]);
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
            case 0:
                this.UIHeaderGameStateDescriptionLabel.setText(String.format("Waiting for %s to set their starting position.", EGameState.INSTANCE.getCurrentPlayer().getPlayerName()));
                return;

            case 1:
                this.UIHeaderGameStateDescriptionLabel.setText("PHASE 1");
                return;

            case 2:
                this.UIHeaderGameStateDescriptionLabel.setText("PHASE 2");
                return;

            case 3:
                this.UIHeaderGameStateDescriptionLabel.setText("PHASE 3");
                return;
        }

        this.UIHeaderGameStateDescriptionLabel.setText("Unknown game state.");
        return;
    }

    // endregion HUD Header

    // region HUD Footer

    private ImageView getEmptyRegisterSlot()
    {
        ImageView iv = new ImageView();
        iv.setFitWidth(ViewSupervisor.REGISTER_SLOT_WIDTH);
        iv.setFitHeight(ViewSupervisor.REGISTER_SLOT_HEIGHT);
        iv.getStyleClass().add("register-slot");
        iv.setImage(TileModifier.getImage("EmptyRegisterSlot"));
        return iv;
    }

    /**
     * @param idx       Index of the register slot.
     * @param cardName  Name of the card to render. Pass null to render an empty slot.
     */
    private void renderRegisterSlot(int idx, String cardName)
    {
        if (cardName == null)
        {
            cardName = "EmptyRegisterSlot";
        }

        ImageView iv = new ImageView();
        iv.setFitWidth(ViewSupervisor.REGISTER_SLOT_WIDTH);
        iv.setFitHeight(ViewSupervisor.REGISTER_SLOT_HEIGHT);
        iv.getStyleClass().add("register-slot");
        iv.setImage(TileModifier.getImage(cardName));

        switch (idx)
        {
            case 0:
                this.registerSlot1.getChildren().clear();
                this.registerSlot1.getChildren().add(iv);
                break;

            case 1:
                this.registerSlot2.getChildren().clear();
                this.registerSlot2.getChildren().add(iv);
                break;

            case 2:
                this.registerSlot3.getChildren().clear();
                this.registerSlot3.getChildren().add(iv);
                break;

            case 3:
                this.registerSlot4.getChildren().clear();
                this.registerSlot4.getChildren().add(iv);
                break;

            case 4:
                this.registerSlot5.getChildren().clear();
                this.registerSlot5.getChildren().add(iv);
                break;
        }

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
//        this.renderRegisterSlot(0, Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getRegisterSlot(0));
//        this.renderRegisterSlot(1, Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getRegisterSlot(1));
//        this.renderRegisterSlot(2, Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getRegisterSlot(2));
//        this.renderRegisterSlot(3, Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getRegisterSlot(3));
//        this.renderRegisterSlot(4, Objects.requireNonNull(EGameState.INSTANCE.getClientRemotePlayer()).getRegisterSlot(4));
        this.renderRegisterSlot(0, null);
        this.renderRegisterSlot(1, null);
        this.renderRegisterSlot(2, null);
        this.renderRegisterSlot(3, null);
        this.renderRegisterSlot(4, null);

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

    public void renderOnPosition(AnchorPane AP, Coordinate c)
    {
        AP.getStyleClass().add("tile");

        double xTranslation = c.getX() * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_HORIZONTAL / 2;
        AP.setTranslateX(this.centralXTranslation < 0 ? xTranslation : xTranslation + this.centralXTranslation);
        AP.setTranslateY(c.getY() * this.tileDimensions + (double) ViewSupervisor.VIRTUAL_SPACE_VERTICAL / 2);

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
                this.renderOnPosition(AP, new Coordinate(i, j));

                if (t.isClickable())
                {
                    AP.setOnMouseClicked(e ->
                    {
                        l.info("User clicked on tile. Checking if valid move.");

                        if (EGameState.INSTANCE.getCurrentPhase() == 0)
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

    // TODO Rotation of the robot
    /**
     * Updates player positions on the course view.
     * No re-renders must be done after this method.
     */
    private void renderPlayerPositions()
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

            RP.getRobotView().setPosition(RP.getStartingPosition(), false);
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
        this.renderPlayerPositions();

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

    public void onPlayerPositionUpdate()
    {
        Platform.runLater(() ->
        {
            this.renderPlayerPositions();
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
