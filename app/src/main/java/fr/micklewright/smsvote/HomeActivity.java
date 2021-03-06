package fr.micklewright.smsvote;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ElectionDao;


public class HomeActivity extends AppCompatActivity implements ElectionNameDialog.ElectionNameDialogListener {

    ElectionDao electionDao;
    ArrayAdapter<Election> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ListView listView = (ListView) findViewById(R.id.listView_election);
        electionDao = ((DaoSessionApplication) getApplicationContext()).getDaoSession().getElectionDao();
        List<Election> elections = electionDao.queryBuilder().list();
        adapter = new ElectionAdapter(this, elections);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Election selected = (Election) adapterView.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), ElectionActivity.class);
                intent.putExtra("electionId", selected.getId());
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Election toRemove = (Election) adapterView.getItemAtPosition(i);
                adapter.remove(toRemove);
                toRemove.delete();
                return true;
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        List<Election> elections = electionDao.queryBuilder().list();
        adapter.clear();
        adapter.addAll(elections);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.home_action_new:
                DialogFragment dialog = new ElectionNameDialog();
                dialog.show(getSupportFragmentManager(), "ElectionNameDialogFragment");
                return true;
            case R.id.home_action_settings:
                ((DaoSessionApplication) getApplicationContext()).getDaoSession().getParticipationDao().queryBuilder().buildDelete().executeDeleteWithoutDetachingEntities();
                ((DaoSessionApplication) getApplicationContext()).getDaoSession().getContactDao().queryBuilder().buildDelete().executeDeleteWithoutDetachingEntities();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onElectionNameDialogPositiveClick(DialogFragment dialog, String name) {
        Election election = new Election();
        election.setName(name);
        election.setDate(new Date());
        election.setStage(Election.STAGE_INITIAL);
        adapter.add(election);
        electionDao.insert(election);
        Intent intent = new Intent(this, ElectionActivity.class);
        intent.putExtra("electionId", election.getId());
        startActivity(intent);
    }

    private class ElectionAdapter extends ArrayAdapter<Election> {
        List<Election> elections;
        Context context;

        public ElectionAdapter(Context context, List<Election> elections){
            super(context, android.R.layout.simple_list_item_2, elections);
            this.context = context;
            this.elections = elections;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            if(convertView == null){
                LayoutInflater mLayoutInflater = LayoutInflater.from(context);
                convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);
            }
            Election election = elections.get(position);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(election.getName());
            ((TextView) convertView.findViewById(android.R.id.text2))
                    .setText(new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.FRANCE).format(election.getDate()));

            return convertView;
        }
    }

}
