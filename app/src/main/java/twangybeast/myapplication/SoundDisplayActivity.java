package twangybeast.myapplication;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class SoundDisplayActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    SurfaceView surface = null;
    SurfaceHolder sHolder = null;
    private int bufferSize;
    boolean isRecording = false;
    AudioRecord recorder = null;
    Thread recordThread;
    Queue<Float> toDisplay;
    short[] displayData;
    int curPosition=0;
    int MAX_ON_DISPLAY = 44000;
    int maxVal = 1 << 15;
    BufferedOutputStream os;
    File file;
    boolean isPlaying=false;
    Thread playbackThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_display);
        toDisplay = new LinkedList<>();
        displayData = new short[MAX_ON_DISPLAY];
        file = new File(getExternalFilesDir(null).getPath(), "audiodataasdfasfd.raw");

        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
        bufferSize = Math.max(bufferSize, 1024 * 2);
        surface = findViewById(R.id.surfaceView);
        sHolder = surface.getHolder();
        sHolder.addCallback(this);
    }
    public void play()
    {
        isPlaying = true;
        if (isRecording)
        {
            stopRecorder();
        }
        int bufferSize = 4096;
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, RECORDER_SAMPLERATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, RECORDER_AUDIO_ENCODING, bufferSize, AudioTrack.MODE_STREAM);
        BufferedInputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            byte[] data = new byte[bufferSize / 4];
            audioTrack.play();
            while (!Thread.interrupted() && is.available() > 0)
            {
                int bytesRead = is.read(data, 0, data.length);
                audioTrack.write(data, 0, bytesRead);
            }
            is.close();
            audioTrack.release();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Button button = ((Button)findViewById(R.id.button));
        button.post(new Runnable() {
            @Override
            public void run() {
                ((Button)findViewById(R.id.button)).setText("Start Playback");
            }
        });
        isPlaying = false;
    }
    public void startPlay()
    {
        playbackThread = new Thread(new Runnable() {
            @Override
            public void run() {
                play();
            }
        });
        playbackThread.start();
    }
    public void stopPlay()
    {
        playbackThread.interrupt();
        playbackThread = null;
        isPlaying = false;
        ((Button)findViewById(R.id.button)).setText("Start Playback");
    }
    public void clickPlay(View v)
    {
        Button b = (Button) v;
        if (playbackThread == null)
        {
            b.setText("Stop Playback");
            startPlay();
        }
        else
        {
            b.setText("Start Playback");
            stopPlay();
        }
    }
    public void record()
    {
        short[] data = new short[bufferSize/4];
        while (isRecording)
        {
            int shortsRead = recorder.read(data, 0, data.length);
            draw(data, shortsRead);
            saveData(data, shortsRead);
        }
    }
    public void startRecorder()
    {
        if (!isPlaying) {
            try {
                os = new BufferedOutputStream(new FileOutputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }
            recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
            recorder.startRecording();
            isRecording = true;
            recordThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    record();
                }
            });
            recordThread.start();
        }
    }
    public void stopRecorder()
    {
        if (recorder != null)
        {
            recorder.stop();
            recorder.release();
            recorder = null;
            try {
                recordThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                os.flush();
                os.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        isRecording = false;
    }
    public void onClick(View v)
    {
        Button buttonView = (Button) v;
        if (isRecording)
        {
            isRecording = false;
            buttonView.setText("Start");
            stopRecorder();
        }
        else
        {

            isRecording = true;
            buttonView.setText("Stop");
            startRecorder();
        }

    }
    public void saveData(short[] data, int dataAmount)
    {
        if (data != null)
        {
            ByteBuffer bb = ByteBuffer.allocate(dataAmount * 2);
            for (int i = 0; i < dataAmount; i++) {
                bb.putShort(data[i]);
            }
            try {
                os.write(bb.array());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    synchronized public void draw(short[] data, int dataAmount)
    {
        Canvas canvas = sHolder.lockCanvas();
        if (canvas != null) {
            if (data != null) {
                if (displayData.length - curPosition >= dataAmount) {
                    System.arraycopy(data, 0, displayData, curPosition, dataAmount);
                    curPosition += dataAmount;
                    if (curPosition >= displayData.length) {
                        curPosition -= displayData.length;
                    }
                } else {
                    int firstAmount = displayData.length - curPosition;
                    System.arraycopy(data, 0, displayData, curPosition, firstAmount);
                    System.arraycopy(data, firstAmount, displayData, 0, dataAmount - firstAmount);
                    curPosition = dataAmount - firstAmount;
                }
            }
            canvas.drawColor(0xFFFFFFFF);
            Paint paint = new Paint();
            paint.setColor(0xFFFF0000);
            int width = surface.getWidth();
            int center = surface.getHeight()/2;
            int mag = (int) (center*0.75f);
            int index = 0;
            for (int i = curPosition; i < displayData.length; i++) {
                float x = (width * index / MAX_ON_DISPLAY);
                float height = (mag * -(displayData[i] * 1.0f / maxVal)) + center;
                canvas.drawLine(x, center, x, height, paint);
                index++;
            }
            for (int i = 0; i < displayData.length; i++) {
                float x = (width * index / MAX_ON_DISPLAY);
                float height = (mag * -(displayData[i] * 1.0f / maxVal)) + center;
                canvas.drawLine(x, center, x, height, paint);
                index++;
            }
            sHolder.unlockCanvasAndPost(canvas);
        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        draw(null, 0);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public static int getSampleFromBytes(byte[] bytes, boolean bigEndian)
    {
        int result = 0;
        if (bigEndian)
        {
            for (int i = 0; i < bytes.length; i++)
            {
                result = result << 8;
                result += bytes[i] & 0xFF;
            }
        }
        else
        {
            for (int i = bytes.length; i -->0;)
            {
                result = result << 8;
                result += bytes[i] & 0xFF;
            }
        }
        return result;
    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        recordThread.interrupt();
    }
}
