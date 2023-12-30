package sep.view.viewcontroller;

import sep.view.lib.Types.  EModifier;
import sep.view.lib.Types.  ERotation;
import sep.view.lib.Types.  RLaserMask;
import sep.view.lib.        RCoordinate;

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
        iv.setImage(TileModifier.getImage(String.format(("LaserBeamSingleFull"))));

        if (mask.rot().equals(ERotation.NORTH) || mask.rot().equals(ERotation.SOUTH))
        {
            iv.setRotate(90);
        }

        return iv;
    }

    public ImageView[] getImageViews()
    {
        final ArrayList<Image> images = new ArrayList<Image>();
        final ArrayList<ImageView> imageViews = new ArrayList<ImageView>();

        for (int i = 0; i < this.tile.length(); i++)
        {
            images.add(this.getModifier(i).getImage());
            continue;
        }

        for (int i = 0; i < images.size(); i++)
        {
            final ImageView iv = new ImageView();
            iv.getStyleClass().add("tile-image");
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            iv.setCache(true);
            iv.setImage(images.get(i));

            this.getModifier(i).rotateImage(iv);

            imageViews.add(iv);

            continue;
        }

        /* Background */
        if (!Objects.equals(this.getModifier(0).getType(), EModifier.EMPTY.toString()))
        {
            imageViews.add(new ImageView(TileModifier.getImage("EmptyTile_00")));
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
        return TileModifier.getImage(String.format("Robot_%d", figureID));
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

    public boolean isTraversable(final ERotation rot, boolean bCheckOpposite)
    {
        for (int i = 0; i < this.getModifierSize(); i++)
        {
            if (!Objects.equals(this.getModifier(i).getType(), EModifier.WALL.toString()))
            {
                continue;
            }

            for (final ERotation r : this.getModifier(i).getRotations())
            {
                if (Objects.equals(r, rot))
                {
                    return false;
                }

                if (bCheckOpposite && Objects.equals(r.getOpposite(), rot))
                {
                    return false;
                }

                continue;
            }

        }

        return true;
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

        if (!this.isTraversable(rot, false))
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

            if (!RCoordinate.exists(toCheck, tiles))
            {
                break;
            }

            masks.add(new RLaserMask(tiles[toCheck.x()][toCheck.y()], rot, count));

            if (RCoordinate.isOccupied(masks.get(masks.size() - 1).t().getTileLocation()))
            {
                break;
            }

            if (!tiles[masks.get(masks.size() - 1).t().getTileLocation().x()][masks.get(masks.size() - 1).t().getTileLocation().y()].isTraversable(rot, true))
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

    // endregion Getters and Setters

}
