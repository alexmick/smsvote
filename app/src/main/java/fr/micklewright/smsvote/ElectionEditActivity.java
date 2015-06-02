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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.dao.DaoException;
import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ElectionDao;
import fr.micklewright.smsvote.database.Post;
import fr.micklewright.smsvote.database.PostDao;


public class ElectionEditActivity extends AppCompatActivity implements PostFragment.PostDialogListener{

    ElectionDao electionDao;
    PostDao postDao;

    List<Post> posts;
    Election election;

    PostAdapter adapter;
    EditText editTextName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_election_edit);
        editTextName = (EditText) findViewById(R.id.editText_name);

        electionDao = ((DaoApplication) getApplicationContext()).getDaoSession().getElectionDao();
        postDao = ((DaoApplication) getApplicationContext()).getDaoSession().getPostDao();

        Intent intent = getIntent();
        posts = new ArrayList<>();
        if( intent.hasExtra("electionId")) {
            election = electionDao.load(intent.getLongExtra("electionId", 0));
            election.resetPosts();
            editTextName.setText(election.getName());
            posts.addAll(election.getPosts());
        } else {
            election = new Election();
            election.setDate(new Date());
        }

        TextView dateTextView = (TextView) findViewById(R.id.textView_date);
        dateTextView.setText(new SimpleDateFormat("HH:mm d MMM yyyy", Locale.FRANCE).format(election.getDate()));

        ListView listViewPosts = (ListView) findViewById(R.id.listView_posts);
        adapter = new PostAdapter(this, posts);
        listViewPosts.setAdapter(adapter);

        listViewPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(ElectionEditActivity.this, getString(R.string.activity_election_click), Toast.LENGTH_SHORT).show();
            }
        });

        listViewPosts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Post post = (Post) adapterView.getItemAtPosition(i);
                posts.remove(post);
                try {
                    ((DaoApplication) getApplicationContext()).getDaoSession().getPostDao()
                            .delete(post);
                } catch (DaoException ignored) {

                }
                adapter.notifyDataSetChanged();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_election_edit, menu);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void addPost(View view) {
        DialogFragment dialog = new PostFragment();
        dialog.show(getSupportFragmentManager(), "PostDialogFragment");
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
                for (Post post : posts){
                    post.setElection(election);
                }
                postDao.insertOrReplaceInTx(posts);
            }
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String name, int places) {
        Post post = new Post();
        post.setName(name);
        post.setPlaces(places);
        posts.add(post);
        adapter.notifyDataSetChanged();

    }
}

class PostAdapter extends ArrayAdapter<Post> {
    List<Post> posts;
    Context context;

    public PostAdapter(Context context, List<Post> posts){
        super(context, android.R.layout.simple_list_item_2, posts);
        this.context = context;
        this.posts = posts;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent){
        if(convertView == null){
            LayoutInflater mLayoutInflater = LayoutInflater.from(context);
            convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_2, null);
        }
        final Post post = posts.get(position);
        ((TextView) convertView.findViewById(android.R.id.text1))
                .setText(post.getName());
        ((TextView) convertView.findViewById(android.R.id.text2))
                .setText(String.valueOf(post.getPlaces()));

        return convertView;
    }
}
