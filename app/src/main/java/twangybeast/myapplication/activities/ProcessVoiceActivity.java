package twangybeast.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import twangybeast.myapplication.R;
import twangybeast.myapplication.soundAnalysis.AudioAnalysis;
import twangybeast.myapplication.soundAnalysis.Complex;
import twangybeast.myapplication.soundAnalysis.WindowHelper;
import twangybeast.myapplication.views.FourierHistoryView;
import twangybeast.myapplication.views.FourierView;
import twangybeast.myapplication.views.WaveformView;

public class ProcessVoiceActivity extends AppCompatActivity {
    public static final String DEFAULT_FILE_NAME = "Voice Note";
    public static final int FOURIER_RADIUS = 1024;
    public static final int FOURIER_STEP = 512;
    private ProgressBar mProgress;
    private File voiceFile;
    private File resultFile;
    private Thread worker;
    private Thread progressThread;
    private boolean continueWorking;
    private int progress = 0;
    private WaveformView waveform;
    private FourierHistoryView fourierView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_voice);

        waveform = findViewById(R.id.WaveViewProcess);
        fourierView = findViewById(R.id.FourierViewProcess);
        voiceFile = new File(getIntent().getStringExtra(BrowseRecordingsActivity.EXTRA_VOICE_FILE));
        mProgress = findViewById(R.id.progressBar);
        resultFile = NoteEditActivity.getNewFile(BrowseNotesActivity.getDefaultFolder(this), DEFAULT_FILE_NAME, NoteEditActivity.NOTE_FILE_SUFFIX);
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    process();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        progressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (continueWorking) {
                    mProgress.setProgress(progress);
                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mProgress.setMax((int) voiceFile.length());
        continueWorking = true;
        worker.start();
        progressThread.start();
    }
    public void process() throws IOException {
        DataInputStream in = new DataInputStream(new FileInputStream(voiceFile));
        DataOutputStream out = new DataOutputStream(new FileOutputStream(resultFile));
        NoteEditActivity.writeString(out, getDefaultTitle());
        int bufferSize= Math.max(4096, AudioTrack.getMinBufferSize(RecordSoundNoteActivity.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT));
        bufferSize += bufferSize % 2;//Make sure % 2 == 0
        byte[] buffer = new byte[bufferSize/4];
        short[] shorts = new short[buffer.length/2];
        float[] floats = new float[shorts.length];
        LinkedList<float[]> history = new LinkedList<>();
        int currentPosition = 0;
        final int N = Integer.highestOneBit(FOURIER_RADIUS * 2 + 1);
        float[] windowed = new float[N];
        Complex[] complexes = new Complex[N];
        WindowHelper windowHelper = new WindowHelper(N);
        while (continueWorking && in.available() > 0)
        {
            //TODO actually process
            int amountRead = in.read(buffer);
            for (int i = 0; i < amountRead/2; i++) {
                shorts[i] = (short)(( buffer[i*2+1] & 0xff )|( buffer[i*2] << 8 ));
            }
            //TODO Use window function on fourier transform & with more resolution
            AudioAnalysis.toFloatArray(shorts, floats, RecordSoundNoteActivity.MAX_AMPLITUDE, amountRead/2);
            float[] historyItem = waveform.updateAudioData(floats);//Get float array from waveform to save memory
            history.offer(historyItem);
            while (currentPosition - FOURIER_RADIUS + N < history.size() * shorts.length)
            {
                int index = currentPosition - FOURIER_RADIUS;
                int count = 0;
                if (index < 0)
                {
                    count -= index;//Ensure positive
                    index = 0;
                }
                SampleLoop:
                for (float[] samples : history)
                {
                    for (; index < samples.length; index++) {
                        windowed[count] = samples[index] * windowHelper.getValue(count);
                        count++;
                        if (!(count < N))
                        {
                            break SampleLoop;
                        }
                    }
                    index -= samples.length;
                }
                currentPosition += FOURIER_STEP;
                AudioAnalysis.calculateFourier(windowed, complexes, N);
                Complex[] fourier = AudioAnalysis.getRange(complexes, 0, (N/2)/8);
                //AudioAnalysis.restrictComplexArray(fourier, N/4);
                AudioAnalysis.complexToFloat(fourier, windowed, fourier.length);
                AudioAnalysis.restrictFloatArray(windowed, AudioAnalysis.getMax(windowed));
                fourierView.updateFourierValues(windowed);
                fourierView.updateDisplay();
            }
            if (currentPosition - FOURIER_RADIUS > shorts.length)
            {
                history.poll();
                currentPosition -= shorts.length;
            }

            waveform.updateDisplay();
            progress += amountRead;
        }
        out.flush();
        out.close();
        if (in.available() <= 0)
        {
            doneProcessing();
        }
    }
    public void doneProcessing()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(NoteEditActivity.EXTRA_NOTE_FILE_NAME, resultFile.getAbsolutePath());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
    public String getDefaultTitle()
    {
        final String DEFAULT_TITLE = "Note From Voice";
        return DEFAULT_TITLE;
    }
    public void interruptStop()
    {
        continueWorking = false;
        try {
            worker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }
    @Override
    public void onBackPressed()
    {
        //TODO Dialog?
        interruptStop();
        super.onBackPressed();
    }
}
