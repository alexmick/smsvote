package fr.micklewright.smsvote;


import android.app.Activity;
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.micklewright.smsvote.database.Application;
import fr.micklewright.smsvote.database.ApplicationDao;
import fr.micklewright.smsvote.database.Contact;
import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.Participation;
import fr.micklewright.smsvote.database.ParticipationDao;
import fr.micklewright.smsvote.database.Post;
import fr.micklewright.smsvote.database.Vote;
import fr.micklewright.smsvote.database.VoteDao;


public class ElectionApplicationFragment extends Fragment {


    private Election election;
    private Post post;

    private ApplicationDao applicationDao;

    private ApplicationFragmentListener mListener;
    private ContactAdapter adapter;

    public ElectionApplicationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        applicationDao = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getApplicationDao();
        election = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getElectionDao().load(getArguments().getLong("electionId"));
        post = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession()
                .getPostDao().load(getArguments().getLong("postId"));

        // Start SMSMonitor Service

        Intent serviceIntent = new Intent(getActivity(), SMSMonitorService.class);
        serviceIntent.setAction(SMSMonitorService.ACTION_APPLY);
        serviceIntent.putExtra(SMSMonitorService.EXTRA_ELECTION_ID, election.getId());
        serviceIntent.putExtra(SMSMonitorService.EXTRA_POST_ID, post.getId());
        getActivity().startService(serviceIntent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_application, container, false);
        ((TextView) view.findViewById(R.id.textView_registeredCount))
                .setText(getString(R.string.activity_election_application_list_title)
                        .replace('#', '0'));


        adapter = new ContactAdapter(getActivity(), new ArrayList<Contact>());
        ((ListView) view.findViewById(R.id.listView_registered_contacts)).setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar()
                .setTitle(getString(R.string.activity_election_application_title) + " " + post.getName());
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_election_application, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.election_application_action_cancel:
                Intent intent = new Intent(getActivity(), SMSMonitorService.class);
                getActivity().stopService(intent);
                mListener.onApplicationCancel(post.getId());
                return true;
            case R.id.election_application_action_confirm:
                mListener.onApplicationAccept();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (ApplicationFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ApplicationFragmentListener");
        }
    }

    public interface ApplicationFragmentListener{
        public void onApplicationCancel(long postId);
        public void onApplicationAccept();
    }


    public void refreshView(){
        List<Application> applications = applicationDao.queryBuilder()
                .where(ApplicationDao.Properties.PostId.eq(post.getId()))
                .list();
        int i=0;
        adapter.clear();
        for (Application application : applications){
            i++;
            adapter.add(application.getContact());
        }
        adapter.notifyDataSetChanged();
        //noinspection ConstantConditions
        ((TextView) getView().findViewById(R.id.textView_registeredCount))
                .setText(getString(R.string.activity_election_application_list_title)
                        .replace("#", String.valueOf(i)));
    }

    private class ContactAdapter extends ArrayAdapter<Contact> {
        List<Contact> contacts;
        Context context;

        public ContactAdapter(Context context, List<Contact> contacts){
            super(context, android.R.layout.simple_list_item_2, contacts);
            this.context = context;
            this.contacts = contacts;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent){
            if(convertView == null){
                LayoutInflater mLayoutInflater = LayoutInflater.from(context);
                convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);
            }
            final Contact contact = contacts.get(position);
            ((TextView) convertView.findViewById(android.R.id.text1))
                    .setText(contact.getName());
            ((TextView) convertView.findViewById(android.R.id.text2))
                    .setText(String.valueOf(contact.getNumber()));

            return convertView;
        }
    }
}
