package twangybeast.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

/**
 * TODO: document your custom view class.
 */
public class WaveformView extends SurfaceView {

    Queue<short[]> rawHistory;
    float[] fourier;
    public static final int MAX_HISTORY = 20;
    public static final int MAX_AMPLITUDE = 1 << 15 - 1;

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
        rawHistory = new LinkedList<>();
        fourier = null;
    }

    public synchronized void updateAudioData(short[] buffer, boolean update) {
        short[] newBuffer;
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
        paint.setColor(Color.RED);
        float width = getWidth();
        float height = getHeight() / 4;
        float yCenter = height;
        int bufferIndex = 0;
        int pixelX = 0;
        float lastX = -1;
        float lastY = 0;
        for (short[] buffer : rawHistory) {
            for (int x = 0; x < width / MAX_HISTORY; x++) {
                int i = (int) ((x / (width / MAX_HISTORY)) * buffer.length);
                short sample = buffer[i];
                float y = (-sample / MAX_AMPLITUDE) * height + yCenter;
                if (lastX != -1) {
                    canvas.drawLine(lastX, lastY, pixelX, y, paint);
                    System.out.printf("%f %f %d %f\n", lastX, lastY, pixelX, y);
                }
                lastX = pixelX;
                lastY = y;
                pixelX++;
            }
            bufferIndex++;
        }
    }
}
