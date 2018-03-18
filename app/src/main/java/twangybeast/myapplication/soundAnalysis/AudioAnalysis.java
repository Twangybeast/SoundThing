package twangybeast.myapplication.soundAnalysis;

import android.arch.core.util.Function;

/**
 * Created by cHeNdAn19 on 2/15/2018.
 */

public class AudioAnalysis
{
    public static float MAX_FREQ = 8000;
    public static float MIN_FREQ = 80;
    public static void functionOnFloats(float[] floats, Function<Float, Float> function)
    {
        for (int i = 0; i < floats.length; i++)
        {
            floats[i] = function.apply(floats[i]);
        }
    }
    //https://github.com/Sciss/SpeechRecognitionHMM/blob/master/src/main/java/org/ioe/tprsa/audio/feature/MFCC.java
    public static float[] dct(float[] origin, int resultCount)
    {
        float[] result = new float[resultCount];
        for (int i = 0; i < result.length; i++)
        {
            for (int j = 0; j < origin.length; j++)
            {
                result[i] += origin[j] * Math.cos(Math.PI * i / origin.length * (j + 0.5));
            }
        }
        return result;
    }

    public static void takeNaturalLog(float[] floats)
    {
        for (int i = 0; i < floats.length; i++)
        {
            floats[i] = (float) Math.log(floats[i] + 1);
        }
    }

    public static float[] melFilter(float[] spectrum, int[] melIndices)
    {
        float[] result = new float[melIndices.length - 2];
        for (int m = 1; m < melIndices.length - 1; m++)
        {
            float sum = 0;
            for (int i = melIndices[m - 1]; i <= melIndices[m]; i++)
            {
                sum += spectrum[i] * ((i - melIndices[m - 1]) / (melIndices[m] - melIndices[m - 1]));
            }
            for (int i = melIndices[m]; i <= melIndices[m + 1]; i++)
            {
                sum += spectrum[i] * ((melIndices[m + 1] - i) / (melIndices[m + 1] - melIndices[m]));
            }
            result[m - 1] = sum;
        }
        return result;
    }

    public static int[] getMelIndices(int sampleFrequency, int N, int numFilters)
    {
        float lowMel = frequencyToMel(MIN_FREQ);
        float highMel = frequencyToMel(MAX_FREQ);
        int[] indexes = new int[numFilters + 2];
        float melStep = (highMel - lowMel) / (numFilters + 1);
        for (int i = 0; i < indexes.length; i++)
        {
            float freq = melToFrequency(i * melStep + lowMel);
            indexes[i] = Math.round(N * freq / sampleFrequency);//TODO Check if valid index
        }
        return indexes;
    }

    public static float frequencyToMel(float f)
    {
        return (float) (1125 * Math.log(1 + (f / 700)));
    }

    public static float melToFrequency(float m)
    {
        return (float) (700 * (Math.exp(m / 1125) - 1));
    }

    public static float getMax(float[] floats)
    {
        float max = 0;
        for (float f : floats)
        {
            max = Math.max(max, f);
        }
        return max;
    }

    public static void complexToFloat(Complex[] complex, float[] target, int N)
    {
        for (int i = 0; i < N; i++)
        {
            target[i] = complex[i].getMag();
        }
    }

    public static float[] toFloatArray(short[] in, float max, int N)
    {
        float[] out = new float[N];
        for (int i = 0; i < N; i++)
        {
            out[i] = (float) in[i] / max;
        }
        return out;
    }

    public static void toFloatArray(short[] src, float[] target, float max, int N)
    {
        for (int i = 0; i < N; i++)
        {
            target[i] = (float) src[i] / max;
        }
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

    public static void getRange(Complex[] in, Complex[] out, int i, int length)
    {
        System.arraycopy(in, i, out, 0, length);
    }
}