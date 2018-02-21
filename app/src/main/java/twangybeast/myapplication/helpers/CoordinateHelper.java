package twangybeast.myapplication.helpers;

import android.content.res.Resources;

/**
 * Created by Twangybeast on 2/20/2018.
 */

public class CoordinateHelper
{
    public static float dpToPixel(float dp)
    {
        return dp * Resources.getSystem().getDisplayMetrics().densityDpi / 160f;
    }
    public static float pixelToDp(float pixel)
    {
        return pixel * 160f / Resources.getSystem().getDisplayMetrics().densityDpi;
    }
}
