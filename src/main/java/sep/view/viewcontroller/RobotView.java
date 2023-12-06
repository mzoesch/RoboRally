package sep.view.viewcontroller;

import sep.view.clientcontroller.RemotePlayer;
import sep.view.lib.RCoordinate;
import sep.view.scenecontrollers.GameJFXController;
import sep.view.lib.RRotation;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RobotView
{
    private static final Logger l = LogManager.getLogger(RobotView.class);

    private final RemotePlayer possessor;

    // TODO To Remove. We want to calculate the pos with the given iv maybe?
    RCoordinate position;
    RRotation rotation;

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

    public void setPosition(RCoordinate c)
    {
        this.setPosition(c, false, true);
        return;
    }

    public void setPosition(RCoordinate c, boolean bOverrideOldRotation, boolean bUpdateInView)
    {
        this.position = c;

        if (bOverrideOldRotation)
        {
            this.rotation = new RRotation(RRotation.NORTH);
        }

        if (bUpdateInView)
        {
            ViewSupervisor.updatePlayerTransforms();
        }

        return;
    }

    /** With animations. */
    public void moveToPosition(RCoordinate c)
    {
        return;
    }

    private void generateIMG()
    {
        this.IV = new ImageView();
        this.IV.setFitHeight(( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getCurrentTileDimensions());
        this.IV.setFitWidth(( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getCurrentTileDimensions());
        this.IV.setPreserveRatio(true);
        this.IV.setSmooth(true);
        this.IV.setCache(true);
        this.IV.setImage(this.getRobotImage());

        this.AP = new AnchorPane();
        this.AP.getChildren().add(this.IV);

        return;
    }

    private void rotateIMG()
    {
        this.IV.setRotate(this.rotation.rotation());
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

        this.rotateIMG();

        ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).renderOnPosition(this.AP, this.position);

        return;
    }

    public void addRotation(String r)
    {
        if (this.rotation == null)
        {
            this.rotation = new RRotation(RRotation.NORTH);
        }

        this.rotation = this.rotation.addRotation(r);
        return;
    }

    public int getRotation()
    {
        return this.rotation.rotation();
    }

}
