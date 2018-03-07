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

public class MoveItemDialog extends DialogFragment {
    public static final String TAG = "MoveItemDialog";
    public interface MoveListener
    {
        public void onMoverDialogPositiveClick(DialogFragment dialog);
        public void onMoveDialogNegativeClick(DialogFragment dialog);
    }
    private MoveListener mListener;
    private String[] stringArray;
    public boolean[] selected;
    public MoveItemDialog(String[] stringArray) {
        //TODO FIX
        super();
        this.stringArray = stringArray;
        selected = new boolean[stringArray.length];
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle(R.string.title_new_folder_dialog);
        builder.setMultiChoiceItems(stringArray, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                selected[which] = isChecked;
            }
        })
                .setPositiveButton(R.string.button_create_folder, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onMoverDialogPositiveClick(MoveItemDialog.this);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onMoverDialogPositiveClick(MoveItemDialog.this);
                        dialog.dismiss();
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
            mListener = (MoveListener) activity;
        } catch (ClassCastException e)
        {
            Log.e(TAG, "Could not cast activity to listener");
            e.printStackTrace();
        }
    }
}
