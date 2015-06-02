package fr.micklewright.smsvote;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.NumberPicker;

import fr.micklewright.smsvote.database.Election;


/**
 * A simple {@link Fragment} subclass.
 */
public class ElectionNameDialog extends DialogFragment {

    private ElectionNameDialogListener mListener;

    public ElectionNameDialog() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.fragment_election_name_dialog, null);

        if (getArguments() != null && getArguments().getString("name") != null){
            ((EditText) view.findViewById(R.id.editText_dialog_election_name))
                    .setText(getArguments().getString("name"));
        }

        builder.setView(view)
                .setTitle(R.string.dialog_election_name_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String name = ((EditText) view.findViewById(R.id.editText_dialog_election_name))
                                .getText().toString();
                        mListener.onElectionNameDialogPositiveClick(ElectionNameDialog.this, name);
                    }
                })
                .setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ElectionNameDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ElectionNameDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface ElectionNameDialogListener {
        public void onElectionNameDialogPositiveClick(DialogFragment dialog, String name);
    }
}
