package twangybeast.myapplication;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity{
    public static final int REQUEST_SOUND_FILE = 0b110110010110101;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    public void onStartSoundNoteClick(View view)
    {
        Intent intent = new Intent(this, RecordSoundNoteActivity.class);
        startActivityForResult(intent, REQUEST_SOUND_FILE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_SOUND_FILE:
                if (resultCode == RESULT_OK)
                {
                    data.setClass(this, NoteEditActivity.class);
                    startActivity(data);
                }
                break;
        }
    }
    public void onStartTextNoteClick(View view)
    {

    }
}
