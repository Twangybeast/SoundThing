package twangybeast.myapplication.activities;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;

import java.io.File;

import twangybeast.myapplication.R;

public class ProcessVoiceActivity extends AppCompatActivity {
    public static final String DEFAULT_FILE_NAME = "Voice Note";
    private ProgressBar mProgress;
    private File voiceFile;
    private File resultFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_process_voice);

        voiceFile = new File(getIntent().getStringExtra(BrowseRecordingsActivity.EXTRA_VOICE_FILE));
        mProgress = findViewById(R.id.progressBar);
        resultFile = NoteEditActivity.getNewFile(BrowseNotesActivity.getDefaultFolder(this), DEFAULT_FILE_NAME, NoteEditActivity.NOTE_FILE_SUFFIX);
    }
    public void doneProcessing()
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(NoteEditActivity.EXTRA_NOTE_FILE_NAME, resultFile.getAbsolutePath());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
    @Override
    public void onBackPressed()
    {
        //TODO Dialogg?
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);
        super.onBackPressed();
        finish();
    }
}
