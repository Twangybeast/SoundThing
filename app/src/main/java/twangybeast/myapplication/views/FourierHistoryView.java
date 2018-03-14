package twangybeast.myapplication.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.Queue;

import twangybeast.myapplication.soundAnalysis.Complex;
import twangybeast.myapplication.util.HeatColor;

/**
 * Created by cHeNdAn19 on 3/14/2018.
 */

public class FourierHistoryView extends SurfaceView implements SurfaceHolder.Callback{
    Queue<float[]> fourierHistory;
    public static final int MAX_HISTORY = 20;

    public FourierHistoryView(Context context)
    {
        super(context);
        init();
    }

    public FourierHistoryView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    public FourierHistoryView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        init();
    }

    public void init()
    {
        setZOrderOnTop(true);
        fourierHistory = new LinkedList<>();
        getHolder().addCallback(this);
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
    public void drawAll(Canvas canvas)
    {
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setColor(0xFFFF0000);
        float width = getWidth();
        int x = 0;
        float xStep = width/(MAX_HISTORY+1);
        float height = getHeight();
        synchronized (fourierHistory)
        {
            for (float[] fourier: fourierHistory)
            {
                for (int y =0;y<height;y++)
                {
                    int index = (int) ((y / height) * fourier.length);
                    paint.setColor(HeatColor.interpolateColor(HeatColor.COLORS_HEATMAP_5, fourier[index]));
                    canvas.drawLine((x*xStep), y, (x+1)*xStep, y, paint);
                }
                x++;
            }
        }
    }
    public void updateFourierValues(float[] fourier)
    {
        float[] newFourier;
        synchronized (fourierHistory)
        {
            if (fourierHistory.size() == MAX_HISTORY)
            {
                newFourier = fourierHistory.poll();
                System.arraycopy(fourier, 0, newFourier, 0, fourier.length);
            }
            else
            {
                newFourier = fourier.clone();
            }

            fourierHistory.offer(newFourier);
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        updateDisplay();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
