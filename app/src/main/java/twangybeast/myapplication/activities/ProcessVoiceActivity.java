package twangybeast.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import twangybeast.myapplication.R;
import twangybeast.myapplication.soundAnalysis.AudioAnalysis;
import twangybeast.myapplication.views.WaveformView;

public class ProcessVoiceActivity extends AppCompatActivity {
    public static final String DEFAULT_FILE_NAME = "Voice Note";
    private ProgressBar mProgress;
    private File voiceFile;
    private File resultFile;
    private Thread worker;
    private Thread progressThread;
    private boolean continueWorking;
    private int progress = 0;//TODO Progress bar
    private WaveformView waveform;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_voice);

        waveform = findViewById(R.id.WaveViewProcess);
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
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, RecordSoundNoteActivity.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);
        audioTrack.play();
        byte[] buffer = new byte[bufferSize/4];
        short[] shorts = new short[buffer.length/2];
        while (continueWorking && in.available() > 0)
        {
            int amountRead = in.read(buffer);
            for (int i = 0; i < amountRead/2; i++) {
                shorts[i] = (short)(( buffer[i*2+1] & 0xff )|( buffer[i*2] << 8 ));
            }
            float[] floats = AudioAnalysis.toFloatArray(shorts, RecordSoundNoteActivity.MAX_AMPLITUDE, amountRead/2);
            waveform.updateAudioData(floats);
            waveform.updateDisplay();
            audioTrack.write(shorts, 0, amountRead/2);
            progress += amountRead;
        }
        audioTrack.pause();
        audioTrack.release();
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
