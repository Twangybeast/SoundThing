package twangybeast.myapplication.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import twangybeast.myapplication.R;
import twangybeast.myapplication.activities.NoteEditActivity;
import twangybeast.myapplication.activities.RecordSoundNoteActivity;
import twangybeast.myapplication.util.FileNoteManager;

/**
 * Created by cHeNdAn19 on 3/2/2018.
 */

public class RecordingFileAdapter extends BaseAdapter {
    public static final String TAG = "RecordingFileAdapter";
    Context mContext;
    public ArrayList<RecordingFile> mData;
    LayoutInflater mInflater;
    File[] mFiles;
    public boolean mIsSelecting;
    public RecordingFileAdapter(Context context, File[] files)
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
            //String noteTitle= NoteEditActivity.readString(in);
            long time = file.lastModified();
            String lastModified = dateFormat.format(new Date(time));
            int recordingLength = RecordSoundNoteActivity.getSecondsFromBytes(file.length());
            String duration = String.format("%02d:%02d", recordingLength/60, recordingLength%60);
            mData.set(i, new RecordingFile(file.getName(), lastModified, duration));
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
        for (RecordingFile recordingFile : mData)
        {
            recordingFile.isSelected = false;
        }
    }
    public void select(int position, boolean val)
    {
        mData.get(position).isSelected = val;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        RecordingFile recordingFile = mData.get(position);
        View rowView = mInflater.inflate(R.layout.item_recordings_list, parent, false);
        TextView textTitle =rowView.findViewById(R.id.textRecordingTitle);
        TextView textLastModified = rowView.findViewById(R.id.textRecordingLastModified);
        TextView textDuration= rowView.findViewById(R.id.textDuration);
        CheckBox checkBox = rowView.findViewById(R.id.checkboxRecordingFile);
        if (recordingFile != null)
        {
            textTitle.setText(recordingFile.noteTitle);
            textLastModified.setText(recordingFile.lastModified);
            textDuration.setText(recordingFile.duration);
            if (mIsSelecting)
            {
                checkBox.setChecked(recordingFile.isSelected);
            }
            else
            {
                checkBox.setVisibility(View.GONE);
            }
        }
        else
        {
            checkBox.setVisibility(View.GONE);
        }
        return rowView;
    }
    private class RecordingFile
    {
        public String noteTitle;
        public String lastModified;
        public String duration;
        public boolean isSelected;
        public RecordingFile(String noteTitle, String lastModified, String duration)
        {
            this.noteTitle = noteTitle;
            this.lastModified = lastModified;
            this.duration = duration;
            isSelected = false;
        }
        public void toggle()
        {
            isSelected = !isSelected;
        }
    }
}
