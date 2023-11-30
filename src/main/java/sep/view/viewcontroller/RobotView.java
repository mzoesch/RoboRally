package sep.view.viewcontroller;

import sep.view.clientcontroller.RemotePlayer;
import sep.view.lib.Coordinate;
import sep.view.scenecontrollers.GameJFXController;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class RobotView
{
    private final RemotePlayer possessor;

    // TODO To Remove. We want to calculate the pos with the given iv maybe?
    Coordinate position;
    ImageView IV;
    /** The actual item in the view. */
    AnchorPane AP;

    public RobotView(RemotePlayer possessor)
    {
        super();
        this.possessor = possessor;
        this.position = null;
        return;
    }

    public Image getRobotImage()
    {
        return Tile.getRobotImage(this.possessor.getFigureID());
    }

    public void setPosition(Coordinate c)
    {
        this.setPosition(c, true);
        return;
    }

    public void setPosition(Coordinate c, boolean bUpdateInView)
    {
        this.position = c;
        if (bUpdateInView)
        {
            ViewLauncher.updatePlayerPosition();
        }
        return;
    }

    /** With animations. */
    public void moveToPosition(Coordinate c)
    {
        return;
    }

    private void generateIMG()
    {
        this.IV = new ImageView();
        this.IV.setFitHeight(( (GameJFXController) ViewLauncher.getSceneController().getCurrentController() ).getCurrentTileDimensions());
        this.IV.setFitWidth(( (GameJFXController) ViewLauncher.getSceneController().getCurrentController() ).getCurrentTileDimensions());
        this.IV.setPreserveRatio(true);
        this.IV.setSmooth(true);
        this.IV.setCache(true);
        this.IV.setImage(this.getRobotImage());

        this.AP = new AnchorPane();
        this.AP.getChildren().add(this.IV);

        return;
    }

    public boolean hasPosition()
    {
        return this.position != null;
    }

    /** Will update the position in the view. */
    public void renderPosition()
    {
        if (this.IV == null || this.AP == null)
        {
            this.generateIMG();
        }

        ( (GameJFXController) ViewLauncher.getSceneController().getCurrentController() ).renderOnPosition(this.AP, this.position);

        return;
    }

}
