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
    private static final String ITEM_KEY = "moveDialogItemKey";
    public interface MoveListener
    {
        public void onMoverDialogOnClick(DialogFragment dialog, int which);
    }
    private MoveListener mListener;
    private String[] strings;
    public boolean[] selected;
    public static MoveItemDialog newInstance(String[] stringArray) {
        MoveItemDialog dialog = new MoveItemDialog();
        Bundle args = new Bundle();
        args.putStringArray(ITEM_KEY, stringArray);
        dialog.setArguments(args);
        return dialog;
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        strings = getArguments().getStringArray(ITEM_KEY);
        selected = new boolean[strings.length];

    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_move_dialog);
        builder.setItems(strings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onMoverDialogOnClick(MoveItemDialog.this, which);
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
