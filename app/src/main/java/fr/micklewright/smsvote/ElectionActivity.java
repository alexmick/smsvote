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

import fr.micklewright.smsvote.database.Application;
import fr.micklewright.smsvote.database.ApplicationDao;
import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ElectionDao;
import fr.micklewright.smsvote.database.ParticipationDao;
import fr.micklewright.smsvote.database.Post;
import fr.micklewright.smsvote.database.PostDao;
import fr.micklewright.smsvote.database.VoteDao;


public class ElectionActivity extends AppCompatActivity  implements PostDialogFragment.PostDialogListener,
        ElectionNameDialog.ElectionNameDialogListener, ElectionRegistrationFragment.RegistrationFragmentListener,
        ElectionVoteOverviewFragment.VoteOverviewFragmentListener, ElectionApplicationFragment.ApplicationFragmentListener,
        ElectionVoteFragment.VoteFragmentListener {

    private static final String REGISTRATION_FRAGMENT_TAG = "registrationFragment";
    private static final String SUMMARY_FRAGMENT_TAG = "summaryFragment";
    private static final String OVERVIEW_FRAGMENT_TAG = "voteOverviewFragment";
    private static final String APPLICATION_FRAGMENT_TAG = "applicationFragment";
    private static final String VOTE_FRAGMENT_TAG = "voteFragment";

    ElectionDao electionDao;
    ParticipationDao participationDao;
    ApplicationDao applicationDao;
    PostDao postDao;
    Election election;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election);

        electionDao = ((DaoApplication) getApplicationContext()).getDaoSession().getElectionDao();
        participationDao = ((DaoApplication) getApplicationContext()).getDaoSession().getParticipationDao();
        applicationDao = ((DaoApplication) getApplicationContext()).getDaoSession().getApplicationDao();
        postDao = ((DaoApplication) getApplicationContext()).getDaoSession().getPostDao();
        election = electionDao.load(getIntent().getLongExtra("electionId", 0));



        if (findViewById(R.id.fragment_container_election) != null && savedInstanceState == null ) {
            switch (election.getStage()) {
                case Election.STAGE_INITIAL:
                    ElectionSummaryFragment electionSummaryFragment = new ElectionSummaryFragment();
                    electionSummaryFragment.setArguments(getIntent().getExtras());

                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container_election, electionSummaryFragment, SUMMARY_FRAGMENT_TAG).commit();
                    break;
                case Election.STAGE_REGISTRATION:
                    ElectionRegistrationFragment electionRegistrationFragment = new ElectionRegistrationFragment();
                    Bundle args = new Bundle();
                    args.putLong("electionId", election.getId());
                    electionRegistrationFragment.setArguments(args);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container_election, electionRegistrationFragment, REGISTRATION_FRAGMENT_TAG).commit();
                    break;
                case Election.STAGE_VOTE:
                    ElectionVoteOverviewFragment electionVoteOverviewFragment = new ElectionVoteOverviewFragment();
                    Bundle args2 = new Bundle();
                    args2.putLong("electionId", election.getId());
                    electionVoteOverviewFragment.setArguments(args2);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.fragment_container_election, electionVoteOverviewFragment, OVERVIEW_FRAGMENT_TAG).commit();
                    break;
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new SMSMonitorReceiver(),
                new IntentFilter(SMSMonitorService.ACTION_REGISTER
                ));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new SMSMonitorReceiver(),
                new IntentFilter(SMSMonitorService.ACTION_APPLY
                ));

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new SMSMonitorReceiver(),
                new IntentFilter(SMSMonitorService.ACTION_VOTE
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

        ElectionRegistrationFragment fragment = new ElectionRegistrationFragment();
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

        ElectionVoteOverviewFragment fragment = new ElectionVoteOverviewFragment();
        Bundle args = new Bundle();
        args.putLong("electionId", election.getId());
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container_election, fragment, OVERVIEW_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void onVoteStart(long postId) {
//        Post post = postDao.load(postId);
        ElectionApplicationFragment fragment = new ElectionApplicationFragment();
        Bundle args = new Bundle();
        args.putLong("electionId", election.getId());
        args.putLong("postId", postId);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container_election, fragment, APPLICATION_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void onFinishElection() {

    }

    @Override
    public void onApplicationCancel(long postId) {
        applicationDao.queryBuilder()
                .where(ApplicationDao.Properties.PostId.eq(postId))
                .buildDelete().executeDeleteWithoutDetachingEntities();

        ElectionVoteOverviewFragment fragment = new ElectionVoteOverviewFragment();
        Bundle args = new Bundle();
        args.putLong("electionId", election.getId());
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container_election, fragment, OVERVIEW_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void onApplicationAccept(long postId) {
        ElectionVoteFragment fragment = new ElectionVoteFragment();
        Bundle args = new Bundle();
        args.putLong("electionId", election.getId());
        args.putLong("postId", postId);
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container_election, fragment, VOTE_FRAGMENT_TAG);
        transaction.commit();
    }

    @Override
    public void onVoteCancel(long postId) {
        VoteDao voteDao = ((DaoApplication) getApplicationContext()).getDaoSession().getVoteDao();
        Post post = postDao.load(postId);
        for (Application application : post.getApplications()){
            voteDao.queryBuilder().where(VoteDao.Properties.ApplicationId.eq(application.getId()))
                    .buildDelete().executeDeleteWithoutDetachingEntities();
        }
        onApplicationCancel(postId);
    }

    @Override
    public void onVoteAccept() {

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
                ((ElectionRegistrationFragment) getSupportFragmentManager().findFragmentByTag(REGISTRATION_FRAGMENT_TAG))
                .refreshView();
            } else if (intent.getAction().equals(SMSMonitorService.ACTION_APPLY)){
                ((ElectionApplicationFragment) getSupportFragmentManager().findFragmentByTag(APPLICATION_FRAGMENT_TAG))
                        .refreshView();
            } else if (intent.getAction().equals(SMSMonitorService.ACTION_VOTE)){
                ((ElectionVoteFragment) getSupportFragmentManager().findFragmentByTag(VOTE_FRAGMENT_TAG))
                        .refreshView();
            }
        }
    }

}
