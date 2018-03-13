package twangybeast.myapplication.soundAnalysis;

/**
 * Created by cHeNDAn19 on 3/13/2018.
 */


public class WindowHelper
{
    private float[] window;
    public WindowHelper(int N)
    {
        window = new float[N];
        for (int i = 0; i < N; i++) {
            window[i] = windowFunction(i, N);
        }
    }
    public float getValue(int n)
    {
        return window[n];
    }
    public static float windowFunction(int n, int N)
    {
        return (float) (0.5f * (1 - Math.cos(Math.PI * 2 * n / (N - 1))));
    }
}