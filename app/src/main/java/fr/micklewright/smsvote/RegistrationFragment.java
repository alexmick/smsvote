package fr.micklewright.smsvote;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import fr.micklewright.smsvote.database.Election;


public class RegistrationFragment extends Fragment {


    private Election election;

    private RegistrationFragmentListener mListener;

    public RegistrationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        election = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getElectionDao().load(getArguments().getLong("electionId"));

        // Start SMSMonitor Service

        Intent serviceIntent = new Intent(getActivity(), SMSMonitorService.class);
        serviceIntent.setAction(SMSMonitorService.ACTION_REGISTER);
        serviceIntent.putExtra(SMSMonitorService.EXTRA_ELECTION_ID, election.getId());
        getActivity().startService(serviceIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getString(R.string.activity_election_registration_title));
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_election_registration, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()){
            case R.id.election_registration_action_cancel:
                Intent intent = new Intent(getActivity(), SMSMonitorService.class);
                getActivity().stopService(intent);
                mListener.onRegistrationCancel();
                return true;
            case R.id.election_registration_action_confirm:
                mListener.onRegistrationAccept();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (RegistrationFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RegistrationFragmentListener");
        }
    }

    public interface RegistrationFragmentListener{
        public void onRegistrationCancel();
        public void onRegistrationAccept();
    }



}
