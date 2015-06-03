package fr.micklewright.smsvote;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ParticipationDao;


public class RegistrationFragment extends Fragment {


    private Election election;

    private ParticipationDao participationDao;

    private RegistrationFragmentListener mListener;

    public RegistrationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        participationDao = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getParticipationDao();
        election = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getElectionDao().load(getArguments().getLong("electionId"));

        election.setRegistrationCode((int) (Math.random()*9000)+1000);
        election.update();

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
        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        ((TextView) view.findViewById(R.id.textView_registrationCode))
                .setText(String.valueOf(election.getRegistrationCode()));
        return view;
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


    public void refreshView(){
        long count = participationDao.queryBuilder()
                .where(ParticipationDao.Properties.ElectionId.eq(election.getId()))
                .count();
        //noinspection ConstantConditions
        ((TextView) getView().findViewById(R.id.textView_registeredCount))
                .setText(String.valueOf(count));
    }

}
