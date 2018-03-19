package twangybeast.myapplication.activities;

import android.app.Activity;
import android.arch.core.util.Function;
import android.content.Intent;
import android.icu.text.AlphabeticIndex;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

import edu.cmu.pocketsphinx.*;
import twangybeast.myapplication.R;
import twangybeast.myapplication.soundAnalysis.AudioAnalysis;
import twangybeast.myapplication.soundAnalysis.Complex;
import twangybeast.myapplication.soundAnalysis.TextProcessor;
import twangybeast.myapplication.soundAnalysis.WindowHelper;
import twangybeast.myapplication.util.FileNoteManager;
import twangybeast.myapplication.util.SoundFileManager;
import twangybeast.myapplication.views.FourierHistoryView;
import twangybeast.myapplication.views.FourierView;
import twangybeast.myapplication.views.WaveformView;


public class ProcessVoiceActivity extends AppCompatActivity {
    static {
        System.loadLibrary("pocketsphinx_jni");
    }
    public static final String DEFAULT_FILE_NAME = "Voice Note";
    public static final String TAG = "ProcessVoiceActivity";
    public static final String EXTRA_FROM_VOICE = "noteFromVoice";
    public static final int FOURIER_RADIUS = 256;
    public static final int FOURIER_STEP = 512;
    private ProgressBar mProgress;
    private File voiceFile;
    private File resultFile;
    private Thread worker;
    private Thread progressThread;
    private boolean continueWorking;
    private int progress = 0;
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
        bufferSize += bufferSize % 2;//Make sure % 2 == 0
        byte[] buffer = new byte[bufferSize];
        File assetsDir = new Assets(this).syncAssets();
        Config config = Decoder.defaultConfig();
        config.setFloat("-samprate", RecordSoundNoteActivity.SAMPLE_RATE);

        config.setString("-hmm", new File(assetsDir, "en-us-ptm").getPath());
        config.setString("-lm", new File(assetsDir, "en-us.lm.bin").getPath());
        config.setString("-dict", new File(assetsDir, "cmudict-en-us.dict").getPath());
        config.setBoolean("-allphone_ci", true);

        Decoder decoder = new Decoder(config);
        decoder.startUtt();
        //Necessary method?
        short[] header = SoundFileManager.convertBytesToShorts(SoundFileManager.getWavHeader((int)voiceFile.length(), RecordSoundNoteActivity.SAMPLE_RATE));
        //decoder.processRaw(header, header.length, false, false);

        short[] shorts = new short[buffer.length/2];
        float[] floats = new float[shorts.length];
        while (continueWorking && in.available() > 0)
        {
            int amountRead = in.read(buffer);
            for (int i = 0; i < amountRead/2; i++) {
                shorts[i] = (short)(( buffer[i*2+1] & 0xff )|( buffer[i*2] << 8 ));
            }
            AudioAnalysis.toFloatArray(shorts, floats, RecordSoundNoteActivity.MAX_AMPLITUDE, amountRead/2);
            waveform.updateAudioData(floats);
            waveform.updateDisplay();
            decoder.processRaw(shorts, amountRead/2, false, false);
            //processor.processBytes(buffer, amountRead);
            progress += amountRead;
        }
        decoder.endUtt();
        String result = decoder.hyp().getHypstr();
        NoteEditActivity.writeString(out, result);
        //System.out.println("Hyp:"+decoder.hyp().getHypstr());
        /*
        for (Segment seg : decoder.seg()) {
            System.out.println(seg.getWord());
        }*/
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
        returnIntent.putExtra(EXTRA_FROM_VOICE, true);
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
