package sep.view.viewcontroller;

import javafx.scene.image.Image;
import org.json.JSONObject;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.image.ImageView;
import org.json.JSONArray;
import javafx.scene.transform.Translate;
import javafx.scene.transform.Rotate;
import org.json.JSONException;

public class TileModifier
{
    private static final Logger l = LogManager.getLogger(TileModifier.class);

    private final JSONObject tile;

    private static final String path = "file:src/main/resources/public/";
    private static final String extension = ".png";

    public TileModifier(JSONObject tile)
    {
        super();
        this.tile = tile;
        return;
    }

    public void rotateImage(ImageView iv)
    {
        if (Objects.equals(this.tile.getString("type"), "Wall"))
        {
            if (this.getOrientationsCount() == 1)
            {
                switch (this.tile.getJSONArray("orientations").getString(0))
                {
                    case "top":
                        iv.setRotate(90);
                        break;
                    case "right":
                        iv.setRotate(180);
                        break;
                    case "bottom":
                        iv.setRotate(270);
                        break;
                    case "left":
                        iv.setRotate(0);
                        break;
                    default:
                        l.error("Unknown orientation: {}", this.tile.getJSONArray("orientations").getString(0));
                        break;
                }

                return;
            }

            return;
        }

        if (Objects.equals(this.tile.getString("type"), "ConveyorBelt"))
        {
            if (this.getOrientationsCount() == 2)
            {
                switch (this.tile.getJSONArray("orientations").getString(0))
                {
                    case "top":
                        iv.setRotate(0);
                        break;
                    case "right":
                        iv.setRotate(90);
                        break;
                    case "bottom":
                        iv.setRotate(180);
                        break;
                    case "left":
                        iv.setRotate(270);
                        break;
                    default:
                        l.error("Unknown orientation: {}", this.tile.getJSONArray("orientations").getString(0));
                        break;
                }

                return;
            }

            if (this.getOrientationsCount() == 3)
            {
                switch (this.tile.getJSONArray("orientations").getString(0))
                {
                    case "top":
                        iv.setRotate(180);
                        if (Objects.equals(this.getOrientations().getString(1), "right") || Objects.equals(this.getOrientations().getString(2), "right"))
                        {
                            iv.setScaleX(-1);
                        }
                        break;

                    case "right":
                        iv.setRotate(270);
                        if (Objects.equals(this.getOrientations().getString(1), "bottom") || Objects.equals(this.getOrientations().getString(2), "bottom"))
                        {
                            iv.setScaleX(-1);
                        }
                        break;

                    case "bottom":
                        iv.setRotate(0);
                        if (Objects.equals(this.getOrientations().getString(1), "left") || Objects.equals(this.getOrientations().getString(2), "left"))
                        {
                            iv.setScaleX(-1);
                        }
                        break;

                    case "left":
                        iv.setRotate(90);
                        if (Objects.equals(this.getOrientations().getString(1), "top") || Objects.equals(this.getOrientations().getString(2), "top"))
                        {
                            iv.setScaleX(-1);
                        }
                        break;

                    default:
                        l.error("Unknown orientation: {}", this.tile.getJSONArray("orientations").getString(0));
                        break;
                }

                return;
            }

            return;
        }

        if (Objects.equals(this.tile.getString("type"), "RestartPoint"))
        {
            if (this.getOrientationsCount() == 1)
            {
                switch (this.tile.getJSONArray("orientations").getString(0))
                {
                    case "top":
                        iv.setRotate(0);
                        break;
                    case "right":
                        iv.setRotate(90);
                        break;
                    case "bottom":
                        iv.setRotate(180);
                        break;
                    case "left":
                        iv.setRotate(270);
                        break;
                    default:
                        l.error("Unknown orientation: {}", this.tile.getJSONArray("orientations").getString(0));
                        break;
                }

                return;
            }

            return;
        }

        if (Objects.equals(this.tile.get("type"), "Laser"))
        {
            switch (this.getOrientations().getString(0))
            {
                case "top":
                    iv.setRotate(270);
                    break;
                case "right":
                    iv.setRotate(0);
                    break;
                case "bottom":
                    iv.setRotate(90);
                    break;
                case "left":
                    iv.setRotate(180);
                    break;

                default:
                    l.error("Unknown orientation: {}", this.tile.getJSONArray("orientations").getString(0));
                    break;
            }
            return;
        }

        return;
    }

    // region Getters and Setters

    public String getType()
    {
        return this.tile.getString("type");
    }

    public static Image getImage(String modName)
    {
        return new Image(String.format("%s%s%s", TileModifier.path, modName, TileModifier.extension));
    }

    public Image getImage()
    {
        if (Objects.equals(this.tile.getString("type"), "Empty"))
        {
            // TODO Variations
            return TileModifier.getImage("EmptyTile_00");
        }

        if (Objects.equals(this.tile.getString("type"), "StartPoint"))
        {
            return TileModifier.getImage("StartPoint");
        }

        if (Objects.equals(this.tile.getString("type"), "Antenna"))
        {
            return TileModifier.getImage("PriorityAntenna");
        }

        if (Objects.equals(this.tile.getString("type"), "EnergySpace"))
        {
            if (this.getCount() == 0)
            {
                return TileModifier.getImage("EnergySpaceInactive");
            }
            if (this.getCount() == 1)
            {
                return TileModifier.getImage("EnergySpaceActive");
            }
        }

        if (Objects.equals(this.tile.getString("type"), "Wall"))
        {
            if (this.getOrientationsCount() == 1)
            {
                return TileModifier.getImage("WallSingle");
            }
        }

        if (Objects.equals(this.tile.getString("type"), "ConveyorBelt"))
        {
            if (this.getOrientationsCount() == 2)
            {
                return TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenStraight" : "ConveyorBeltBlueStraight");
            }

            if (this.getOrientationsCount() == 3)
            {
                switch (this.getOrientations().getString(0))
                {
                    case "top":
                        return Objects.equals(this.getOrientations().getString(1), "bottom") || Objects.equals(this.getOrientations().getString(2), "bottom")
                            ?
                            TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTIn" : "ConveyorBeltBlueTIn")
                            :
                            TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTOut" : "ConveyorBeltBlueTOut")
                            ;

                    case "right":
                        return Objects.equals(this.getOrientations().getString(1), "left") || Objects.equals(this.getOrientations().getString(2), "left")
                            ?
                            TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTIn" : "ConveyorBeltBlueTIn")
                            :
                            TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTOut" : "ConveyorBeltBlueTOut")
                            ;

                    case "bottom":
                        return Objects.equals(this.getOrientations().getString(1), "top") || Objects.equals(this.getOrientations().getString(2), "top")
                            ?
                            TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTIn" : "ConveyorBeltBlueTIn")
                            :
                            TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTOut" : "ConveyorBeltBlueTOut")
                            ;

                    case "left":
                        return Objects.equals(this.getOrientations().getString(1), "right") || Objects.equals(this.getOrientations().getString(2), "right")
                            ?
                            TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTIn" : "ConveyorBeltBlueTIn")
                            :
                            TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTOut" : "ConveyorBeltBlueTOut")
                            ;

                    default:
                        l.error("Unknown orientation: {}", this.tile.getJSONArray("orientations").getString(0));
                        break;
                }
            }
        }

        if (Objects.equals(this.tile.getString("type"), "RestartPoint"))
        {
            return TileModifier.getImage("RestartPoint");
        }

        if (Objects.equals(this.tile.getString("type"), "CheckPoint"))
        {
            return TileModifier.getImage("CheckPoint");
        }

        l.error("Unknown tile type: {}", this.tile.getString("type"));
        if (Objects.equals(this.tile.getString("type"), "Laser"))
        {
            // TODO We ofc have to check if the laser is on the inside or outside of a wall
            //      and use a different image accordingly.
            switch (this.getCount())
            {
                case 1:
                    return TileModifier.getImage("LaserSingleInsetInactive");
                case 2:
                    return TileModifier.getImage("LaserDoubleInsetInactive");
                case 3:
                    return TileModifier.getImage("LaserTripleInsetInactive");

                default:
                    l.error("Unknown laser count: {}", this.getCount());
                    break;
            }
        }


        return TileModifier.getImage("Empty");
    }

    private int getCount()
    {
        return this.tile.getInt("count");
    }

    private int getSpeed()
    {
        return this.tile.getInt("speed");
    }

    private JSONArray getOrientations()
    {
        return this.tile.getJSONArray("orientations");
    }

    private int getOrientationsCount()
    {
        try
        {
            return this.tile.getJSONArray("orientations").length();
        }
        catch (JSONException e)
        {
           l.fatal(this.tile.toString(4));
            throw new RuntimeException(e);
        }
    }

    // endregion Getters and Setters

}
