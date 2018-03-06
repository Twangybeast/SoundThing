package twangybeast.myapplication.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import twangybeast.myapplication.R;
import twangybeast.myapplication.activities.NoteEditActivity;
import twangybeast.myapplication.models.FileItem;
import twangybeast.myapplication.models.FolderFile;
import twangybeast.myapplication.models.NoteFile;
import twangybeast.myapplication.util.FileNoteManager;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by Twangybeast on 2/21/2018.
 */

public class NoteFileAdapter extends BaseAdapter
{
    public static final String TAG = "NoteFileAdapter";
    Context mContext;
    public ArrayList<FileItem> mData;
    LayoutInflater mInflater;
    File[] mFiles;
    public boolean mIsSelecting;
    public NoteFileAdapter(Context context, File[] files)
    {
        this.mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        updateFiles(files);
    }
    @Override
    public int getCount()
    {
        return mData.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position ;
    }
    public String getFilePath(int position)
    {
        return mFiles[position].getAbsolutePath();
    }
    public void updateFiles(File[] files)
    {
        mData = new ArrayList<>(files.length);
        for (int i = 0; i < files.length; i++)
        {
            mData.add(null);
        }
        mFiles = files;
        mIsSelecting = false;
        refreshItemInfos();
    }
    public void refreshItemInfos()
    {
        Arrays.sort(mFiles, new Comparator<File>()
        {
            @Override
            public int compare(File o1, File o2)
            {
                return (int) (o2.lastModified() - o1.lastModified());
            }
        });
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

        for (int i = 0; i < mFiles.length; i++)
        {
            File file = mFiles[i];
            Log.d(TAG, file.getAbsolutePath());
            try
            {
                if (file.isDirectory())
                {
                    //Is a folder
                    String title = file.getName();
                    mData.set(i, new FolderFile(title));
                }
                else {
                    //Is note file
                    DataInputStream in = new DataInputStream(new FileInputStream(file));
                    String fileTitle = NoteEditActivity.readString(in);
                    in.close();
                    long time = file.lastModified();
                    String lastModified = dateFormat.format(new Date(time));
                    mData.set(i, new NoteFile(fileTitle, lastModified));
                }
            }
            catch (IOException e)
            {
                Log.e(TAG, "Error loading files to view.");
                FileNoteManager.printAllFileContents(file, TAG);
                e.printStackTrace();
            }
        }
    }
    public void deleteSelected()
    {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).isSelected)
            {
                mFiles[i].delete();
            }
        }
    }
    public void clearSelected()
    {
        for (FileItem fileItem : mData)
        {
            fileItem.isSelected = false;
        }
    }
    public void select(int position, boolean val)
    {
        mData.get(position).isSelected = val;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        FileItem fileItem= mData.get(position);
        if (fileItem instanceof NoteFile) {
            NoteFile noteFile = (NoteFile) fileItem;
            View rowView = mInflater.inflate(R.layout.item_notes_list, parent, false);
            TextView textTitle = rowView.findViewById(R.id.textNoteTitle);
            TextView textLastModified = rowView.findViewById(R.id.textLastModified);
            CheckBox checkBox = rowView.findViewById(R.id.checkboxNoteFile);
            if (noteFile != null) {
                textTitle.setText(noteFile.fileTitle);
                textLastModified.setText(noteFile.lastModified);
                if (mIsSelecting) {
                    checkBox.setChecked(noteFile.isSelected);
                } else {
                    checkBox.setVisibility(View.GONE);
                }
            } else {
                checkBox.setVisibility(View.GONE);
            }
            return rowView;
        }
        else
        {
            View rowView = mInflater.inflate(R.layout.item_folder_list, parent, false);
            TextView textTitle = rowView.findViewById(R.id.textFolderTitle);
            CheckBox checkBox = rowView.findViewById(R.id.checkboxFolder);
            if (fileItem != null)
            {
                FolderFile folder = (FolderFile) fileItem;
                textTitle.setText(folder.fileTitle);
                if (mIsSelecting) {
                    checkBox.setChecked(folder.isSelected);
                } else {
                    checkBox.setVisibility(View.GONE);
                }
            }
            else
            {
                checkBox.setVisibility(View.GONE);
            }
            return rowView;
        }
    }
}
