package sep.view.viewcontroller;

import sep.view.lib.                EModifier;
import sep.view.lib.                ERotation;
import sep.view.lib.                RLaserMask;
import sep.view.lib.                RCheckpointMask;
import sep.view.lib.                RCoordinate;
import sep.view.clientcontroller.   EGameState;

import org.json.            JSONArray;
import javafx.scene.image.  Image;
import javafx.scene.image.  ImageView;
import java.util.           ArrayList;
import java.util.           Objects;

public final class Tile
{
    private final JSONArray tile;

    private int xTranslation;
    private int yTranslation;

    public Tile(final JSONArray tile)
    {
        super();

        this.tile = tile;
        this.xTranslation = 0;
        this.yTranslation = 0;

        return;
    }

    // region Getters and Setters

    public int getModifierSize()
    {
        return this.tile.length();
    }

    public TileModifier getModifier(final int idx)
    {
        return new TileModifier(this.tile.getJSONObject(idx));
    }

    public static ImageView getFormattedImageView(final RLaserMask mask)
    {
        final ImageView iv = new ImageView();
        iv.getStyleClass().add("tile-image");
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);
        iv.setImage(TileModifier.loadCachedImage(String.format(("LaserBeamSingleFull"))));

        if (mask.rot().equals(ERotation.NORTH) || mask.rot().equals(ERotation.SOUTH))
        {
            iv.setRotate(90);
        }

        return iv;
    }

    public static ImageView getFormattedImageView(final RCheckpointMask mask)
    {
        final ImageView iv = new ImageView();
        iv.getStyleClass().add("tile-image");
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setCache(true);
        iv.setImage(TileModifier.loadCachedImage(String.format(("CheckPoint%d"), mask.id())));

        return iv;

    }

    public ImageView[] getImageViews()
    {
        final ArrayList<Image>      images      = new ArrayList<Image>();
        final ArrayList<ImageView>  imageViews  = new ArrayList<ImageView>();
        /* Images that are always rendered first, but before the background. Warning: These images cannot be rotated. */
        final ArrayList<Image>      renderFirst  = new ArrayList<Image>();

        for (int i = 0; i < this.tile.length(); ++i)
        {
            /* Checkpoints are rendered dynamically because they are not static. */
            if (Objects.equals(this.getModifier(i).getType(), EModifier.CHECK_POINT.toString()))
            {
                /* Moving checkpoints are rendered desperately as they are not static. */
                if (this.hasModifier(EModifier.CONVEYOR_BELT))
                {
                    images.add(null);
                    continue;
                }

                renderFirst.add(this.getModifier(i).loadCachedImage());
                images.add(null);

                continue;
            }

            if (Objects.equals(this.getModifier(i).getType(), EModifier.ENERGY_SPACE.toString()))
            {
                if (EGameState.INSTANCE.isEnergySpaceDeactivated(new RCoordinate(this.xTranslation, this.yTranslation)))
                {
                    images.add(TileModifier.loadCachedImage("EnergySpaceInactive"));
                    continue;
                }
            }

            images.add(this.getModifier(i).loadCachedImage());

            continue;
        }

        images.addAll(renderFirst);

        for (int i = 0; i < images.size(); ++i)
        {
            if (images.get(i) == null)
            {
                continue;
            }

            final ImageView iv = new ImageView();
            iv.getStyleClass().add("tile-image");
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setCache(true);
            iv.setImage(images.get(i));

            if (i < this.getModifierSize())
            {
                this.getModifier(i).rotateImage(iv);
            }

            imageViews.add(iv);

            continue;
        }

        renderFirst.clear();

        /* Background */
        if (!Objects.equals(this.getModifier(0).getType(), EModifier.EMPTY.toString()))
        {
            imageViews.add(new ImageView(TileModifier.loadCachedImage("EmptyTile_00")));
        }

        return imageViews.toArray(new ImageView[0]);
    }

    public int getXTranslation()
    {
        return this.xTranslation;
    }

    public int getYTranslation()
    {
        return this.yTranslation;
    }

    public void setXTranslation(final int xTranslation)
    {
        this.xTranslation = xTranslation;
        return;
    }

    public void setYTranslation(final int yTranslation)
    {
        this.yTranslation = yTranslation;
        return;
    }

    public boolean isClickable()
    {
        for (int i = 0; i < this.tile.length(); i++)
        {
            if (Objects.equals(this.getModifier(i).getType(), "StartPoint"))
            {
                return true;
            }
        }

        return false;
    }

    public static Image getRobotImage(final int figureID)
    {
        return TileModifier.loadCachedImage(String.format("Robot_%d", figureID));
    }

    public boolean hasModifier(final EModifier m)
    {
        for (int i = 0; i < this.tile.length(); i++)
        {
            if (Objects.equals(this.getModifier(i).getType(), m.toString()))
            {
                return true;
            }
        }

        return false;
    }

    public boolean isNotTraversable(final ERotation rot, boolean bCheckOpposite)
    {
        for (int i = 0; i < this.getModifierSize(); i++)
        {
            if (Objects.equals(this.getModifier(i).getType(), EModifier.ANTENNA.toString()))
            {
                return true;
            }

            if (!Objects.equals(this.getModifier(i).getType(), EModifier.WALL.toString()))
            {
                continue;
            }

            for (final ERotation r : this.getModifier(i).getRotations())
            {
                if (Objects.equals(r, rot))
                {
                    return true;
                }

                if (bCheckOpposite && Objects.equals(r.getOpposite(), rot))
                {
                    return true;
                }

                continue;
            }

        }

        return false;
    }

    private TileModifier getLaserModifier()
    {
        for (int i = 0; i < this.getModifierSize(); i++)
        {
            if (Objects.equals(this.getModifier(i).getType(), EModifier.LASER.toString()))
            {
                return this.getModifier(i);
            }
        }

        return null;
    }

    public RLaserMask[] getLaserAffectedTiles(final Tile[][] tiles, final int count)
    {
        final ERotation rot                 = Objects.requireNonNull(this.getLaserModifier()).getRotations()[0];
        final ArrayList<RLaserMask> masks   = new ArrayList<RLaserMask>();

        masks.add(new RLaserMask(this, rot, count));

        if (this.isNotTraversable(rot, false))
        {
            return masks.toArray(new RLaserMask[0]);
        }

        if (RCoordinate.isOccupied(this.getTileLocation()))
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

            if (RCoordinate.isOutOfBounds(toCheck, tiles))
            {
                break;
            }

            masks.add(new RLaserMask(tiles[toCheck.x()][toCheck.y()], rot, count));

            if (RCoordinate.isOccupied(masks.get(masks.size() - 1).t().getTileLocation()))
            {
                break;
            }

            if (tiles[masks.get(masks.size() - 1).t().getTileLocation().x()][masks.get(masks.size() - 1).t().getTileLocation().y()].isNotTraversable(rot, true))
            {
                break;
            }

            continue;
        }

        return masks.toArray(new RLaserMask[0]);
    }

    public RCoordinate getTileLocation()
    {
        return new RCoordinate(this.xTranslation, this.yTranslation);
    }

    public int getCheckpointID()
    {
        for (int i = 0; i < this.getModifierSize(); i++)
        {
            if (Objects.equals(this.getModifier(i).getType(), EModifier.CHECK_POINT.toString()))
            {
                return this.getModifier(i).getCount();
            }
        }

        return -1;
    }

    @Override
    public String toString()
    {
        return this.tile.toString(0);
    }

    public ArrayList<Integer> getPushPanelRegisters()
    {
        for (int i = 0; i < this.getModifierSize(); i++)
        {
            if (Objects.equals(this.getModifier(i).getType(), EModifier.PUSH_PANEL.toString()))
            {
                return this.getModifier(i).getPushPanelRegisters();
            }

            continue;
        }

        return new ArrayList<Integer>();
    }

    // endregion Getters and Setters

}
