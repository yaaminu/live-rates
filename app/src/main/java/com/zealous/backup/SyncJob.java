package com.zealous.backup;

import android.support.annotation.NonNull;

import com.backup.GoogleDriveStorage;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.zealous.Zealous;
import com.zealous.utils.Config;
import com.zealous.utils.PLog;
import com.zealous.utils.Task;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

/**
 * Created by yaaminu on 6/1/17.
 */

public class SyncJob extends Task {

    public SyncJob() {
    }


    private SyncJob(Params params) {
        super(params);
    }

    public static SyncJob create() {
        Params params = new Params(100);
        params.setGroupId("syncdrive");
        params.setPersistent(true);
        params.setRequiresNetwork(true);
        return new SyncJob(params);
    }

    @Override
    protected JSONObject toJSON() {
        return new JSONObject();
    }

    @Override
    protected Task fromJSON(JSONObject jsonObject) {
        return create();
    }

    @Override
    public void onRun() throws Throwable {
        PLog.d(TAG, "running job %s", getClass().getName());
        GoogleDriveStorage storage = new GoogleDriveStorage(Config.getApplicationContext());
        storage.initiaize(Config.getApplicationContext());
        storage.sync(Zealous.OPERATION_LOG);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        PLog.d(TAG, "sync job failed ", throwable);
        return RetryConstraint.createExponentialBackoff(runCount, TimeUnit.MINUTES.toMillis(5));
    }

    @Override
    protected int getRetryLimit() {
        return 3;
    }
}
