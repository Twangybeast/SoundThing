package twangybeast.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO: document your custom view class.
 */
public class WaveformView extends SurfaceView implements SurfaceHolder.Callback{

    Queue<float[]> rawHistory;
    float[] fourier;
    public static final int MAX_HISTORY = 20;

    public WaveformView(Context context) {
        super(context);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void init() {
        setZOrderOnTop(true);
        rawHistory = new LinkedList<>();
        fourier = null;
        getHolder().addCallback(this);
    }

    public synchronized void updateAudioData(float[] buffer, boolean update) {
        float[] newBuffer;
        if (rawHistory.size() == MAX_HISTORY) {
            newBuffer = rawHistory.poll();
            System.arraycopy(buffer, 0, newBuffer, 0, buffer.length);
        } else {
            newBuffer = buffer.clone();
        }

        rawHistory.offer(newBuffer);
        if (update) {
            updateDisplay();
        }
    }

    public void updateDisplay() {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            drawAll(canvas);
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public synchronized void updateFourierValues(float[] fourier) {
        this.fourier = fourier;
    }

    public void drawAll(Canvas canvas) {
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint();
        paint.setColor(0xFFFF0000);
        float width = getWidth();
        float height = getHeight() / 4;
        float yCenter = height;
        int bufferIndex = 0;
        int pixelX = 0;
        float lastX = -1;
        float lastY = 0;
        for (float[] buffer : rawHistory) {
            for (int x = 0; x < width / MAX_HISTORY; x++) {
                int i = (int) ((x / (width / MAX_HISTORY)) * buffer.length);
                float sample = buffer[i];
                float y = (-sample) * height + yCenter;
                if (lastX != -1) {
                    canvas.drawLine(lastX, lastY, pixelX, y, paint);
                    //System.out.printf("%f %f %d %f\n", lastX, lastY, pixelX, y);
                }
                lastX = pixelX;
                lastY = y;
                pixelX++;
            }
            bufferIndex++;
        }
        lastX = -1;
        paint.setColor(Color.GREEN);
        yCenter = height * 3;
        if (fourier != null && fourier.length > 0) {
            for (int x = 0; x < width; x++) {
                int i = (int) ((x / width) * fourier.length);
                float y = (-fourier[i] * height) + yCenter;
                if (lastX != -1) {
                    canvas.drawLine(lastX, lastY, x, y, paint);
                }
                lastX = x;
                lastY = y;
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        updateDisplay();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        updateDisplay();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
