package twangybeast.myapplication.soundAnalysis;

/**
 * Created by Twangybeast on 2/18/2018.
 */

public class Complex
{
    //format a + bi
    public float a;
    public float b;

    public Complex(float a, float b)
    {
        this.a = a;
        this.b = b;
    }

    public Complex plus(Complex n)
    {
        return Complex.add(this, n);
    }

    public Complex minus(Complex n)
    {
        return Complex.subtract(this, n);
    }

    public Complex times(Complex n)
    {
        return Complex.multiply(this, n);
    }

    public Complex scale(float factor)
    {
        return new Complex(this.a * factor, this.b * factor);
    }

    public Complex conjugate()
    {
        return new Complex(this.a, -this.b);
    }

    public Complex divide(float divisor)
    {
        return new Complex(this.a / divisor, this.b / divisor);
    }

    public static Complex multiply(Complex n1, Complex n2)
    {
        return new Complex(n1.a * n2.a - n1.b * n2.b, n1.a * n2.b + n2.a * n1.b);
    }

    public static Complex add(Complex n1, Complex n2)
    {
        return new Complex(n1.a + n2.a, n1.b + n2.b);
    }

    public static Complex subtract(Complex n1, Complex n2)
    {
        return new Complex(n1.a - n2.a, n1.b - n2.b);
    }

    public static Complex exp(double angle)
    {
        return new Complex((float) Math.cos(angle), (float) Math.sin(angle));
    }

    public float getMag()
    {
        return (float) Math.sqrt(a*a+ b*b);
    }

    @Override
    public String toString()
    {
        return String.format("%5.5f  \t %5.5f", a, b);
    }

    public static float[] getReal(Complex[] X)
    {
        float[] re = new float[X.length];
        for (int i = 0; i < X.length; i++)
        {
            re[i] = X[i].a;
        }
        return re;
    }

    public static float[] getImaginary(Complex[] X)
    {
        float[] im = new float[X.length];
        for (int i = 0; i < X.length; i++)
        {
            im[i] = X[i].b;
        }
        return im;
    }

    public static float[] getMagnitude(Complex[] X)
    {
        float[] mag = new float[X.length];
        for (int i = 0; i < X.length; i++)
        {
            mag[i] = X[i].getMag();
        }
        return mag;
    }

    public static Complex[] getComplex(float[] re, int N)
    {
        Complex[] c = new Complex[N];
        for (int i = 0; i < N; i++)
        {
            c[i] = new Complex(re[i], 0);
        }
        return c;
    }
}