package twangybeast.myapplication.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import twangybeast.myapplication.soundAnalysis.Complex;

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 */
public class WaveformView extends SurfaceView implements SurfaceHolder.Callback
{
    //TODO Separate waveform view into 2 different views
    Queue<float[]> rawHistory;
    Queue<float[]> fourierHistory;
    Complex[] fourier;
    public static final int MAX_HISTORY = 20;

    public WaveformView(Context context)
    {
        super(context);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public void init()
    {
        setZOrderOnTop(true);
        rawHistory = new LinkedList<>();
        fourier = null;
        getHolder().addCallback(this);
    }

    public float[] updateAudioData(float[] buffer)
    {
        float[] newBuffer;
        synchronized (rawHistory)
        {
            if (rawHistory.size() == MAX_HISTORY)
            {
                newBuffer = rawHistory.poll();
                System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
            }
            else
            {
                newBuffer = buffer.clone();
            }

            rawHistory.offer(newBuffer);
            return newBuffer;
        }
    }

    public void updateDisplay()
    {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null)
        {
            drawAll(canvas);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public void updateFourierValues(Complex[] fourier)
    {
        this.fourier = fourier;
    }

    public void drawAll(Canvas canvas)
    {
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setColor(0xFFFF0000);
        float width = getWidth();
        float height = getHeight() / 4;
        float yCenter = height;
        int pixelX = 0;
        float lastX = -1;
        float lastY = 0;
        synchronized (rawHistory)
        {
            for (float[] buffer : rawHistory)
            {
                for (int x = 0; x < width / MAX_HISTORY; x++)
                {
                    int i = (int) ((x / (width / MAX_HISTORY)) * buffer.length);
                    float sample = buffer[i];
                    float y = (-sample) * height + yCenter;
                    if (lastX != -1)
                    {
                        canvas.drawLine(lastX, lastY, pixelX, y, paint);
                    }
                    lastX = pixelX;
                    lastY = y;
                    pixelX++;
                }
            }
        }
        if (fourier != null) {
            yCenter = height * 3;
            paint.setColor(Color.GRAY);
            canvas.drawLine(0, yCenter, width, yCenter, paint);
            paint.setColor(Color.GREEN);
            drawFourier(canvas, fourier, paint, width, height, yCenter, new FloatFromComplex() {
                @Override
                public float getValue(Complex c) {
                    return c.a;
                }
            });
            paint.setColor(Color.BLUE);
            drawFourier(canvas, fourier, paint, width, height, yCenter, new FloatFromComplex() {
                @Override
                public float getValue(Complex c) {
                    return c.b;
                }
            });
        }
    }

    public static void drawFourier(Canvas canvas, Complex[] fourier, Paint paint, float width, float height, float yCenter, FloatFromComplex func)
    {
        float lastX = -1;
        float lastY = 0;
        if (fourier != null && fourier.length > 0)
        {
            if (fourier.length > width) {
                for (int x = 0; x < width; x++) {
                    int i = (int) ((x / width) * fourier.length);
                    float y = (-func.getValue(fourier[i]) * height) + yCenter;
                    if (lastX != -1) {
                        canvas.drawLine(lastX, lastY, x, y, paint);
                    }
                    lastX = x;
                    lastY = y;
                }
            }
            else
            {
                for (int i = 0; i < fourier.length; i++) {
                    int x = (int) ((i*1.0f /fourier.length) * width);
                    float y = (-func.getValue(fourier[i]) * height) + yCenter;
                    if (lastX != -1)
                    {
                        canvas.drawLine(lastX, lastY, x, y, paint);
                    }
                    lastX = x;
                    lastY = y;
                }
            }
        }
    }

    interface FloatFromComplex
    {
        public float getValue(Complex c);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        updateDisplay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        updateDisplay();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {

    }
}
