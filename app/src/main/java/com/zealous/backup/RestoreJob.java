package com.zealous.backup;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.backup.BackupException;
import com.backup.BackupManager;
import com.backup.BackupStats;
import com.backup.DependencyInjector;
import com.backup.GoogleDriveStorage;
import com.backup.LocalFileSystemStorage;
import com.backup.LogEntryGsonSerializer;
import com.backup.LoggerImpl;
import com.backup.Operation;
import com.backup.Storage;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.zealous.*;
import com.zealous.R;
import com.zealous.expense.BaseExpenditureOperation;
import com.zealous.expense.ExpenditureDataSource;
import com.zealous.expense.ExpenditureRepo;
import com.zealous.utils.Config;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.Task;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by yaaminu on 6/1/17.
 */

public class RestoreJob extends Task {

    private static final String TAG = "RestoreJob";

    public static final String RESTORE_LOCAL = "local";
    public static final String RESTORE_REMOTE = "remote";


    public static final String EXPECTED = "expected";
    public static final String RESTORED = "restored";
    public static final String END = "error";
    public static final String STATS = "stats";
    private boolean restoreFromRemote;

    public RestoreJob() {
    }

    private RestoreJob(Params params, boolean restoreFromRemote) {
        super(params);
        this.restoreFromRemote = restoreFromRemote;
    }

    public static RestoreJob create(boolean restoreFromRemote) {
        Params params = new Params(100);
        params.addTags("restore");
        params.setGroupId("restore");
        params.setPersistent(false);
        params.setRequiresNetwork(false);
        return new RestoreJob(params, restoreFromRemote);
    }

    @Override
    protected JSONObject toJSON() {
        return new JSONObject();
    }

    @Override
    protected Task fromJSON(JSONObject jsonObject) {
        return new RestoreJob();
    }

    @Override
    public void onRun() throws Throwable {
        PLog.d(TAG, "running  job %s", getClass().getName());
        handleIntent(restoreFromRemote);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        PLog.d(TAG, "Restore job failed", throwable);
        EventBus.getDefault()
                .post(Collections.singletonMap(END, throwable));
        PLog.d(TAG, throwable.getMessage(), throwable);
        return RetryConstraint.CANCEL;
    }

    private void handleIntent(boolean restoreFromRemote) {
        String restoreFrom = restoreFromRemote ? RESTORE_REMOTE : RESTORE_LOCAL;

        GenericUtils.ensureNotEmpty(restoreFrom);
        Storage storage;
        if (RESTORE_LOCAL.equals(restoreFrom)) {
            storage = new LocalFileSystemStorage(Config.getBackupDir());
        } else //noinspection ConstantConditions
            if (RESTORE_REMOTE.equals(restoreFrom)) {
                storage = new GoogleDriveStorage(Config.getApplicationContext());
                ((GoogleDriveStorage) storage).initiaize(Config.getApplicationContext());
            } else {
                throw new RuntimeException();
            }
        ExpenditureDataSource expenseDatasource = null;
        try {


            expenseDatasource = new ExpenditureDataSource(Realm.getInstance(configuration), null);
            BackupManager manager =
                    BackupManager.getInstance(new LoggerImpl(Zealous
                            .OPERATION_LOG, new DepInjector(expenseDatasource)
                            , new LogEntryGsonSerializer(), storage));

            BackupStats stats = manager.stats();
            EventBus.getDefault()
                    .post(Collections.singletonMap(STATS, stats));
            manager.restore(new BackupManager.ProgressListener() {
                @Override
                public void onStart(long expected) {
                    Map<String, Object> params = new HashMap<>(2);
                    params.put(EXPECTED, expected);
                    params.put(RESTORED, 0L);
                    EventBus.getDefault()
                            .post(params);
                    SystemClock.sleep(5000);
                }

                @Override
                public void onProgress(long expected, long restored) {
                    Map<String, Object> params = new HashMap<>(2);
                    params.put(EXPECTED, expected);
                    params.put(RESTORED, restored);
                    EventBus.getDefault()
                            .post(params);
                    SystemClock.sleep(100);
                }

                @Override
                public void done(BackupException e) {
                    if (e == null) {
                        EventBus.getDefault()
                                .post(Collections.singletonMap(END, null));
                    } else {
                        PLog.d(TAG, e.getMessage(), e);
                        EventBus.getDefault()
                                .post(Collections.singletonMap(END, new Throwable(getErrorMessage(e), e)));
                    }
                }
            });
        } catch (BackupException e) {
            PLog.d(TAG, e.getMessage(), e);
            EventBus.getDefault()
                    .post(Collections.singletonMap(END, new Throwable(getErrorMessage(e), e)));
        } finally {
            if (expenseDatasource != null) {
                expenseDatasource.close();
            }
        }
    }

    private String getErrorMessage(BackupException exception) {
        switch (exception.getErrorCode()) {
            case BackupException.EIOERROR:
                return GenericUtils.getString(R.string.no_internet_connection);
            case BackupException.EUNKNOWN:
            default:
                return GenericUtils.getString(R.string.error_backup_restore);
        }
    }


    private static class DepInjector implements DependencyInjector {
        private ExpenditureDataSource expenditureDataSource;

        public DepInjector(ExpenditureDataSource expenseDatasources) {
            this.expenditureDataSource = expenseDatasources;
        }

        @Override
        public void inject(Operation operation) {
            if (operation instanceof BaseExpenditureOperation) {
                ((BaseExpenditureOperation) operation).dataSource = expenditureDataSource;
            }
        }
    }

    private final RealmConfiguration configuration = new RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .name("expenditure.realm")
            .modules(new ExpenditureRepo())
            .directory(Config.getApplicationContext().getDir("expenditures.data", Context.MODE_PRIVATE)).build();

}
