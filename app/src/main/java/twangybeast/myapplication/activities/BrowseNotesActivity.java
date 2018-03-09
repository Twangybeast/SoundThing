package twangybeast.myapplication.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import twangybeast.myapplication.R;
import twangybeast.myapplication.adapters.NoteFileAdapter;
import twangybeast.myapplication.fragments.MoveItemDialog;
import twangybeast.myapplication.fragments.NewFolderDialog;
import twangybeast.myapplication.models.FileItem;
import twangybeast.myapplication.models.NoteFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;

public class BrowseNotesActivity extends AppCompatActivity implements NewFolderDialog.NewFolderListener, MoveItemDialog.MoveListener
{//TODO categories
    public static final String TAG = "BrowseNotesActivity";
    public static final String EXTRA_BROWSING_DIRECTORY = "BrowsingDirectory";
    public static final String EXTRA_TOP_DIRECTORY = "isTopDirectory";
    public static final String MAIN_NOTE_FOLDER = "notes";
    private NoteFileAdapter mAdapter;
    private ListView mList;
    private static final int TRASH_ID = 101;
    private static final int NEW_FOLDER_ID = 104;
    private static final int MOVE_ID = 106;
    private File mDir;
    private ArrayList<File> folders;
    private boolean isTop;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_notes);
        
        Toolbar toolbar = findViewById(R.id.toolbar_browse_notes);
        setSupportActionBar(toolbar);

        String directory = getIntent().getStringExtra(EXTRA_BROWSING_DIRECTORY);
        isTop = getIntent().getBooleanExtra(EXTRA_TOP_DIRECTORY, true);
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
                    if (checkBox == null)
                    {
                        checkBox = view.findViewById(R.id.checkboxFolder);
                    }
                    checkBox.performClick();
                    mAdapter.select(position, checkBox.isChecked());
                }
                else
                {
                    if (mAdapter.mData.get(position) instanceof NoteFile) {
                        Intent intent = new Intent(BrowseNotesActivity.this, NoteEditActivity.class);
                        intent.putExtra(NoteEditActivity.EXTRA_NOTE_FILE_NAME, mAdapter.getFilePath(position));
                        startActivity(intent);
                    }
                    else
                    {
                        Intent intent = new Intent(BrowseNotesActivity.this, BrowseNotesActivity.class);
                        intent.putExtra(BrowseNotesActivity.EXTRA_BROWSING_DIRECTORY, mAdapter.getFilePath(position));
                        intent.putExtra(EXTRA_TOP_DIRECTORY, false);
                        startActivity(intent);
                    }
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
        DialogFragment dialogFragment = new NewFolderDialog();
        dialogFragment.show(getSupportFragmentManager(), "NewFolderDialog");
    }
    public void onNewFolderDialogPositiveClick(DialogFragment dialogFragment)
    {
        Dialog dialog = dialogFragment.getDialog();
        EditText editText = dialog.findViewById(R.id.editTextNewFolderName);
        String folder = editText.getText().toString();
        if (!folder.endsWith(NoteEditActivity.NOTE_FILE_SUFFIX))
        {
            File file = new File(mDir, folder);
            if (!file.exists())
            {
                file.mkdir();
                mAdapter.updateFiles(mDir.listFiles());
                return;
            }
        }
        Toast toast = Toast.makeText(this, R.string.toast_invalid_folder_name, Toast.LENGTH_SHORT);
        toast.show();
    }
    public void onNewFolderDialogNegativeClick(DialogFragment dialog)
    {
        //Does nothing on negative click
    }
    public void move()
    {
        LinkedList<String> folderList = new LinkedList<>();
        folders = new ArrayList<>();
        if (!isTop) {
            folderList.add(getResources().getString(R.string.item_parent_folder));
            folders.add(mDir.getParentFile());
        }
        for (int i =0; i<mAdapter.mFiles.length;i++)
        {
            File file = mAdapter.mFiles[i];
            if (file.exists() && file.isDirectory() && !mAdapter.mData.get(i).isSelected)
            {
                folderList.add(file.getName());
                folders.add(file);
            }
        }
        if (folderList.size() == 0)
        {
            Toast toast = Toast.makeText(this, R.string.toast_no_folder_available, Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        MoveItemDialog dialog = MoveItemDialog.newInstance(folderList.toArray(new String[folderList.size()]));
        dialog.show(getSupportFragmentManager(), "MoveItemDialog");
    }
    public void onMoverDialogOnClick(DialogFragment dialog, int which)
    {
        File targetFolder = folders.get(which);
        for (int i =0;i<mAdapter.getCount();i++)
        {
            FileItem item = mAdapter.mData.get(i);
            if (item.isSelected)
            {
                moveFile(new File(mAdapter.getFilePath(i)), targetFolder);
            }
        }
        mAdapter.updateFiles(mDir.listFiles());
        stopSelecting();
    }
    public static void moveFile(File source, File target)
    {
        if (source.exists()) {
            if (source.isDirectory()) {
                File child = new File(target, source.getName());
                child.mkdir();
                for (File file : source.listFiles())
                {
                    moveFile(file, child);
                }
                source.delete();
            }
            if (source.isFile()) {
                String name = source.getName();
                int index = name.lastIndexOf(".");
                File newFile;
                if (index == -1)
                {
                    newFile = NoteEditActivity.getNewFile(target, name, "");
                }
                else {
                    newFile = NoteEditActivity.getNewFile(target, name.substring(0, index), name.substring(index + 1));
                }
                source.renameTo(newFile);
            }
        }
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
