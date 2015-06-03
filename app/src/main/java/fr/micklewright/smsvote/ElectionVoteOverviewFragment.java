package fr.micklewright.smsvote;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ElectionDao;
import fr.micklewright.smsvote.database.Post;
import fr.micklewright.smsvote.database.PostDao;


public class ElectionVoteOverviewFragment extends Fragment{

    ElectionDao electionDao;
    PostDao postDao;

    List<Post> posts;
    Election election;

    VoteOverviewFragmentListener listener;
    PostAdapter adapter;

    public ElectionVoteOverviewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        electionDao = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession().getElectionDao();
        postDao = ((DaoApplication) getActivity().getApplicationContext()).getDaoSession().getPostDao();

        election = electionDao.load(getArguments().getLong("electionId"));

        election.resetPosts();
        posts = new ArrayList<>();
        posts.addAll(election.getPosts());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_election_vote_overview, container, false);

        ListView listViewPosts = (ListView) view.findViewById(R.id.listView_posts);
        adapter = new PostAdapter(getActivity(), posts);
        listViewPosts.setAdapter(adapter);

        listViewPosts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Post post = (Post) adapterView.getItemAtPosition(i);
                listener.onVoteStart(post.getId());
            }
        });

        listViewPosts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                return true;
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //noinspection ConstantConditions
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.activity_election_voteOverview_title);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_election_vote_overview, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.election_action_finish) {
            listener.onFinishElection();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (VoteOverviewFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement VoteOverviewFragmentListener");
        }
    }

    public interface VoteOverviewFragmentListener{
        public void onVoteStart(long postId);
        public void onFinishElection();
    }

    private class PostAdapter extends ArrayAdapter<Post> {
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
}



