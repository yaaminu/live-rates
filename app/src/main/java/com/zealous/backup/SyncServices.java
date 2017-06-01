package com.zealous.backup;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

import com.zealous.utils.ConnectionUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.TaskManager;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by yaaminu on 6/1/17.
 */

public class SyncServices extends IntentService {
    private static final String TAG = "SyncServices";
    public static final String ACTION_SETUP_ALARMS = "setup.alarms",
            ACTION_SYNC = "drive.sync";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public SyncServices() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PLog.d(TAG, "sync service started with intent %s", intent.toString());
        if (ACTION_SETUP_ALARMS.equals(intent.getAction())) {
            setUpAlarms();
        } else if (ACTION_SYNC.equals(intent.getAction())) {
            if (ConnectionUtils.isConnected()) {
                TaskManager.runJob(SyncJob.create());
            } else {
                PLog.d(TAG, "unable to run google drive sync job, since there was no internet connection");
            }
        } else {
            PLog.d(TAG, "unknown intent action %s", intent.getAction());
            throw new RuntimeException();
        }
    }

    private void setUpAlarms() {
        Intent intent = new Intent(this, SyncServices.class);
        intent.setAction(ACTION_SYNC);

        PendingIntent pendingIntent = PendingIntent.getService(this, 1001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE));
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, MINUTES.toMillis(5),
                HOURS.toMillis(1), pendingIntent);
    }
}
