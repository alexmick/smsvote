package fr.micklewright.smsvote;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;


public class PostDialogFragment extends DialogFragment {

    private PostDialogListener mListener;

    public PostDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.fragment_post, null);

        NumberPicker np = (NumberPicker) view.findViewById(R.id.dialog_post_places);
        np.setMaxValue(9);
        np.setMinValue(1);

        builder.setView(view)
                .setTitle(R.string.dialog_post_name)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = ((EditText) view.findViewById(R.id.dialog_post_name)).getText().toString();
                        int places = ((NumberPicker) view.findViewById(R.id.dialog_post_places)).getValue();
                        mListener.onPostDialogPositiveClick(PostDialogFragment.this, name, places);
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (PostDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PostDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface PostDialogListener {
        public void onPostDialogPositiveClick(DialogFragment dialog, String name, int places);
    }

}
