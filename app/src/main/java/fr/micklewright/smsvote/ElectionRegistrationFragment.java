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

import fr.micklewright.smsvote.database.Contact;
import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.Participation;
import fr.micklewright.smsvote.database.ParticipationDao;


public class ElectionRegistrationFragment extends Fragment {


    private Election election;

    private ParticipationDao participationDao;

    private RegistrationFragmentListener mListener;
    private ContactAdapter adapter;

    public ElectionRegistrationFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        participationDao = ((DaoSessionApplication) getActivity().getApplicationContext()).getDaoSession()
                .getParticipationDao();
        election = ((DaoSessionApplication) getActivity().getApplicationContext()).getDaoSession()
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
                .setText(getString(R.string.activity_election_registration_code)
                        .replace("####", String.valueOf(election.getRegistrationCode())));
        ((TextView) view.findViewById(R.id.textView_registeredCount))
                .setText(getString(R.string.activity_election_registration_list_title)
                        .replace('#', '0'));


        adapter = new ContactAdapter(getActivity(), new ArrayList<Contact>());
        ((ListView) view.findViewById(R.id.listView_registered_contacts)).setAdapter(adapter);
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
                Intent intent2 = new Intent(getActivity(), SMSMonitorService.class);
                getActivity().stopService(intent2);
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
        List<Participation> participations = participationDao.queryBuilder()
                .where(ParticipationDao.Properties.ElectionId.eq(election.getId()))
                .list();
        int i=0;
        adapter.clear();
        for (Participation participation : participations){
            i++;
            adapter.add(participation.getContact());
        }
        adapter.notifyDataSetChanged();
        //noinspection ConstantConditions
        ((TextView) getView().findViewById(R.id.textView_registeredCount))
                .setText(getString(R.string.activity_election_registration_list_title)
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
