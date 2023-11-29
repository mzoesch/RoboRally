package sep.view.scenecontrollers;

import javafx.scene.control.ScrollPane;
import sep.view.clientcontroller.EGameState;
import sep.view.clientcontroller.RemotePlayer;
import sep.view.viewcontroller.Tile;
import sep.view.viewcontroller.TileModifier;
import sep.view.viewcontroller.ViewLauncher;
import sep.view.clientcontroller.EClientInformation;
import sep.view.json.game.SetStartingPointModel;

import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.image.ImageView;

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

    private int tileDimensions;
    private static final int resizeAmount = 10;

    private boolean bClickedOnTile = false;

    public GameJFXController()
    {
        super();
        this.tileDimensions = ViewLauncher.TILE_DIMENSIONS;
        this.bClickedOnTile = false;
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
            this.updateCourse();
            return;
        });

        this.updateView();

        this.masterContainer.setOnKeyPressed(e ->
        {
            // TODO Add min max
            switch (e.getCode())
            {
                /* Zoom in. */
                case W:
                    this.tileDimensions += GameJFXController.resizeAmount;
                    this.updateCourse();
                    break;

                /* Zoom out. */
                case S:
                    this.tileDimensions -= GameJFXController.resizeAmount;
                    this.updateCourse();
                    break;
            }
            return;

        });

        return;
    }

    /** Update the view player area. */
    private void updatePlayers()
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

    // TODO Updates the whole course view. Not efficient. We need to split this method up.
    //      Many requests only want to do small updates, so we do not need to update the whole
    //      course view.
    private void updateCourse()
    {
        this.courseScrollPaneContent.getChildren().clear();

        if (EGameState.INSTANCE.getCurrentServerCourseJSON() == null)
        {
            l.warn("No course data available.");
            return;
        }

        /* This is not safe at all. This only works with rectangle courses. */
        int countFiles = EGameState.INSTANCE.getCurrentServerCourseJSON().getJSONArray(0).toList().size();
        int countRanks = EGameState.INSTANCE.getCurrentServerCourseJSON().toList().size();
        this.setStyleOfCourseViewContent();

        Tile[][] tiles = new Tile[countFiles][countRanks];

        /* Generate */
        for (int i = 0; i < countFiles; i++)
        {
            for (int j = 0; j < countRanks; j++)
            {
                Tile t = new Tile(EGameState.INSTANCE.getCurrentServerCourseJSON().getJSONArray(j).getJSONArray(i));
                for (int k = 0; k < t.getModifierSize(); k++)
                {
                    TileModifier tm = t.getModifier(k);
                    continue;
                }

                t.setTranslateX(i);
                t.setTranslateY(j);

                tiles[i][j] = t;

                continue;
            }

            continue;
        }

        // TODO Only works with rectangle courses
        final double minXTranslate = tiles[0][0].getTranslateX() * this.tileDimensions + (double) ViewLauncher.VIRTUAL_SPACE_HORIZONTAL / 2;
        final double maxXTranslate = tiles[countFiles - 1][0].getTranslateX() * this.tileDimensions + (double) ViewLauncher.VIRTUAL_SPACE_HORIZONTAL / 2;
        final double centerXTranslate = ((this.courseScrollPane.getWidth() - maxXTranslate) - minXTranslate) / 2 - (double) this.tileDimensions / 2;

        /* Render */
        for (int i = 0; i < countFiles; i++)
        {
            for (int j = 0; j < countRanks; j++)
            {
                Tile t = tiles[i][j];

                AnchorPane a = new AnchorPane();
                a.getStyleClass().add("tile");

                double xTranslate = t.getTranslateX() * this.tileDimensions + (double) ViewLauncher.VIRTUAL_SPACE_HORIZONTAL / 2;
                a.setTranslateX(centerXTranslate < 0 ? xTranslate : xTranslate + centerXTranslate);
                a.setTranslateY(t.getTranslateY() * this.tileDimensions + (double) ViewLauncher.VIRTUAL_SPACE_VERTICAL / 2);

                for (int k = t.getImageViews().length - 1; k >= 0; k--)
                {
                    ImageView iv = t.getImageViews()[k];
                    iv.setFitHeight(this.tileDimensions);
                    iv.setFitWidth(this.tileDimensions);
                    a.getChildren().add(iv);
                    continue;
                }

                if (t.isClickable())
                {
                    a.setOnMouseClicked(e ->
                    {
                        l.info("User clicked on tile. Checking if valid move.");

                        if (EGameState.INSTANCE.getCurrentPhase() == 0)
                        {
                            if (EGameState.INSTANCE.getCurrentPlayer().getPlayerID() != EClientInformation.INSTANCE.getPlayerID())
                            {
                                return;
                            }

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

                    a.getChildren().add(after);
                }

                this.courseScrollPaneContent.getChildren().add(a);

                continue;
            }

            continue;
        }

        return;
    }

    /** Updates the UI Phase Title in the Header. */
    private void updatePhaseTitle()
    {
        this.UIHeaderPhaseLabel.setText(EGameState.PHASE_NAMES[EGameState.INSTANCE.getCurrentPhase()]);
        return;
    }

    private void updateGameStateDescription()
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

    /** Updates every dependency of the header. */
    private void updateUIHeader()
    {
        this.updatePhaseTitle();
        this.updateGameStateDescription();
        return;
    }

    private void updateView()
    {
        this.updatePlayers();
        this.updateCourse();
        this.updatePhaseTitle();
        return;
    }

    // region Update View Methods

    public void onPhaseUpdate()
    {
        Platform.runLater(() ->
        {
            this.updateUIHeader();
            return;
        });

        return;
    }

    public void onPlayerUpdate()
    {
        Platform.runLater(() ->
        {
            this.updatePlayers();
            this.updateUIHeader();
            return;
        });

        return;
    }

    public void onPlayerAdded()
    {
        Platform.runLater(() ->
        {
            this.updatePlayers();
            return;
        });

        return;
    }

    public void onCourseUpdate()
    {
        Platform.runLater(() ->
        {
            this.updateView();
            return;
        });

        return;
    }

    // endregion Update View Methods

    private void setStyleOfCourseViewContent()
    {
        // With current scale
        double viewWidth = this.getCountFiles() * this.tileDimensions + ViewLauncher.VIRTUAL_SPACE_HORIZONTAL;
        double viewHeight = this.getCountRanks() * this.tileDimensions + ViewLauncher.VIRTUAL_SPACE_VERTICAL;
        this.courseScrollPaneContent.setStyle(String.format("-fx-background-color: #000000ff; -fx-min-width: %spx; -fx-min-height: %spx;", (int) viewWidth, (int) viewHeight));

        return;
    }

    private int getCountFiles()
    {
        return EGameState.INSTANCE.getCurrentServerCourseJSON().getJSONArray(0).toList().size();
    }

    private int getCountRanks()
    {
        return EGameState.INSTANCE.getCurrentServerCourseJSON().toList().size();
    }

}
