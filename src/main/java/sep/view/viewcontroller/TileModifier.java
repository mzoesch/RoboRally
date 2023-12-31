package sep.view.viewcontroller;

import sep.view.lib.Types.          ERotation;
import sep.view.lib.Types.          RGearMask;

import java.util.                   Objects;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import javafx.scene.image.          Image;
import javafx.scene.image.          ImageView;
import org.json.                    JSONObject;
import org.json.                    JSONArray;
import org.json.                    JSONException;

public final class TileModifier
{
    private static final Logger l = LogManager.getLogger(TileModifier.class);

    private final JSONObject tile;

    private static final String path        = "file:src/main/resources/public/";
    private static final String extension   = ".png";

    public TileModifier(final JSONObject tile)
    {
        super();
        this.tile = tile;
        return;
    }

    public void rotateImage(final ImageView iv)
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
            if (this.getOrientationsCount() == 2 && isConveyorBeltCurved()){
                if(isConveyorBeltCurvedLeft()) {
                    switch (this.tile.getJSONArray("orientations").getString(0)) {
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
                } else{
                    switch (this.tile.getJSONArray("orientations").getString(0)) {
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
                }
            }
            else if (this.getOrientationsCount() == 2)
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
        if (Objects.equals(this.tile.get("type"), "PushPanel"))
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
            if (this.getOrientationsCount() == 2) {
                if (this.isConveyorBeltCurved()) {
                    if (this.isConveyorBeltCurvedLeft())
                        return TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenCurvedLeft" : "ConveyorBeltBlueCurvedLeft");
                    else{
                        return TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenCurvedRight" : "ConveyorBeltBlueCurvedRight");
                    }
                } else {
                    return TileModifier.getImage(this.getSpeed() == 1 ? "ConveyorBeltGreenStraight" : "ConveyorBeltBlueStraight");
                }
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

        if (Objects.equals(this.tile.getString("type"), "Gear"))
        {
                switch (this.tile.getString("orientations")) {
                    case "clockwise":
                        return TileModifier.getImage("GearClockwise");

                    case "counterclockwise":
                        return TileModifier.getImage("GearCounterclockwise");

                    default:
                        l.error("Unknown orientation for gear: {}", this.tile.getJSONArray("orientations").toString());
                        break;
                }
        }

        if (Objects.equals(this.tile.getString("type"), "Pit"))
        {
            return TileModifier.getImage("Pit");
        }


        if (Objects.equals(this.tile.getString("type"), "RestartPoint"))
        {
            return TileModifier.getImage("RestartPoint");
        }

        if (Objects.equals(this.tile.getString("type"), "CheckPoint"))
        {
            return TileModifier.getImage("CheckPoint");
        }

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

        if(Objects.equals(this.tile.getString("type"), "PushPanel")) {
            if(this.getRegisters().length() == 1){
                switch(this.getRegisters().getInt(0)){
                    case 1:
                        return TileModifier.getImage("PushPanelRegister1");
                    case 2:
                        return TileModifier.getImage("PushPanelRegister2");
                    case 3:
                        return TileModifier.getImage("PushPanelRegister3");
                    case 4:
                        return TileModifier.getImage("PushPanelRegister4");
                    case 5:
                        return TileModifier.getImage("PushPanelRegister5");
                }
            }
            else if(this.getRegisters().length() == 2){
                return TileModifier.getImage("PushPanelRegister2And4");
            }
            else if(this.getRegisters().length() == 3){
                return TileModifier.getImage("PushPanelRegister1And3And5");
            } else{
                l.debug("Can not resolve Registers of PushPanel");
                //TODO Entfernen, wenn PushRegister richtig in Map
                return TileModifier.getImage("PushPanelRegister1");
            }
        }

        l.error("Unknown tile type or variation: {}. Rendering empty tile.", this.tile.getString("type"));
        return TileModifier.getImage("Empty");
    }

    private int getCount()
    {
        return this.tile.getInt("count");
    }

    private JSONArray getRegisters()
    {
        return this.tile.getJSONArray("registers");
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
        catch (final JSONException e)
        {
            l.error(this.tile.toString(4));
            throw new RuntimeException(e);
        }
    }

    private boolean isConveyorBeltCurved(){
        int in = 0;
        int out = 0;
        switch(this.getOrientations().getString(0)){
            case "top":
                out = 0;
                break;
            case "right":
                out = 90;
                break;
            case "bottom":
                out = 180;
                break;
            case "left":
                out = 270;
                break;
        }
        switch(this.getOrientations().getString(1)){
            case "top":
                in = 0;
                break;
            case "right":
                in = 90;
                break;
            case "bottom":
                in = 180;
                break;
            case "left":
                in = 270;
                break;
        }
        int diff = (out-in);
        return diff % 180 != 0;
    }

    public boolean isConveyorBeltCurvedLeft(){
        try {
            int in = 0;
            int out = 0;
            switch (this.getOrientations().getString(0)) {
                case "top":
                    out = 0;
                    break;
                case "right":
                    out = 90;
                    break;
                case "bottom":
                    out = 180;
                    break;
                case "left":
                    out = 270;
                    break;
            }
            switch (this.getOrientations().getString(1)) {
                case "top":
                    in = 0;
                    break;
                case "right":
                    in = 90;
                    break;
                case "bottom":
                    in = 180;
                    break;
                case "left":
                    in = 270;
                    break;
            }
            int diff = (out - in);
            if ((270 > diff) && ((diff > 0)) || diff == -270) {
                return true;
            }
            return false;
        }
        catch(Exception e){
            return false;
        }
    }

    public ERotation[] getRotations()
    {
        final ERotation[] rotations = new ERotation[this.getOrientationsCount()];

        for (int i = 0; i < this.getOrientationsCount(); i++)
        {
            rotations[i] = ERotation.fromString(this.getOrientations().getString(i));
        }

        return rotations;
    }

    public static boolean isGear(final Image i)
    {
        return Objects.equals(i.getUrl(), TileModifier.getImage("GearClockwise").getUrl()) || Objects.equals(i.getUrl(), TileModifier.getImage("GearCounterclockwise").getUrl());
    }

    public static RGearMask generateGearMask(final ImageView iv)
    {
        return new RGearMask(iv, Objects.equals(iv.getImage().getUrl(), TileModifier.getImage("GearClockwise").getUrl()), 0);
    }

    // endregion Getters and Setters

}
