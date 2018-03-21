package twangybeast.myapplication.activities;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import twangybeast.myapplication.R;

import java.io.*;

public class NoteEditActivity extends AppCompatActivity
{
    public static final String EXTRA_NOTE_FILE_NAME = "noteFileName";
    public static final String NOTE_FILE_SUFFIX = ".note";
    public static final String TAG = "NoteEditActivity";
    private boolean mChanged;
    EditText mNoteTitle;
    EditText mNoteBody;
    File mNoteFile;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar3));
        getSupportActionBar().setTitle(R.string.app_name);


        mNoteTitle = findViewById(R.id.editNoteTitle);
        mNoteBody = findViewById(R.id.editNoteBody);
        TextWatcher textChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mChanged = true;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
        mNoteTitle.addTextChangedListener(textChangedListener);
        mNoteBody.addTextChangedListener(textChangedListener);
        String noteFilePath =getIntent().getStringExtra(NoteEditActivity.EXTRA_NOTE_FILE_NAME);
        if (noteFilePath == null)
        {
            //Make new file
            mNoteFile = null;
        }
        else
        {
            //Attempt to load a file
            mNoteFile = new File(noteFilePath);
            readFile(mNoteFile);

        }
        mChanged = getIntent().getBooleanExtra(ProcessVoiceActivity.EXTRA_FROM_VOICE, false);
    }
    public static File getNewFile(File dir, String name, String suffix)
    {
        File result = new File(dir, name+suffix);
        for (int i = 1; result.exists(); i++)
        {
            result = new File(dir, String.format("%s (%d)%s", name, i, suffix));
        }
        return result;
    }
    public void readFile(File file)
    {
        if (file.exists())
        {
            try
            {
                DataInputStream in = new DataInputStream(new FileInputStream(file));
                mNoteTitle.setText(readString(in));
                mNoteBody.setText(readString(in));
                in.close();
            }
            catch (EOFException e)
            {
                e.printStackTrace();
                Log.w(TAG, "File invalid format.");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    public void cancelSave()
    {
        Toast canceled = Toast.makeText(this, R.string.toast_no_change_save_canceled, Toast.LENGTH_SHORT);
        canceled.show();
    }
    public void save(File file)
    {
        Toast saving = Toast.makeText(this, R.string.toast_saving, Toast.LENGTH_LONG);
        saving.show();
        String noteTitle = mNoteTitle.getText().toString();
        if (file == null)
        {
            if (noteTitle.length() == 0 && mNoteBody.getText().length() == 0)
            {
                cancelSave();
                return;
            }
            file= getNewFile(BrowseNotesActivity.getDefaultFolder(this), noteTitle, NOTE_FILE_SUFFIX);
        }
        try
        {
            saveFile(file, noteTitle, mNoteBody.getText().toString());
            saving.cancel();
            Toast saved = Toast.makeText(this, R.string.toast_saved, Toast.LENGTH_SHORT);
            saved.show();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            Toast failed = Toast.makeText(this, R.string.toast_save_failed, Toast.LENGTH_SHORT);
            failed.show();
        }
    }
    public static void saveFile(File file, String title, String body) throws IOException
    {
        DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
        writeString(out, title);
        writeString(out, body);
        out.flush();
        out.close();
    }
    public static void writeString(DataOutputStream out, String str) throws IOException
    {
        byte[] data;
        data = str.getBytes("UTF-8");
        out.writeInt(data.length);
        out.write(data);
    }
    public static String readString(DataInputStream in) throws IOException
    {
        int length;
        byte[] data;
        length = in.readInt();
        if (length > 1000000)
        {
            throw new IOException();
        }
        data = new byte[length];
        in.readFully(data);
        return new String(data, "UTF-8");
    }
    @Override
    public void onBackPressed()
    {
        if (mChanged) {
            save(mNoteFile);
        }
        else
        {
            cancelSave();
        }
        super.onBackPressed();
    }
}
