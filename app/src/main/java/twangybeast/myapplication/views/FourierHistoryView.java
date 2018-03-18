package twangybeast.myapplication.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Random;

import twangybeast.myapplication.soundAnalysis.Complex;
import twangybeast.myapplication.util.HeatColor;

/**
 * Created by cHeNdAn19 on 3/14/2018.
 */

public class FourierHistoryView extends SurfaceView implements SurfaceHolder.Callback{
    LinkedList<float[]> fourierHistory;
    private int added=0;
    private int removed = 0;
    public static final int MAX_HISTORY = 40;
    private Bitmap lastBitmap;
    private Bitmap cachedBitmap;
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
            Canvas drawingCanvas = new Canvas(cachedBitmap);
            drawAll(drawingCanvas);
            canvas.drawBitmap(cachedBitmap, 0, 0, new Paint());
            Bitmap temp = lastBitmap;//Switches lastBitmap & cachedBitmap to reuse the same 2 bitmaps w/o reallocating memory each time
            lastBitmap = cachedBitmap;
            cachedBitmap = temp;
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
    public void drawAll(Canvas canvas)
    {
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setColor(0xFFFF0000);
        float width = getWidth();
        int x = fourierHistory.size() - added;
        float xStep = width/(MAX_HISTORY);
        float height = getHeight();
        canvas.drawBitmap(lastBitmap, -(removed*xStep), 0, paint);
        removed = 0;
        //TODO Efficient drawing
        synchronized (fourierHistory)
        {
            if (!fourierHistory.isEmpty())
            {
                ListIterator<float[]> iter = fourierHistory.listIterator(x);
                while (iter.hasNext())
                {
                    float[] fourier = iter.next();
                    for (int y = 0; y < height; y++)
                    {
                        int index = (int) ((y / height) * fourier.length);
                        paint.setColor(HeatColor.interpolateColor(HeatColor.COLORS_HEATMAP_5, fourier[index]));
                        canvas.drawLine((x * xStep), y, (x + 1) * xStep, y, paint);
                    }
                    x++;
                }
            }
            added = 0;
        }
    }
    public void updateFourierValues(float[] fourier)
    {
        float[] newFourier;
        synchronized (fourierHistory)
        {
            added++;
            if (fourierHistory.size() == MAX_HISTORY)
            {
                newFourier = fourierHistory.poll();
                System.arraycopy(fourier, 0, newFourier, 0, fourier.length);
                removed++;
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
        lastBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        cachedBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        updateDisplay();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
