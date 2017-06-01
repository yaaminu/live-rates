package com.zealous.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zealous.backup.SyncServices;
import com.zealous.utils.PLog;

/**
 * Created by yaaminu on 6/1/17.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        PLog.d(TAG,"boot completed");
        Intent syncService = new Intent(context, SyncServices.class);
        syncService.setAction(SyncServices.ACTION_SETUP_ALARMS);
        context.startService(intent);
    }
}
