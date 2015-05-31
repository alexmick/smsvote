package fr.micklewright.smsvote;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ElectionDao;


public class ElectionActivity extends AppCompatActivity {

    Election election;
    ElectionDao electionDao;

    EditText editTextName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election);
        editTextName = (EditText) findViewById(R.id.editText_name);

        electionDao = ((DaoApplication) getApplicationContext()).getDaoSession().getElectionDao();

        Intent intent = getIntent();
        if( intent.hasExtra("electionId")) {
            election = electionDao.load(intent.getLongExtra("electionId", 0));
            editTextName.setText(election.getName());
        } else {
            election = new Election();
            election.setDate(new Date());
        }

        TextView dateTextView = (TextView) findViewById(R.id.textView_date);
        dateTextView.setText(new SimpleDateFormat("hh:mm dd/MM/yyyy ", Locale.FRANCE).format(election.getDate()));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_election, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {
            if (!editTextName.getText().toString().isEmpty()) {
                election.setName(editTextName.getText().toString());
                electionDao.insertOrReplace(election);
            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
