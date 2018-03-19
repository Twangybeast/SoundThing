package twangybeast.myapplication.activities;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Process;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import twangybeast.myapplication.R;
import twangybeast.myapplication.soundAnalysis.AudioAnalysis;
import twangybeast.myapplication.soundAnalysis.Complex;
import twangybeast.myapplication.views.FourierView;
import twangybeast.myapplication.views.WaveformView;

import java.io.*;

public class RecordSoundNoteActivity extends AppCompatActivity
{
    File file;
    public static final String VOICE_FILE_NAME = "Voice Note ";
    public static final String VOICE_FILE_SUFFIX = ".pcm";
    public static int SAMPLE_RATE = new int[]{16000, 8000}[0];
    public static int CHANNEL = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    public static int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int MAX_AMPLITUDE = 1 << 15 - 1;
    int bufferSize = 0;
    AudioRecord audioRecord;
    boolean isRecording = false;
    Thread recordThread;
    Thread displayThread;
    long totalRead = 0;
    TextView time;
    WaveformView waveView;
    FourierView fourierView;
    float[] displayArr = null;
    int currentTime = 0;
    DataOutputStream out;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sound_note);
        isRecording = false;
        out = null;
        chooseSoundFile();
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL, ENCODING);
        bufferSize = Math.max(4096, bufferSize);
        time = findViewById(R.id.textSoundLength);
        waveView = findViewById(R.id.WaveView);
        fourierView = findViewById(R.id.FourierView);
    }

    public static String getSoundDirectory(Context context)
    {
        File file = new File(context.getExternalFilesDir(null) + File.separator + BrowseRecordingsActivity.MAIN_RECORDING_FOLDER);
        if (!file.exists() || !file.isDirectory())
        {
            file.mkdir();
        }
        return file.getAbsolutePath();
    }

    public void chooseSoundFile()
    {
        int i = 1;
        do
        {
            file = new File(getSoundDirectory(this), String.format("%s(%d)%s", VOICE_FILE_NAME, i, VOICE_FILE_SUFFIX));
            i++;
        }
        while (file.exists());
    }

    public static int getSecondsFromBytes(long bytes)
    {
        return (int) ((bytes / 2) / SAMPLE_RATE);
    }

    public void recordBytes()
    {
        short[] data = new short[bufferSize / 2];
        displayArr = new float[data.length];
        if (out == null)
        {
            try
            {
                out = new DataOutputStream(new FileOutputStream(file));
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        while (isRecording)
        {
            int amountRead = audioRecord.read(data, 0, data.length);
            updateBytesToDisplay(data, amountRead);
            writeBytes(out, data, amountRead);
            processTime(amountRead);
        }
    }

    public static void writeBytes(DataOutputStream out, short[] data, int amount)
    {
        try
        {
            for (int i = 0; i < amount; i++)
            {
                out.writeShort(data[i]);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
        Complex[] restricted = new Complex[(N/2)/8];
        AudioAnalysis.getRange(fourier, restricted, 0, restricted.length);
        AudioAnalysis.restrictComplexArray(restricted, N / 4);
        fourierView.updateFourierValues(restricted);
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
            fourierView.updateDisplay();
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
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
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
        if (out != null)
        {
            try
            {
                out.flush();
                out.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        finish();
    }

    @Override
    public void onBackPressed()
    {
        doneRecord(null);
        super.onBackPressed();
    }
}
