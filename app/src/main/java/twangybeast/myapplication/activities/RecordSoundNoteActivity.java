package twangybeast.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import twangybeast.myapplication.soundAnalysis.AudioAnalysis;
import twangybeast.myapplication.soundAnalysis.Complex;
import twangybeast.myapplication.R;
import twangybeast.myapplication.views.WaveformView;

import java.io.BufferedOutputStream;
import java.io.File;

public class RecordSoundNoteActivity extends AppCompatActivity
{
    File file;
    public static final String FILE_PREFIX = "voiceRecording";
    public static final String FILE_SUFFIX = ".wav";
    public static final String EXTRA_SOUND_FILE_NAME = "soundFileName";
    public static int SAMPLE_RATE = new int[]{16000, 44100}[0];
    public static int CHANNEL = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    public static int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int MAX_AMPLITUDE = 1 << 15 - 1;
    int bufferSize = 0;
    AudioRecord audioRecord;
    BufferedOutputStream os;
    boolean isRecording = false;
    Thread recordThread;
    Thread displayThread;
    long totalRead = 0;
    int currentTime = 0;
    private float mLastFourierMax = 1;
    TextView time;
    WaveformView waveView;
    Object displayArrayLock = new Object();
    float[] displayArr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sound_note);
        isRecording = false;
        chooseSoundFile();
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);
        bufferSize = Math.max(4096, bufferSize);
        time = findViewById(R.id.textSoundLength);
        waveView = findViewById(R.id.WaveView);
    }

    public void chooseSoundFile()
    {
        int i = 0;
        do
        {
            file = new File(this.getExternalFilesDir(null), String.format("%s%d%s", FILE_PREFIX, i, FILE_SUFFIX));
            i++;
        }
        while (file.exists());
    }

    public void recordBytes()
    {
        short[] data = new short[bufferSize / 2];
        displayArr = new float[data.length];
        while (isRecording)
        {
            int amountRead = audioRecord.read(data, 0, data.length);
            long t1 = System.currentTimeMillis();
            updateBytesToDisplay(data, amountRead);
            int cycleTime = (int) (System.currentTimeMillis() - t1);
            processTime(amountRead);
            if (cycleTime > 20)
            {
                System.out.println(cycleTime);
            }
        }
    }

    public void updateBytesToDisplay(short[] in, int amount)
    {
        displayArr = AudioAnalysis.toFloatArray(in, MAX_AMPLITUDE, amount);
        waveView.updateAudioData(displayArr);
    }

    public void processBytes(float[] in, int amount)
    {
        float[] data = in;
        int N = Integer.highestOneBit(amount);
        Complex[] fourier = new Complex[N];
        AudioAnalysis.calculateFourier(data, fourier, N);
        //fourier = AudioAnalysis.getUpperHalf(fourier);
        fourier = AudioAnalysis.getRange(fourier, 0, 300);
        AudioAnalysis.restrictComplexArray(fourier, N/4);
        waveView.updateFourierValues(fourier);
    }

    public void runDisplayBytes()
    {
        while (displayArr == null)
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        while (isRecording)
        {
            processBytes(displayArr, displayArr.length);
            waveView.updateDisplay();
            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void processTime(int amountRead)
    {
        totalRead += amountRead;
        //TODO Mod and divide at same time
        if (totalRead >= SAMPLE_RATE)
        {
            currentTime += totalRead / SAMPLE_RATE;
            totalRead %= SAMPLE_RATE;
            time.post(new Runnable()
            {
                @Override
                public void run()
                {
                    time.setText(getTime(currentTime));
                }
            });
        }
    }

    public static String getTime(int time)
    {
        return String.format("%02d:%02d", time / 60, time % 60);
    }

    public void resumeRecord()
    {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize);
        isRecording = true;
        recordThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                recordBytes();
            }
        });
        displayThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                android.os.Process.setThreadPriority(10);
                runDisplayBytes();
            }
        });
        audioRecord.startRecording();
        recordThread.start();
        displayThread.start();
    }

    public void pauseRecord()
    {
        isRecording = false;
        try
        {
            recordThread.join();
            displayThread.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        audioRecord.stop();
        recordThread = null;
        displayThread = null;
    }

    public void toggleRecord(View v)
    {
        Button button = (Button) v;
        if (!isRecording)
        {
            button.setText(getResources().getText(R.string.button_pause_record));
            resumeRecord();
        }
        else
        {
            button.setText(getResources().getText(R.string.button_resume_record));
            pauseRecord();
        }
    }

    public void doneRecord(View v)
    {
        if (isRecording)
        {
            pauseRecord();
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra(RecordSoundNoteActivity.EXTRA_SOUND_FILE_NAME, file);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onBackPressed()
    {
        doneRecord(null);
        super.onBackPressed();
    }
}
