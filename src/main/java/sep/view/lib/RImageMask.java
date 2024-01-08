package sep.view.lib;

import sep. Types;

import javafx.scene.image.  Image;

public final record RImageMask(Image i, String url)
{
    /* We store the url outside the JFX Image because by translating from the SWING Util Library, the url will be discarded. */

    /** Will always return a valid image url. */
    public String getSanitizedURL()
    {
        if (Types.EConfigurations.isDev())
        {
            return i.getUrl();
        }

        return url;
    }

}
