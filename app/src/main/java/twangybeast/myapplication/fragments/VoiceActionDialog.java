package twangybeast.myapplication.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;

import twangybeast.myapplication.R;

/**
 * Created by cHeNdAn19 on 3/6/2018.
 */

public class VoiceActionDialog extends DialogFragment {
    public static final String TAG = "VoiceActionDialog";
    public interface VoiceActionListener
    {
        public void onVoiceActionSelectionClick(DialogFragment dialog, int which);
    }
    public int position;
    private VoiceActionListener mListener;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_voice_action_dialog);
        builder.setItems(new String[]{getString(R.string.text_play_file), getString(R.string.text_process_file)}, new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onVoiceActionSelectionClick(VoiceActionDialog.this, which);
            }
        });

        return builder.create();
    }
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            mListener = (VoiceActionListener) activity;
        } catch (ClassCastException e)
        {
            Log.e(TAG, "Could not cast activity to listener");
            e.printStackTrace();
        }
    }
}
