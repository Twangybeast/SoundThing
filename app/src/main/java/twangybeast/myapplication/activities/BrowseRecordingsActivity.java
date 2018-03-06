package twangybeast.myapplication.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.io.File;

import twangybeast.myapplication.R;
import twangybeast.myapplication.adapters.RecordingFileAdapter;

public class BrowseRecordingsActivity extends AppCompatActivity {

    public static final String MAIN_RECORDING_FOLDER = "recordings";
    public static final String TAG = "BrowseRecordingsActivity";
    public static final int REQUEST_PROCESS = 0b1101000001101;
    public static final String EXTRA_VOICE_FILE = "voiceFile";
    private RecordingFileAdapter mAdapter;
    private ListView mList;
    private static final int TRASH_ID = 153;
    private File mDir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_recordings);
        Toolbar toolbar = findViewById(R.id.toolbar_browse_recordings);
        setSupportActionBar(toolbar);

        mDir = new File(RecordSoundNoteActivity.getSoundDirectory(this));
        mAdapter = new RecordingFileAdapter(this, mDir.listFiles());
        mList = findViewById(R.id.listRecordings);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (mAdapter.mIsSelecting)
                {
                    CheckBox checkBox = view.findViewById(R.id.checkboxRecordingFile);
                    checkBox.performClick();
                    mAdapter.select(position, checkBox.isChecked());
                }
                else
                {
                    Intent intent = new Intent(BrowseRecordingsActivity.this, ProcessVoiceActivity.class);
                    intent.putExtra(EXTRA_VOICE_FILE, mAdapter.getFilePath(position));
                    startActivityForResult(intent, REQUEST_PROCESS);
                }
            }
        });
        mList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                boolean alreadySelecting = mAdapter.mIsSelecting;
                startSelecting();
                mAdapter.select(position, true);
                mAdapter.notifyDataSetChanged();
                return !alreadySelecting;
            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_PROCESS)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                data.setClass(this, NoteEditActivity.class);
                startActivity(data);
            }
        }
    }
    @Override
    public void onResume()
    {
        mAdapter.refreshItemInfos();
        mAdapter.notifyDataSetChanged();
        super.onResume();
    }
    public void startSelecting()
    {
        mAdapter.mIsSelecting = true;
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(100);
        mAdapter.clearSelected();
        supportInvalidateOptionsMenu();
    }
    public void stopSelecting()
    {
        mAdapter.mIsSelecting = false;
        mAdapter.clearSelected();
        mAdapter.notifyDataSetChanged();
        supportInvalidateOptionsMenu();
    }
    public void delete()
    {
        mAdapter.deleteSelected();
        mAdapter.updateFiles(mDir.listFiles());
        stopSelecting();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_default, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if (mAdapter.mIsSelecting)
        {
            if (menu.findItem(TRASH_ID) == null)
            {
                MenuItem trash = menu.add(Menu.NONE, TRASH_ID, Menu.NONE, R.string.action_delete);
                trash.setIcon(android.R.drawable.ic_menu_delete);
                trash.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
                trash.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        delete();
                        return true;
                    }
                });
            }
        }
        else
        {
            if (menu.findItem(TRASH_ID) != null)
            {
                menu.removeItem(TRASH_ID);
            }
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }
    @Override
    public void onBackPressed()
    {
        if (mAdapter.mIsSelecting)
        {
            stopSelecting();
        }
        else
        {
            super.onBackPressed();
        }
    }

}
