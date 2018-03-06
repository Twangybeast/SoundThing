package twangybeast.myapplication.activities;

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
import twangybeast.myapplication.R;
import twangybeast.myapplication.adapters.NoteFileAdapter;

import java.io.*;

public class BrowseNotesActivity extends AppCompatActivity
{//TODO categories
    public static final String TAG = "BrowseNotesActivity";
    public static final String EXTRA_BROWSING_DIRECTORY = "BrowsingDirectory";
    public static final String MAIN_NOTE_FOLDER = "notes";
    private NoteFileAdapter mAdapter;
    private ListView mList;
    private static final int TRASH_ID = 101;
    private static final int NEW_FOLDER_ID = 104;
    private static final int MOVE_ID = 106;
    private File mDir;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_notes);
        
        Toolbar toolbar = findViewById(R.id.toolbar_browse_notes);
        setSupportActionBar(toolbar);

        String directory = getIntent().getStringExtra(EXTRA_BROWSING_DIRECTORY);
        mDir = new File(directory);
        mAdapter = new NoteFileAdapter(this, mDir.listFiles());
        mList = findViewById(R.id.layoutNotesList);
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (mAdapter.mIsSelecting)
                {
                    CheckBox checkBox = view.findViewById(R.id.checkboxNoteFile);
                    checkBox.performClick();
                    mAdapter.select(position, checkBox.isChecked());
                }
                else
                {
                    Intent intent = new Intent(BrowseNotesActivity.this, NoteEditActivity.class);//TODO differentiate file vs directory
                    intent.putExtra(NoteEditActivity.EXTRA_NOTE_FILE_NAME, mAdapter.getFilePath(position));
                    startActivity(intent);
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
    public static File getDefaultFolder(Context context)
    {
        File dir = new File(context.getFilesDir() + File.pathSeparator + MAIN_NOTE_FOLDER);
        if (!dir.exists() || !dir.isDirectory())
        {
            dir.mkdir();
        }
        return dir;
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
    public void newFolder()
    {
        //TODO
        //TODO Disallow same endings as notes
    }
    public void move()
    {
        //TODO
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
                MenuItem trash = menu.add(Menu.NONE, TRASH_ID, 0, R.string.action_delete);
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
            if (menu.findItem(NEW_FOLDER_ID) == null)
            {
                final MenuItem newFolder = menu.add(Menu.NONE, NEW_FOLDER_ID, 1, R.string.action_new_folder);
                newFolder.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
                newFolder.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        newFolder();
                        return true;
                    }
                });
            }
            if (menu.findItem(MOVE_ID) == null)
            {
                final MenuItem move = menu.add(Menu.NONE, MOVE_ID, 2, R.string.action_move_file);
                move.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER);
                move.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        move();
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
            if (menu.findItem(NEW_FOLDER_ID) != null)
            {
                menu.removeItem(NEW_FOLDER_ID);
            }
            if (menu.findItem(MOVE_ID) != null)
            {
                menu.removeItem(MOVE_ID);
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
