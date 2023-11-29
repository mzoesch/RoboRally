package sep.view.scenecontrollers;

import javafx.scene.control.ScrollPane;
import sep.view.clientcontroller.EGameState;
import sep.view.clientcontroller.RemotePlayer;
import sep.view.viewcontroller.Tile;
import sep.view.viewcontroller.TileModifier;
import sep.view.viewcontroller.ViewLauncher;

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

    @FXML private AnchorPane masterContainer;
    @FXML private HBox playerContainer;
    @FXML private AnchorPane courseContainer;
    @FXML private ScrollPane courseScrollPane;
    @FXML private AnchorPane courseScrollPaneContent;

    private int tileDimensions;
    private static final int resizeAmount = 10;

    public GameJFXController()
    {
        super();
        this.tileDimensions = ViewLauncher.TILE_DIMENSIONS;
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
            this.playerContainer.getChildren().add(v);
            continue;
        }

        return;
    }

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

                this.courseScrollPaneContent.getChildren().add(a);

                continue;
            }

            continue;
        }

        return;
    }

    private void updateView()
    {
        this.updatePlayers();
        this.updateCourse();
        return;
    }

    // region Update View Methods

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

    // endregion Update View Methods

}
