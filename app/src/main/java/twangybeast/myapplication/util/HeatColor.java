package twangybeast.myapplication.util;

import android.animation.ArgbEvaluator;
import android.graphics.Color;

/**
 * Created by cHeNdAn19 on 3/12/2018.
 */

public class HeatColor {
    public static ArgbEvaluator evaluator = new ArgbEvaluator();
    public static int[] COLORS_HEATMAP_5 = {
            Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED
    };
    //0,    1,  2,  3,  4,
    //0     0.25 0.5 0.75 1
    public static int interpolateColor(int[] colors, float val)
    {
        final int zones = colors.length-1;
        int index = Math.min(zones-1, (int) (Math.min(val, 1.0f)*zones));
        return interpolate2Colors(val, colors[index], index *1.0f / zones, colors[index+1], (index+1)*1.0f/zones);
    }
    public static int interpolate2Colors(float val, int color1, float value1, int color2, float value2)
    {
        return (Integer) evaluator.evaluate((val - value1)/(value2-value1), color1, color2);
    }
}
