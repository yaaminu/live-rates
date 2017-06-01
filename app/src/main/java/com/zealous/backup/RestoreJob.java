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
import com.zealous.Zealous;
import com.zealous.expense.BaseExpenditureOperation;
import com.zealous.expense.ExpenditureDataSource;
import com.zealous.expense.ExpenditureRepo;
import com.zealous.news.AddNewsFavoriteOperation;
import com.zealous.news.BaseNewsProvider;
import com.zealous.news.NewsDataSource;
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

    public static final String ACTION_RESTORE_FROM = "restoreFrom",
            RESTORE_LOCAL = "local", RESTORE_REMOTE = "remote";


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
        } else if (RESTORE_REMOTE.equals(restoreFrom)) {
            storage = new GoogleDriveStorage(Config.getApplicationContext());
            ((GoogleDriveStorage) storage).initiaize(Config.getApplicationContext());
        } else {
            throw new RuntimeException();
        }
        ExpenditureDataSource expenseDatasource = null;
        NewsDataSource newsDataSource = null;
        try {

            BaseNewsProvider baseNewsProvider = new BaseNewsProvider(null);

            newsDataSource = baseNewsProvider.createDataSource
                    (Realm.getInstance(baseNewsProvider.getConfiguration()),
                            baseNewsProvider.loader(baseNewsProvider.client(), baseNewsProvider.feedSources()));

            expenseDatasource = new ExpenditureDataSource(Realm.getInstance(configuration), null);
            BackupManager manager =
                    BackupManager.getInstance(new LoggerImpl(Zealous
                            .OPERATION_LOG, new DepInjector(expenseDatasource, newsDataSource)
                            , new LogEntryGsonSerializer(), storage));

            BackupStats stats = manager.stats();
            EventBus.getDefault()
                    .post(Collections.singletonMap(STATS, stats));
            SystemClock.sleep(1000);
            manager.restore(new BackupManager.ProgressListener() {
                @Override
                public void onStart(long expected) {
                    Map<String, Object> params = new HashMap<>(2);
                    params.put(EXPECTED, expected);
                    params.put(RESTORED, 0L);
                    EventBus.getDefault()
                            .post(params);
                }

                @Override
                public void onProgress(long expected, long restored) {
                    Map<String, Object> params = new HashMap<>(2);
                    params.put(EXPECTED, expected);
                    params.put(RESTORED, restored);
                    EventBus.getDefault()
                            .post(params);
                }

                @Override
                public void done(BackupException e) {
                    EventBus.getDefault()
                            .post(Collections.singletonMap(END, e));
                }
            });
        } catch (BackupException e) {
            EventBus.getDefault()
                    .post(Collections.singletonMap(END, e));
        } finally {
            if (expenseDatasource != null) {
                expenseDatasource.close();
            }
            if (newsDataSource != null) {
                newsDataSource.close();
            }
        }
    }


    private static class DepInjector implements DependencyInjector {
        private ExpenditureDataSource expenditureDataSource;
        private NewsDataSource newsDataSource;

        public DepInjector(ExpenditureDataSource expenseDatasources, NewsDataSource newsDataSource) {
            this.expenditureDataSource = expenseDatasources;
            this.newsDataSource = newsDataSource;
        }

        @Override
        public void inject(Operation operation) {
            if (operation instanceof BaseExpenditureOperation) {
                ((BaseExpenditureOperation) operation).dataSource = expenditureDataSource;
            } else if (operation instanceof AddNewsFavoriteOperation) {
                ((AddNewsFavoriteOperation) operation).dataSource = newsDataSource;
            }
        }
    }

    private final RealmConfiguration configuration = new RealmConfiguration.Builder()
            .deleteRealmIfMigrationNeeded()
            .name("expenditure.realm")
            .modules(new ExpenditureRepo())
            .directory(Config.getApplicationContext().getDir("expenditures.data", Context.MODE_PRIVATE)).build();

}
