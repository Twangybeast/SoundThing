package twangybeast.myapplication.activities;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import twangybeast.myapplication.R;
import twangybeast.myapplication.views.NoteSummaryView;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;

public class BrowseNotesActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final String TAG = "BrowseNotesActivity";
    NoteSummaryView[] noteSummaryViews;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_notes);

        generateNotesList();
    }
    @Override
    public void onResume()
    {
        generateNotesList();
        super.onResume();
    }
    public void generateNotesList()
    {
        File dir = getFilesDir();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        ViewGroup layout = findViewById(R.id.layoutNotesList);
        layout.removeAllViews();
        File[] files = dir.listFiles();
        noteSummaryViews = new NoteSummaryView[files.length];
        for (int i =0 ;i<files.length;i++)
        {
            try
            {
                File file = files[i];
                NoteSummaryView noteSummaryView = new NoteSummaryView(this);
                DataInputStream in = new DataInputStream(new FileInputStream(file));
                noteSummaryView.setNoteTitle(NoteEditActivity.readString(in));
                in.close();
                long time = file.lastModified();
                noteSummaryView.setLastModified(dateFormat.format(new Date(time)));
                noteSummaryView.setFilePath(file.getPath());
                noteSummaryView.setOnClickListener(this);
                noteSummaryView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                layout.addView(noteSummaryView);
                noteSummaryViews[i] = noteSummaryView;
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error loading files to view.");
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onClick(View v)
    {
        NoteSummaryView noteSummaryView = (NoteSummaryView)(v);
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.putExtra(NoteEditActivity.EXTRA_NOTE_FILE_NAME, noteSummaryView.getFilePath());
        startActivity(intent);
    }
}
