package twangybeast.myapplication.soundAnalysis;

import java.util.Arrays;

/**
 * Created by cHeNdAn19 on 2/15/2018.
 */

public class AudioAnalysis
{
    public static float[] toFloatArray(short[] in, float max, int N)
    {
        float[] out = new float[N];
        for (int i = 0; i < N; i++)
        {
            out[i] = (float) in[i] / max;
        }
        return out;
    }

    public static void restrictFloatArray(float[] in, float max)
    {
        for (int i = 0; i < in.length; i++)
        {
            in[i] = in[i] / max;
        }
    }
    public static void restrictComplexArray(Complex[] in, float max)
    {
        for (int i = 0; i < in.length; i++)
        {
            in[i] = in[i].divide(max);
        }
    }
    //TODO REMOVE COMMENT
    //See https://en.wikipedia.org/wiki/Cooley%E2%80%93Tukey_FFT_algorithm#C++_Example_Code
    public static void calculateFourier(float[] input, Complex[] data, int N)
    {
        for (int i = 0; i < N; i++)
        {
            data[i] = new Complex(input[i], 0);
        }
        calculateFourier(data, 0, N);
    }

    public static void calculateFourier(Complex[] data, int i, int N)
    {
        if (N >= 2)
        {
            separate(data, i, N);
            calculateFourier(data, i, N / 2);
            calculateFourier(data, i + N / 2, N / 2);
            for (int k = 0; k < N / 2; k++)
            {
                Complex even = data[k + i];
                Complex odd = data[k + i + N / 2];
                //Twiddle factor
                Complex w = Complex.exp(-2. * Math.PI * k / N);
                data[k + i] = Complex.add(even, Complex.multiply(w, odd));
                data[k + i + N / 2] = Complex.subtract(even, Complex.multiply(w, odd));
            }
        }
    }

    public static void inverseFourier(Complex[] in, Complex[] data, int N)
    {
        for (int i = 0; i < N; i++)
        {
            data[i] = in[i].conjugate();
        }
        calculateFourier(data, 0, N);
        for (int i = 0; i < N; i++)
        {
            data[i] = data[i].conjugate().divide(N);
        }
    }

    //puts evens in lower half of array, odds in upper
    private static void separate(Complex[] a, int index, int n)
    {
        Complex[] b = new Complex[n / 2];
        for (int i = 0; i < n / 2; i++)
        {//put odd in temp storage
            b[i] = a[i * 2 + 1 + index];
        }
        for (int i = 0; i < n / 2; i++)
        {
            a[i + index] = a[i * 2 + index];
        }
        for (int i = 0; i < n / 2; i++)
        {
            a[i + n / 2 + index] = b[i];
        }
        b = null;
    }

    public static Complex[] getUpperHalf(Complex[] in)
    {
        Complex[] upper = new Complex[in.length / 2];
        for (int i = 0; i < upper.length; i++)
        {
            upper[i] = in[i + upper.length / 2];
        }
        return upper;
    }
    public static Complex[] getRange(Complex[] in, int i, int length)
    {
        Complex[] res = new Complex[length];
        System.arraycopy(in, i, res, 0, length);
        return res;
    }
}