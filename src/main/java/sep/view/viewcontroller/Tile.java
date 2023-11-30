package sep.view.viewcontroller;

import org.json.JSONArray;
import javafx.scene.image.Image;
import java.util.ArrayList;
import java.util.Objects;
import javafx.scene.image.ImageView;

public class Tile
{
    private final JSONArray tile;

    private int translateX;
    private int translateY;

    public Tile(JSONArray tile)
    {
        super();
        this.tile = tile;
        this.translateX = 0;
        this.translateY = 0;
        return;
    }

    public int getModifierSize()
    {
        return this.tile.length();
    }

    public TileModifier getModifier(int idx)
    {
        return new TileModifier(this.tile.getJSONObject(idx));
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
            ImageView iv = new ImageView();
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
        if (!Objects.equals(this.getModifier(0).getType(), "Empty"))
        {
            imageViews.add(new ImageView(TileModifier.getImage("EmptyTile_00")));
        }

        return imageViews.toArray(new ImageView[0]);
    }

    public int getTranslateX()
    {
        return this.translateX;
    }

    public int getTranslateY()
    {
        return this.translateY;
    }

    public void setTranslateX(int translateX)
    {
        this.translateX = translateX;
        return;
    }

    public void setTranslateY(int translateY)
    {
        this.translateY = translateY;
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

    public static Image getRobotImage(int figureID)
    {
        return TileModifier.getImage(String.format("Robot_%d", figureID));
    }

}
