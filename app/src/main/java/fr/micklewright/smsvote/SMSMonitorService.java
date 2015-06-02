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
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import fr.micklewright.smsvote.database.Election;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class SMSMonitorService extends Service {
    private static final int NOTIFICATION_ID = 42;

    public static final String ACTION_REGISTER = "fr.micklewright.smsvote.action.register";
    public static final String ACTION_BAZ = "fr.micklewright.smsvote.action.BAZ";

    public static final String EXTRA_ELECTION_ID = "fr.micklewright.smsvote.extra.electionId";
    public static final String EXTRA_POST_ID = "fr.micklewright.smsvote.extra.postId";

    private Election election;
    private SMSReceiver smsReceiver;

    public SMSMonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags,startId);

        if (intent != null) {
            smsReceiver = new SMSReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
            intentFilter.setPriority(999);
            registerReceiver(smsReceiver, intentFilter);


            final long electionId = intent.getLongExtra(EXTRA_ELECTION_ID, -1);
            election = ((DaoApplication) getApplicationContext()).getDaoSession()
                    .getElectionDao().load(electionId);
        }

        startForeground(NOTIFICATION_ID, getNotification());

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(smsReceiver);
        super.onDestroy();
    }

    private Notification getNotification(){
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.notification_template_icon_bg)
                        .setContentTitle("SMSVote")
                        .setContentText("Monitoring incoming SMS");
        Intent intent = new Intent(this, ElectionActivity.class);
        intent.putExtra("electionId", election.getId());
        PendingIntent resultIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.setContentIntent(resultIntent);
        return mBuilder.build();
    }
}

class SMSReceiver extends BroadcastReceiver
{
    private final String TAG = this.getClass().getSimpleName();

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

                Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
                Log.d(TAG, strMessage);
            }

        }
        // Do not forward the SMS to other apps
        this.abortBroadcast();
    }

}
