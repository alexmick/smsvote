package fr.micklewright.smsvote;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ElectionDao;


public class ElectionActivity extends AppCompatActivity  implements PostDialogFragment.PostDialogListener, ElectionNameDialog.ElectionNameDialogListener {

    final String SUMMARY_FRAGMENT_TAG = "summaryFragment";

    ElectionDao electionDao;
    Election election;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election);

        electionDao = ((DaoApplication) getApplicationContext()).getDaoSession().getElectionDao();
        election = electionDao.load(getIntent().getLongExtra("electionId", 0));

        if (findViewById(R.id.fragment_container_election) != null && savedInstanceState == null ) {
            ElectionSummaryFragment fragment = new ElectionSummaryFragment();
            fragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container_election, fragment, SUMMARY_FRAGMENT_TAG).commit();
        }
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

        transaction.replace(R.id.fragment_container_election, fragment);
        transaction.commit();
    }
}
