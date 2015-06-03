package fr.micklewright.smsvote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ElectionDao;
import fr.micklewright.smsvote.database.Participation;
import fr.micklewright.smsvote.database.ParticipationDao;


public class ElectionActivity extends AppCompatActivity  implements PostDialogFragment.PostDialogListener,
        ElectionNameDialog.ElectionNameDialogListener, RegistrationFragment.RegistrationFragmentListener {

    private static final String REGISTRATION_FRAGMENT_TAG = "registrationFragment";
    private static final String SUMMARY_FRAGMENT_TAG = "summaryFragment";

    ElectionDao electionDao;
    ParticipationDao participationDao;
    Election election;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election);

        electionDao = ((DaoApplication) getApplicationContext()).getDaoSession().getElectionDao();
        participationDao = ((DaoApplication) getApplicationContext()).getDaoSession().getParticipationDao();
        election = electionDao.load(getIntent().getLongExtra("electionId", 0));



        if (findViewById(R.id.fragment_container_election) != null && savedInstanceState == null ) {
            switch (election.getStage()) {
                case Election.STAGE_INITIAL:
                case Election.STAGE_VOTE:
                    ElectionSummaryFragment electionSummaryFragment = new ElectionSummaryFragment();
                    electionSummaryFragment.setArguments(getIntent().getExtras());

                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container_election, electionSummaryFragment, SUMMARY_FRAGMENT_TAG).commit();
                    break;
                case Election.STAGE_REGISTRATION:
                    RegistrationFragment registrationFragment = new RegistrationFragment();
                    Bundle args = new Bundle();
                    args.putLong("electionId", election.getId());
                    registrationFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container_election, registrationFragment, REGISTRATION_FRAGMENT_TAG).commit();
                    break;
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new SMSMonitorReceiver(),
                new IntentFilter(SMSMonitorService.ACTION_REGISTER
                ));
    }

    @Override
    public void onPostDialogPositiveClick(DialogFragment dialog, String name, int places) {
        ((ElectionSummaryFragment) getSupportFragmentManager().findFragmentByTag(SUMMARY_FRAGMENT_TAG))
                .createPost(name, places);
    }

    @Override
    public void onElectionNameDialogPositiveClick(DialogFragment dialog, String name) {
        election.setName(name);
        election.update();

        ElectionSummaryFragment fragment = new ElectionSummaryFragment();
        Bundle args = new Bundle();
        args.putLong("electionId", election.getId());
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container_election, fragment, SUMMARY_FRAGMENT_TAG);
        transaction.commit();
    }

    public void clickStartButton(View view) {
        election.setStage(Election.STAGE_REGISTRATION);
        election.update();

        RegistrationFragment fragment = new RegistrationFragment();
        Bundle args = new Bundle();
        args.putLong("electionId", election.getId());
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container_election, fragment, REGISTRATION_FRAGMENT_TAG);
        //transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRegistrationCancel() {
        participationDao.queryBuilder()
                .where(ParticipationDao.Properties.ElectionId.eq(election.getId()))
                .buildDelete().executeDeleteWithoutDetachingEntities();
        election.setStage(Election.STAGE_INITIAL);
        election.update();

        ElectionSummaryFragment fragment = new ElectionSummaryFragment();
        Bundle args = new Bundle();
        args.putLong("electionId", election.getId());
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container_election, fragment, SUMMARY_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void onRegistrationAccept() {
        election.setStage(Election.STAGE_VOTE);
        election.update();
    }


    // Broadcast receiver for receiving status updates from the IntentService
    private class SMSMonitorReceiver extends BroadcastReceiver
    {
        // Prevents instantiation
        private SMSMonitorReceiver() {
        }
        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(SMSMonitorService.ACTION_REGISTER)){
                ((RegistrationFragment) getSupportFragmentManager().findFragmentByTag(REGISTRATION_FRAGMENT_TAG))
                .refreshView();
            }
        }
    }

}
