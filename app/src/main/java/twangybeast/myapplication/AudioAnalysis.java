package twangybeast.myapplication;

/**
 * Created by cHeNdAn19 on 2/15/2018.
 */

public class AudioAnalysis {
    public static float[] toFloatArray(short[] in, float max)
    {
        float[] out = new float[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = in[i] / max;
        }
        return out;
    }
    public static void restrictFloatArray(float[] in, float max){
        for (int i = 0; i < in.length; i++) {
            in[i] = in[i] / max;
        }
    }
    //TODO REMOVE COMMENT
    //See https://en.wikipedia.org/wiki/Cooley%E2%80%93Tukey_FFT_algorithm#C++_Example_Code
    public static float calculateFourier(float[] input, float[] result, int N)
    {
        Complex[] data = new Complex[N];
        for (int i = 0; i < N; i++) {
            data[i] = new Complex(input[i], 0);
        }
        calculateFourier(data, 0, N);
        float max = 0;
        for (int i = 0; i < N; i++) {
            result[i] = data[i].getMag();
            if (result[i] > max)
            {
                max = result[i];
            }
        }
        return max;
    }
    public static void calculateFourier(Complex[] data, int i, int N)
    {
        if (N >= 2)
        {
            separate(data, i, N);
            calculateFourier(data, i, N/2);
            calculateFourier(data, i+N/2, N/2);
            for (int k = 0; k < N/2; k++) {
                Complex even = data[k + i];
                Complex odd = data[k + i + N/2];
                //Twiddle factor
                Complex w = Complex.exp(-2. * Math.PI * k/N);
                data[k + i] = Complex.add(even, Complex.multiply(w, odd));
                data[k + i  + N/2] = Complex.subtract(even, Complex.multiply(w, odd));
            }
        }
    }
    //puts evens in lower half of array, odds in upper
    private static void separate(Complex[] a, int index, int n)
    {
        Complex[] b = new Complex[n/2];
        for (int i = 0; i < n/2; i++) {//put odd in temp storage
            b[i] = a[i * 2 + 1 + index];
        }
        for (int i = 0; i < n/2; i++) {
            a[i + index] = a[i*2 + index];
        }
        for (int i = 0; i < n/2; i++) {
            a[i+n/2 + index] = b[i];
        }
        b = null;
    }
}
class Complex
{
    //format a + bi
    public float a;
    public float b;

    public Complex(float a, float b) {
        this.a = a;
        this.b = b;
    }
    public static Complex multiply(Complex n1, Complex n2)
    {
        return new Complex(n1.a * n1.a - n2.b * n2.b, n1.a * n2.b + n2.a * n1.b);
    }
    public static Complex add(Complex n1, Complex n2)
    {
        return new Complex(n1.a + n2.a, n1.b+ n2.b);
    }
    public static Complex subtract(Complex n1, Complex n2)
    {
        return new Complex(n1.a - n2.a, n1.b - n2.b);
    }
    public static Complex exp(double angle)
    {
        return new Complex((float)Math.cos(angle), (float)Math.sin(angle));
    }
    public float getMag()
    {
        return (float) Math.hypot(a, b);
    }
}