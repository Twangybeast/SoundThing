package twangybeast.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class RecordSoundNoteActivity extends AppCompatActivity {
    boolean isRecording;
    File file;
    public static final String FILE_PREFIX = "voiceRecording";
    public static final String FILE_SUFFIX = ".wav";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_sound_note);
        isRecording = false;
        chooseSoundFile();
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
    public void resumeRecord()
    {

    }
    public void pauseRecord()
    {

    }
    public void toggleRecord(View v)
    {
        Button button = (Button) v;
        if (isRecording)
        {
            button.setText(getResources().getText(R.string.button_pause_record));

            isRecording = false;
        }
        else
        {
            button.setText(getResources().getText(R.string.button_resume_record));

            isRecording = true;
        }
    }
    public void doneRecord(View v)
    {
        if (isRecording)
        {
            pauseRecord();
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra("fileName", file);
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
