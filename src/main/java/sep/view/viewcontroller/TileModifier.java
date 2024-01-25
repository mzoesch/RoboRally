package sep.view.viewcontroller;

import sep.                         Types;
import sep.view.lib.                ERotation;
import sep.view.lib.                RGearMask;
import sep.view.lib.                RImageMask;
import sep.view.clientcontroller.   GameInstance;

import java.io.                     IOException;
import java.util.                   Objects;
import org.apache.logging.log4j.    LogManager;
import org.apache.logging.log4j.    Logger;
import javax.imageio.               ImageIO;
import java.net.                    URL;
import javafx.scene.image.          Image;
import javafx.scene.image.          ImageView;
import org.json.                    JSONObject;
import org.json.                    JSONArray;
import org.json.                    JSONException;
import javafx.embed.swing.          SwingFXUtils;
import java.awt.image.              BufferedImage;
import java.util.                   HashMap;

public final class TileModifier
{
    private static final Logger l = LogManager.getLogger(TileModifier.class);

    private final JSONObject tile;

    /** TODO This is currently fine but if we have to many images this will get really big and significantly increases memory usage. */
    private static final HashMap<String, RImageMask>    IMG_CACHE   = new HashMap<String, RImageMask>();
    private static final String                         PATH_DEV    = "file:src/main/resources/public/";
    private static final String                         PATH_PROD   = "/public/";
    private static final String                         EXTENSION   = ".png";

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

    private static Image loadImage(final String modName)
    {
        // Why are we doing this?
        // Loading directly in JavaFX is way faster than loading via URL and is basically instant.
        // But when the Application is packaged with MVN into a JAR, the JavaFX fails to load the images, and we must
        // use the SWING Framework to do the loading for us. If it is figured out how to directly load files, we can
        // safety remove this code.

        if (Types.EConfigurations.isDev())
        {
            final String    strURL  = String.format("%s%s%s", TileModifier.PATH_DEV, modName, TileModifier.EXTENSION);
            final Image     i       = new Image(strURL);
            if (i.isError())
            {
                l.error("Could not load image: {}", strURL);
                l.error(i.getException().getMessage());
                return i;
            }

            return i;
        }

        if (Types.EConfigurations.isProd())
        {
            final String            strURL  = String.format("%s%s%s", TileModifier.PATH_PROD, modName, TileModifier.EXTENSION);
            final URL               url     = TileModifier.class.getResource(strURL);
            final BufferedImage     awtImg;

            if (url == null)
            {
                l.error("Could not load image because the calculated url does not exist: {}", strURL);
                return null;
            }

            try
            {
                awtImg = ImageIO.read(url);
            }
            catch (final IOException e)
            {
                l.fatal("Could not load image: {}", modName);
                l.fatal(e.getMessage());
                GameInstance.kill();
                return null;
            }

            /* TODO Test if this method is faster then the above. */
            /* final Image fxImgDirect = new Image(url.openStream() */

            return SwingFXUtils.toFXImage(awtImg, null);
        }

        l.fatal("Failed to detect application configuration.");
        GameInstance.kill();
        return null;
    }

    public static Image loadCachedImage(final String modName)
    {
        if (TileModifier.IMG_CACHE.containsKey(modName))
        {
            return TileModifier.IMG_CACHE.get(modName).i();
        }

        l.debug("Loading and caching image: {}.", modName);
        final Image i = TileModifier.loadImage(modName);
        TileModifier.IMG_CACHE.put(modName, new RImageMask(i, Types.EConfigurations.isDev() ? null : String.format("%s%s%s", TileModifier.PATH_PROD, modName, TileModifier.EXTENSION)));
        return i;
    }

    public Image loadCachedImage()
    {
        if (Objects.equals(this.tile.getString("type"), "Empty"))
        {
            // TODO Variations
            return TileModifier.loadCachedImage("EmptyTile_00");
        }

        if (Objects.equals(this.tile.getString("type"), "StartPoint"))
        {
            return TileModifier.loadCachedImage("StartPoint");
        }

        if (Objects.equals(this.tile.getString("type"), "Antenna"))
        {
            return TileModifier.loadCachedImage("PriorityAntenna");
        }

        if (Objects.equals(this.tile.getString("type"), "EnergySpace"))
        {
            if (this.getCount() == 0)
            {
                return TileModifier.loadCachedImage("EnergySpaceInactive");
            }
            if (this.getCount() == 1)
            {
                return TileModifier.loadCachedImage("EnergySpaceActive");
            }
        }

        if (Objects.equals(this.tile.getString("type"), "Wall"))
        {
            if (this.getOrientationsCount() == 1)
            {
                return TileModifier.loadCachedImage("WallSingle");
            }
        }

        if (Objects.equals(this.tile.getString("type"), "ConveyorBelt"))
        {
            if (this.getOrientationsCount() == 2) {
                if (this.isConveyorBeltCurved()) {
                    if (this.isConveyorBeltCurvedLeft())
                        return TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenCurvedLeft" : "ConveyorBeltBlueCurvedLeft");
                    else{
                        return TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenCurvedRight" : "ConveyorBeltBlueCurvedRight");
                    }
                } else {
                    return TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenStraight" : "ConveyorBeltBlueStraight");
                }
            }

            if (this.getOrientationsCount() == 3)
            {
                switch (this.getOrientations().getString(0))
                {
                    case "top":
                        return Objects.equals(this.getOrientations().getString(1), "bottom") || Objects.equals(this.getOrientations().getString(2), "bottom")
                            ?
                            TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTIn" : "ConveyorBeltBlueTIn")
                            :
                            TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTOut" : "ConveyorBeltBlueTOut")
                            ;

                    case "right":
                        return Objects.equals(this.getOrientations().getString(1), "left") || Objects.equals(this.getOrientations().getString(2), "left")
                            ?
                            TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTIn" : "ConveyorBeltBlueTIn")
                            :
                            TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTOut" : "ConveyorBeltBlueTOut")
                            ;

                    case "bottom":
                        return Objects.equals(this.getOrientations().getString(1), "top") || Objects.equals(this.getOrientations().getString(2), "top")
                            ?
                            TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTIn" : "ConveyorBeltBlueTIn")
                            :
                            TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTOut" : "ConveyorBeltBlueTOut")
                            ;

                    case "left":
                        return Objects.equals(this.getOrientations().getString(1), "right") || Objects.equals(this.getOrientations().getString(2), "right")
                            ?
                            TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTIn" : "ConveyorBeltBlueTIn")
                            :
                            TileModifier.loadCachedImage(this.getSpeed() == 1 ? "ConveyorBeltGreenTOut" : "ConveyorBeltBlueTOut")
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
                        return TileModifier.loadCachedImage("GearClockwise");

                    case "counterclockwise":
                        return TileModifier.loadCachedImage("GearCounterclockwise");

                    default:
                        l.error("Unknown orientation for gear: {}", this.tile.getJSONArray("orientations").toString());
                        break;
                }
        }

        if (Objects.equals(this.tile.getString("type"), "Pit"))
        {
            return TileModifier.loadCachedImage("Pit");
        }


        if (Objects.equals(this.tile.getString("type"), "RestartPoint"))
        {
            return TileModifier.loadCachedImage("RestartPoint");
        }

        if (Objects.equals(this.tile.getString("type"), "CheckPoint"))
        {
            switch (this.getCount())
            {
                case 1:
                    return TileModifier.loadCachedImage("CheckPoint1");
                case 2:
                    return TileModifier.loadCachedImage("Checkpoint2");
                case 3:
                    return TileModifier.loadCachedImage("CheckPoint3");
                case 4:
                    return TileModifier.loadCachedImage("CheckPoint4");
                case 5:
                    return TileModifier.loadCachedImage("CheckPoint5");

                default:
                    l.error("Unknown laser count: {}", this.getCount());
                    break;
            }
        }

        if (Objects.equals(this.tile.getString("type"), "Laser"))
        {
            // TODO We ofc have to check if the laser is on the inside or outside of a wall
            //      and use a different image accordingly.
            switch (this.getCount())
            {
                case 1:
                    return TileModifier.loadCachedImage("LaserSingleInsetInactive");
                case 2:
                    return TileModifier.loadCachedImage("LaserDoubleInsetInactive");
                case 3:
                    return TileModifier.loadCachedImage("LaserTripleInsetInactive");

                default:
                    l.error("Unknown laser count: {}", this.getCount());
                    break;
            }
        }

        if(Objects.equals(this.tile.getString("type"), "PushPanel")) {
            if(this.getRegisters().length() == 1){
                switch(this.getRegisters().getInt(0)){
                    case 1:
                        return TileModifier.loadCachedImage("PushPanelRegister1");
                    case 2:
                        return TileModifier.loadCachedImage("PushPanelRegister2");
                    case 3:
                        return TileModifier.loadCachedImage("PushPanelRegister3");
                    case 4:
                        return TileModifier.loadCachedImage("PushPanelRegister4");
                    case 5:
                        return TileModifier.loadCachedImage("PushPanelRegister5");
                }
            }
            else if(this.getRegisters().length() == 2){
                return TileModifier.loadCachedImage("PushPanelRegister2And4");
            }
            else if(this.getRegisters().length() == 3){
                return TileModifier.loadCachedImage("PushPanelRegister1And3And5");
            } else{
                l.debug("Can not resolve Registers of PushPanel");
                //TODO Entfernen, wenn PushRegister richtig in Map
                return TileModifier.loadCachedImage("PushPanelRegister1");
            }
        }

        l.error("Unknown tile type or variation: {}. Rendering empty tile.", this.tile.getString("type"));
        return TileModifier.loadCachedImage("Empty");
    }

    public int getCount()
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
        if (Types.EConfigurations.isDev())
        {
            return Objects.equals(i.getUrl(), TileModifier.loadCachedImage("GearClockwise").getUrl()) || Objects.equals(i.getUrl(), TileModifier.loadCachedImage("GearCounterclockwise").getUrl());
        }

        final RImageMask rim = TileModifier.getCachedImageMask(i);
        if (rim == null)
        {
            l.error("Could not check if image is gear because the image mask could not be found.");
            return false;
        }

        return rim.getSanitizedURL().contains("GearClockwise") || rim.getSanitizedURL().contains("GearCounterclockwise");
    }

    public static RGearMask generateGearMask(final ImageView iv)
    {
        l.debug("Generating gear mask for image: {}.", iv);

        if (Types.EConfigurations.isDev())
        {
            return new RGearMask(iv, Objects.equals(iv.getImage().getUrl(), TileModifier.loadCachedImage("GearClockwise").getUrl()), 0);
        }

        return new RGearMask(iv, Objects.requireNonNull(TileModifier.getCachedImageMask(iv.getImage())).getSanitizedURL().contains("GearClockwise"), 0);
    }

    private static RImageMask getCachedImageMask(final Image i)
    {
        if (Types.EConfigurations.isDev())
        {
            l.error("This method should not be called in dev mode as the image urls can be easily compared instead.");
            return null;
        }

        for (final RImageMask rim : TileModifier.IMG_CACHE.values())
        {
            if (Objects.equals(rim.i(), i))
            {
                return rim;
            }

            continue;
        }

        /* As an image could not have an url. */
        l.error("Could not find image mask for image: {} [url: {}]", i, i.getUrl());

        return null;
    }

    // endregion Getters and Setters

}
