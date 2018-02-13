package twangybeast.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;

public class NoteEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        File file = (File) getIntent().getSerializableExtra(RecordSoundNoteActivity.EXTRA_FILE_NAME);
        ((TextView)findViewById(R.id.textView)).setText(file.getAbsolutePath());
    }
}
