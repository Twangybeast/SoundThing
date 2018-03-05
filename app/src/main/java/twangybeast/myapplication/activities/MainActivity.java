package twangybeast.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import twangybeast.myapplication.R;

import java.io.File;
import java.io.Serializable;

public class MainActivity extends AppCompatActivity
{
    public static final int REQUEST_SOUND_FILE = 0b110110010110101;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onStartSoundNoteClick(View view)
    {
        Intent intent = new Intent(this, RecordSoundNoteActivity.class);
        startActivity(intent);
    }

    public void onStartTextNoteClick(View view)
    {
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra(NoteEditActivity.EXTRA_NOTE_FILE_NAME, (String)null);
        startActivity(intent);
    }
    public void onBrowseNotesClick(View view)
    {
        Intent intent = new Intent(this, BrowseNotesActivity.class);
        intent.putExtra(BrowseNotesActivity.EXTRA_BROWSING_DIRECTORY, BrowseNotesActivity.getDefaultFolder(this).getAbsolutePath());
        startActivity(intent);
    }
    public void onBrowseVoiceClick(View view)
    {
        Intent intent = new Intent(this, BrowseRecordingsActivity.class);
        startActivity(intent);
    }
}
