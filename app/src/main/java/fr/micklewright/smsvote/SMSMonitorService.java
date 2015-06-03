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
import android.widget.Toast;

import fr.micklewright.smsvote.database.Application;
import fr.micklewright.smsvote.database.Contact;
import fr.micklewright.smsvote.database.ContactDao;
import fr.micklewright.smsvote.database.DaoSession;
import fr.micklewright.smsvote.database.Election;
import fr.micklewright.smsvote.database.ElectionDao;
import fr.micklewright.smsvote.database.Participation;
import fr.micklewright.smsvote.database.ParticipationDao;
import fr.micklewright.smsvote.database.Post;


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
    private Application application = null;
    private Post post = null;

    private ElectionDao electionDao;
    private ParticipationDao participationDao;
    private ContactDao contactDao;

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

        connectDatabase();

        election = electionDao.load(electionId);
        startForeground(election.getId().intValue(), getNotification());

        return START_REDELIVER_INTENT;
    }

    private void connectDatabase(){
        DaoSession appSession = ((DaoApplication) getApplicationContext()).getDaoSession();
        electionDao = appSession.getElectionDao();
        participationDao = appSession.getParticipationDao();
        contactDao = appSession.getContactDao();
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


