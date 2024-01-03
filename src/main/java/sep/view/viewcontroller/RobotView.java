package sep.view.viewcontroller;

import sep.view.clientcontroller.RemotePlayer;
import sep.view.lib.RCoordinate;
import sep.view.lib.Types;
import sep.view.lib.ERotation;
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
import java.util.ArrayList;

public class RobotView
{
    private static final Logger l = LogManager.getLogger(RobotView.class);

    private final RemotePlayer possessor;

    // TODO To Remove. We want to calculate the pos with the given iv maybe?
    RCoordinate position;
    RRotation rotation;

    ImageView iv;
    /** The actual item in the view. */
    AnchorPane ap;

    boolean bIsNextRotationLerp;

    public RobotView(RemotePlayer possessor)
    {
        super();
        this.possessor = possessor;
        this.position = null;
        this.rotation = null;

        this.iv = null;
        this.ap = null;

        this.bIsNextRotationLerp = false;

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
            this.bIsNextRotationLerp = false;
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

    private void rotateIV()
    {
        this.iv.setRotate(this.rotation.rotation());
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

    public void addRotationWithLerp(final String r)
    {
        if (!this.bIsNextRotationLerp)
        {
            this.addRotation(r);
            ViewSupervisor.updatePlayerTransforms();
            this.bIsNextRotationLerp = true;
            return;
        }

        /* The initial position of the robot. */
        final boolean bLerp = this.rotation != null;
        if (this.rotation == null)
        {
            this.rotation = new RRotation(RRotation.NORTH);
        }
        final RRotation oRot = this.rotation;
        this.rotation = this.rotation.addRotation(r);

        if (!bLerp)
        {
            ViewSupervisor.updatePlayerTransforms();
            return;
        }

        final Timeline t = new Timeline();
        final KeyFrame kf = new KeyFrame(Duration.seconds(2), new KeyValue(this.iv.rotateProperty(), this.rotation.rotation()));
        t.getKeyFrames().add(kf);

        t.play();

        return;
    }

    /** Will update the position in the view. Must be called from the JFX Thread. */
    public void renderPosition()
    {
        if (this.iv == null || this.ap == null)
        {
            this.generateIMG();
        }

        this.rotateIV();

        ( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).renderOnPosition(this.ap, this.position);

        return;
    }

    // region Getters and Setters

    private void generateIMG()
    {
        this.iv = new ImageView();
        this.iv.setFitHeight(( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getCurrentTileDimensions());
        this.iv.setFitWidth(( (GameJFXController) ViewSupervisor.getSceneController().getCurrentController() ).getCurrentTileDimensions());
        this.iv.setPreserveRatio(true);
        this.iv.setSmooth(true);
        this.iv.setCache(true);
        this.iv.setImage(this.getRobotImage());

        this.ap = new AnchorPane();
        this.ap.getChildren().add(this.iv);

        return;
    }

    public boolean hasPosition()
    {
        return this.position != null;
    }

    public int getRotation()
    {
        return this.rotation.rotation();
    }

    public Image getRobotImage()
    {
        return Tile.getRobotImage(this.possessor.getFigure().i);
    }

    public RCoordinate getPosition()
    {
        return this.position;
    }

    public RLaserMask[] getLaserAffectedTiles(final Tile[][] tiles, final int count)
    {
        final Tile t                          = tiles[this.position.x()][this.position.y()];
        final ERotation rot                   = this.rotation.toEnum();
        final ArrayList<RLaserMask> masks     = new ArrayList<RLaserMask>();

        System.out.printf("Player rotation: %s\n", rot);

        if (rot == null)
        {
            l.warn("Tried to animate a robot shooting with an invalid rotation.");
            return masks.toArray(new RLaserMask[0]);
        }

        masks.add(new RLaserMask(t, rot, count));

        if (t.isNotTraversable(rot, false))
        {
            return masks.toArray(new RLaserMask[0]);
        }

        while (true)
        {
            final RCoordinate toCheck = masks.get(masks.size() - 1).t().getTileLocation().getNeighbour(rot);

            if (toCheck == null)
            {
                break;
            }

            if (!RCoordinate.exists(toCheck, tiles))
            {
                break;
            }

            masks.add(new RLaserMask(tiles[toCheck.x()][toCheck.y()], rot, count));

            if (tiles[masks.get(masks.size() - 1).t().getTileLocation().x()][masks.get(masks.size() - 1).t().getTileLocation().y()].isNotTraversable(rot, false))
            {
                /* We may not shoot inside the Antenna. */
                if (tiles[masks.get(masks.size() - 1).t().getTileLocation().x()][masks.get(masks.size() - 1).t().getTileLocation().y()].hasModifier(EModifier.ANTENNA))
                {
                    masks.remove(masks.size() - 1);
                    break;
                }

                break;
            }

            /* If the laser is at the entry point where we shoot. We cannot reach players on this tile. */
            if (tiles[masks.get(masks.size() - 1).t().getTileLocation().x()][masks.get(masks.size() - 1).t().getTileLocation().y()].isNotTraversable(rot.getOpposite(), false))
            {
                masks.remove(masks.size() - 1);
                break;
            }

            if (RCoordinate.isOccupied(masks.get(masks.size() - 1).t().getTileLocation()))
            {
                break;
            }

            continue;
        }

        return masks.toArray(new RLaserMask[0]);
    }

    // endregion Getters and Setters

}
