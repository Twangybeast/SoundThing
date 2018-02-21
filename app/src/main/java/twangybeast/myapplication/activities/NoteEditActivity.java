package twangybeast.myapplication.activities;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
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
    EditText mNoteTitle;
    EditText mNoteBody;
    File mNoteFile;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        mNoteTitle = findViewById(R.id.editNoteTitle);
        mNoteBody = findViewById(R.id.editNoteBody);
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
    public void save(File file)
    {
        Toast saving = Toast.makeText(this, R.string.toast_saving, Toast.LENGTH_LONG);
        saving.show();
        String noteTitle = mNoteTitle.getText().toString();
        if (file == null)
        {
            file= getNewFile(getFilesDir(), noteTitle, NOTE_FILE_SUFFIX);
        }
        try
        {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(file));
            byte[] data;
            data = noteTitle.getBytes("UTF-8");
            out.writeInt(data.length);
            out.write(data);
            data = mNoteBody.getText().toString().getBytes("UTF-8");
            out.writeInt(data.length);
            out.write(data);
            out.flush();
            out.close();
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
    public static String readString(DataInputStream in) throws IOException
    {
        int length;
        byte[] data;
        length = in.readInt();
        data = new byte[length];
        in.readFully(data);
        return new String(data, "UTF-8");
    }
    @Override
    public void onBackPressed()
    {
        save(mNoteFile);
        super.onBackPressed();
    }
}
