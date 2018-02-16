package twangybeast.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.File;

public class RecordSoundNoteActivity extends AppCompatActivity{
    File file;
    public static final String FILE_PREFIX = "voiceRecording";
    public static final String FILE_SUFFIX = ".wav";
    public static final String EXTRA_FILE_NAME = "fileName";
    public static int SAMPLE_RATE = 16000;
    public static int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    public static int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int MAX_AMPLITUDE = 1 << 15 - 1;
    int bufferSize = 0;
    AudioRecord audioRecord;
    BufferedOutputStream os;
    boolean isRecording = false;
    Thread recordThread;
    long totalRead = 0;
    int currentTime = 0;
    private float mLastFourierMax = 0;
    TextView time;
    WaveformView waveView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        do {
            file = new File(this.getExternalFilesDir(null), String.format("%s%d%s", FILE_PREFIX, i, FILE_SUFFIX));
            i++;
        }
        while (file.exists());
    }
    public void recordBytes()
    {
        short[] data = new short[bufferSize/4];
        while (isRecording)
        {
            int amountRead = audioRecord.read(data, 0, data.length);
            processBytes(data, amountRead);
            processTime(amountRead);
        }
    }
    public void processBytes(short[] in, int amount)
    {
        float[] data = AudioAnalysis.toFloatArray(in, MAX_AMPLITUDE);
        int N = Integer.highestOneBit(amount);
        float[] fourier = new float[N];
        float max = AudioAnalysis.calculateFourier(data, fourier, N);
        if (mLastFourierMax < max)
        {
            mLastFourierMax = max;
        }
        AudioAnalysis.restrictFloatArray(fourier, mLastFourierMax);
        waveView.updateFourierValues(fourier);
        waveView.updateAudioData(data, true);
    }
    public void processTime(int amountRead)
    {
        totalRead += amountRead;
        //TODO Mod and divide at same time
        if (totalRead >= SAMPLE_RATE)
        {
            currentTime += totalRead / SAMPLE_RATE;
            totalRead %= SAMPLE_RATE;
            time.post(new Runnable() {
                @Override
                public void run() {
                    time.setText(getTime(currentTime));
                }
            });
        }
    }
    public static String getTime(int time)
    {
        return String.format("%02d:%02d", time/60, time%60);
    }
    public void resumeRecord()
    {
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL, ENCODING, bufferSize);
        isRecording = true;
        recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                recordBytes();
            }
        });
        audioRecord.startRecording();
        recordThread.start();
    }
    public void pauseRecord()
    {
        isRecording = false;
        try {
            recordThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        audioRecord.stop();
        recordThread = null;
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
        returnIntent.putExtra(RecordSoundNoteActivity.EXTRA_FILE_NAME, file);
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
