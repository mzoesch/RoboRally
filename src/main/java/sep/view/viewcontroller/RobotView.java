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
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class RobotView
{
    private static final Logger l = LogManager.getLogger(RobotView.class);

    private final RemotePlayer possessor;

    // TODO To Remove. We want to calculate the pos with the given iv maybe?
    RCoordinate position;
    RRotation rotation;

    ImageView iv;
    /** The actual item in the view. */
    AnchorPane AP;
    AnchorPane ap;

    public RobotView(RemotePlayer possessor)
    {
        super();
        this.possessor = possessor;
        this.position = null;
        return;
    }

        this.iv = null;
        this.ap = null;

    public void setPosition(RCoordinate c)
    {
        this.setPosition(c, false, true);
        return;
    }

    /**
     * Sets the position of the robot. This method should only be used to override the old position. For example, if
     * this robot should be teleported to a new position (e.g., when the robot is being rebooted). For a smooth
     * transition, use {@link #lerpTo(RCoordinate)} instead.
     *
     * @param c                    The new position.
     * @param bOverrideOldRotation If true, the rotation will be set to NORTH.
     * @param bUpdateInView        If true, the view will be notified to update itself.
     */
    public void setPosition(final RCoordinate c, final boolean bOverrideOldRotation, final boolean bUpdateInView)
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

    /**
     * Will lerp the robot to the given position. A position must be set before calling this method with {@link
     * #setPosition(RCoordinate, boolean, boolean)}.
     *
     * @param c The new position.
     */
    public void lerpTo(final RCoordinate c)
    {
        this.position = c;

        final double tX = ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).calcXTranslation(c.x());
        final double tY = ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).calcYTranslation(c.y());

        final Timeline t = new Timeline();
        final KeyFrame kf = new KeyFrame(Duration.seconds(2), new KeyValue(this.ap.translateXProperty(), tX), new KeyValue(this.ap.translateYProperty(), tY));
        t.getKeyFrames().add(kf);

        t.play();

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
