package fr.micklewright.smsvote;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.List;

import fr.micklewright.smsvote.database.Application;
import fr.micklewright.smsvote.database.ApplicationDao;
import fr.micklewright.smsvote.database.Contact;
import fr.micklewright.smsvote.database.ContactDao;
import fr.micklewright.smsvote.database.DaoSession;
import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ElectionDao;
import fr.micklewright.smsvote.database.Participation;
import fr.micklewright.smsvote.database.ParticipationDao;
import fr.micklewright.smsvote.database.Post;
import fr.micklewright.smsvote.database.PostDao;
import fr.micklewright.smsvote.database.Vote;
import fr.micklewright.smsvote.database.VoteDao;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SMSMonitorService extends Service {

    public static final String ACTION_REGISTER = "fr.micklewright.smsvote.action.register";
    public static final String ACTION_APPLY = "fr.micklewright.smsvote.action.APPLY";
    public static final String ACTION_VOTE = "fr.micklewright.smsvote.action.VOTE";

    public static final String EXTRA_ELECTION_ID = "fr.micklewright.smsvote.extra.electionId";
    public static final String EXTRA_POST_ID = "fr.micklewright.smsvote.extra.postId";

    private String action;

    private Election election = null;
    private Post post = null;

    private ElectionDao electionDao;
    private ParticipationDao participationDao;
    private ApplicationDao applicationDao;
    private ContactDao contactDao;
    private PostDao postDao;
    private VoteDao voteDao;

    private SMSReceiver smsReceiver;

    public SMSMonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        smsReceiver = new SMSReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        intentFilter.setPriority(Integer.MAX_VALUE);
        registerReceiver(smsReceiver, intentFilter);

        action = intent.getAction();

        final long electionId = intent.getLongExtra(EXTRA_ELECTION_ID, -1);
        final long postId = intent.getLongExtra(EXTRA_POST_ID, -1);

        connectDatabase();

        election = electionDao.load(electionId);
        startForeground(election.getId().intValue(), getNotification());

        if( postId != -1 ){
            post = postDao.load(postId);
        }

        return START_REDELIVER_INTENT;
    }

    private void connectDatabase(){
        DaoSession appSession = ((DaoSessionApplication) getApplicationContext()).getDaoSession();
        electionDao = appSession.getElectionDao();
        participationDao = appSession.getParticipationDao();
        contactDao = appSession.getContactDao();
        applicationDao = appSession.getApplicationDao();
        postDao = appSession.getPostDao();
        voteDao = appSession.getVoteDao();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(smsReceiver);
        super.onDestroy();
    }

    public void onSMSReceive(String from, String body){
        if (action.equals(ACTION_REGISTER)){
            // Check the person isn't already registered
            long found = participationDao.queryBuilder()
                    .where(
                            ParticipationDao.Properties.ContactNumber.eq(from),
                            ParticipationDao.Properties.ElectionId.eq(election.getId())
                    ).count();
            if (found > 0){
                return;
            }

            //Check that the code is correct
            if (body.contains(String.valueOf(election.getRegistrationCode()))){
                Contact sender = contactDao.queryBuilder()
                        .where(ContactDao.Properties.Number.eq(from))
                        .unique();
                if (sender == null){
                    sender = new Contact(Long.parseLong(from), from);
                    contactDao.insert(sender);
                }
                Participation participation =
                        new Participation(sender.getNumber(),election.getId());
                participationDao.insert(participation);

                Intent localIntent = new Intent(ACTION_REGISTER);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

            }
        } else if(action.equals(ACTION_APPLY)){
            Contact sender = contactDao.queryBuilder()
                    .where(ContactDao.Properties.Number.eq(from))
                    .unique();
            if (sender == null){
                return;
            }

            // Check the person can participate in this election
            long participates = participationDao.queryBuilder()
                    .where(
                            ParticipationDao.Properties.ContactNumber.eq(sender.getNumber()),
                            ParticipationDao.Properties.ElectionId.eq(election.getId())
                    ).count();
            if (participates == 0) {
                return;
            }

            List<Application> applications = applicationDao.queryBuilder()
                    .where(
                            ApplicationDao.Properties.PostId.eq(post.getId())
                    ).orderAsc(ApplicationDao.Properties.ApplicantId)
                    .list();
            for (Application test : applications){
                if (test.getContact().getNumber().equals(sender.getNumber())) {
                    return;
                }
            }

            int lastNumber;
            if (applications.size() == 0){
                lastNumber = 0;
            } else {
                lastNumber = applications.get(applications.size()-1).getCandidateNumber();
            }

            Application application = new Application();
            application.setPost(post);
            application.setContact(sender);
            application.setCandidateNumber(lastNumber + 1);

            applicationDao.insertOrReplace(application);

            Intent localIntent = new Intent(ACTION_APPLY);
            // Broadcasts the Intent to receivers in this app.
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        } else if (action.equals(ACTION_VOTE)){
            Contact sender = contactDao.queryBuilder()
                    .where(ContactDao.Properties.Number.eq(from))
                    .unique();
            if (sender == null){
                return;
            }

            // Check the person can participate in this election
            long participates = participationDao.queryBuilder()
                    .where(
                            ParticipationDao.Properties.ContactNumber.eq(sender.getNumber()),
                            ParticipationDao.Properties.ElectionId.eq(election.getId())
                    ).count();
            if (participates == 0) {
                return;
            }

            // Check the person hasn't voted more than the posts places
            List<Vote> votes = voteDao.queryBuilder()
                    .where(
                            VoteDao.Properties.ContactNumber.eq(sender.getNumber())
                    ).list();
            int count = 0;
            for (Vote vote : votes){
                if (post.getId().equals(vote.getApplication().getPostId()))
                    count++;
            }
            if (count >= post.getPlaces()) {
                return;
            }

            Vote vote = new Vote();
            vote.setContact(sender);
            String id = body.substring(0,1);
            for (Application application : post.getApplications()){
                if (id.equals(application.getCandidateNumber().toString())) {
                    vote.setApplication(application);
                    voteDao.insert(vote);

                    Intent localIntent = new Intent(ACTION_VOTE);
                    // Broadcasts the Intent to receivers in this app.
                    LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
                    return;
                }
            }
        }

    }



    private Notification getNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_template_icon_bg)
                        .setContentTitle("SMSVote")
                        .setContentText("Monitoring incoming SMS");
        Intent intent = new Intent(this, ElectionActivity.class);
        intent.putExtra("electionId", election.getId());

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ElectionActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        return mBuilder.build();
    }

    private class SMSReceiver extends BroadcastReceiver
    {
        private final String TAG = this.getClass().getSimpleName();

        private final SMSMonitorService smsMonitorService;

        public SMSReceiver(SMSMonitorService smsMonitorService){
            this.smsMonitorService = smsMonitorService;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle extras = intent.getExtras();

            String strMessage = "";

            if ( extras != null )
            {
                Object[] smsExtras = (Object[]) extras.get( "pdus" );

                for (Object smsExtra : smsExtras) {
                    SmsMessage msg = SmsMessage.createFromPdu((byte[]) smsExtra);

                    String strMsgBody = msg.getMessageBody();
                    String strMsgSrc = msg.getOriginatingAddress();

                    strMessage += "SMS from " + strMsgSrc + " : " + strMsgBody;

                    Log.d(TAG, strMessage);
                    smsMonitorService.onSMSReceive(strMsgSrc, strMsgBody);
                }

            }
            // Do not forward the SMS to other apps
            this.abortBroadcast();
        }

    }
}


