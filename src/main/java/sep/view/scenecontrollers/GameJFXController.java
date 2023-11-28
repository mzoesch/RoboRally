package sep.view.scenecontrollers;

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

    @FXML private HBox playerContainer;
    @FXML private AnchorPane courseContainer;

    @FXML
    private void initialize()
    {
        this.updateView();
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
        this.courseContainer.getChildren().clear();

        if (EGameState.INSTANCE.getCurrentServerCourseJSON() == null)
        {
            l.warn("No course data available.");
            return;
        }

        /* This is not safe at all. This only works with rectangle courses. */
        int countFiles = EGameState.INSTANCE.getCurrentServerCourseJSON().getJSONArray(0).toList().size();
        int countRanks = EGameState.INSTANCE.getCurrentServerCourseJSON().toList().size();

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

        /* Render */
        for (int i = 0; i < countFiles; i++)
        {
            for (int j = 0; j < countRanks; j++)
            {
                Tile t = tiles[i][j];
                System.out.println(t.getModifier(0).getType());

                AnchorPane a = new AnchorPane();
                a.getStyleClass().add("tile");
                a.setTranslateX(t.getTranslateX() * ViewLauncher.TILE_DIMENSIONS);
                a.setTranslateY(t.getTranslateY() * ViewLauncher.TILE_DIMENSIONS);

                for (int k = t.getImageViews().length - 1; k >= 0; k--)
                {
                    ImageView iv = t.getImageViews()[k];
                    iv.setFitHeight(ViewLauncher.TILE_DIMENSIONS);
                    iv.setFitWidth(ViewLauncher.TILE_DIMENSIONS);
                    a.getChildren().add(iv);
                    continue;
                }

                this.courseContainer.getChildren().add(a);

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

    // endregion Update View Methods

}
