package fr.micklewright.smsvote;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.micklewright.smsvote.database.Application;
import fr.micklewright.smsvote.database.ApplicationDao;
import fr.micklewright.smsvote.database.Contact;
import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.Post;
import fr.micklewright.smsvote.database.VoteDao;


public class ElectionVoteFragment extends Fragment {


    private Election election;
    private Post post;

    private ApplicationDao applicationDao;
    private VoteDao voteDao;

    private VoteFragmentListener mListener;
    private ContactAdapter adapter;

    public ElectionVoteFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        applicationDao = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getApplicationDao();
        voteDao = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getVoteDao();
        election = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getElectionDao().load(getArguments().getLong("electionId"));
        post = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getPostDao().load(getArguments().getLong("postId"));


        // Start SMSMonitor Service

        Intent serviceIntent = new Intent(getActivity(), SMSMonitorService.class);
        serviceIntent.setAction(SMSMonitorService.ACTION_VOTE);
        serviceIntent.putExtra(SMSMonitorService.EXTRA_ELECTION_ID, election.getId());
        serviceIntent.putExtra(SMSMonitorService.EXTRA_POST_ID, post.getId());
        getActivity().startService(serviceIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vote, container, false);

        post.resetApplications();
        ArrayList<Application> applications = new ArrayList<>();
        applications.addAll(post.getApplications());
        adapter = new ContactAdapter(getActivity(), applications);
        ((ListView) view.findViewById(R.id.listView_registered_contacts)).setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getString(R.string.activity_election_vote_title) + " " + post.getName());
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_election_vote, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.election_vote_action_cancel:
                Intent intent = new Intent(getActivity(), SMSMonitorService.class);
                getActivity().stopService(intent);
                mListener.onVoteCancel(post.getId());
                return true;
            case R.id.election_vote_action_confirm:
                Intent intent2 = new Intent(getActivity(), SMSMonitorService.class);
                getActivity().stopService(intent2);
                mListener.onVoteAccept();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (VoteFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement VoteFragmentListener");
        }
    }

    public interface VoteFragmentListener{
        public void onVoteCancel(long postId);
        public void onVoteAccept();
    }


    public void refreshView(){
        post.resetApplications();
        adapter.clear();
        adapter.addAll(post.getApplications());
        adapter.notifyDataSetChanged();

        for (Application application : post.getApplications()){
            application.resetVotes();
            Log.w(application.getContact().getName(), String.valueOf(application.getVotes().size()));
        }
    }

    private class ContactAdapter extends ArrayAdapter<Application> {
        List<Application> applications;
        Context context;

        public ContactAdapter(Context context, List<Application> applications){
            super(context, android.R.layout.simple_list_item_2, applications);
            this.context = context;
            this.applications = applications;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            if(convertView == null){
                LayoutInflater mLayoutInflater = LayoutInflater.from(context);
                convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);
            }
            final Application application = applications.get(position);
            application.resetVotes();
            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(application.getCandidateNumber() +": "+application.getContact().getName());

            ((TextView) convertView.findViewById(android.R.id.text2))
            .setText(String.valueOf(application.getVotes().size()));

            return convertView;
        }
    }
}
