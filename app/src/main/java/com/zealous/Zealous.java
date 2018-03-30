package com.zealous;

import android.app.Application;
import android.support.annotation.NonNull;

import com.backup.BackupManager;
import com.backup.DependencyInjector;
import com.backup.GoogleDriveStorage;
import com.backup.LocalFileSystemStorage;
import com.backup.LogEntryGsonSerializer;
import com.backup.LoggerImpl;
import com.backup.Operation;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.birbit.android.jobqueue.config.Configuration;
import com.zealous.expense.DaggerExpenseActivityComponent;
import com.zealous.expense.ExpenseActivityComponent;
import com.zealous.expense.MultipleStorage;
import com.zealous.utils.Config;
import com.zealous.utils.ConnectionUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.Task;
import com.zealous.utils.TaskManager;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * by yaaminu on 12/14/16.
 */

public class Zealous extends Application {

    public static final String OPERATION_LOG = "operation.log";
    public static final String ENABLE_GDRIVE_BACKUP = "sync.backup.gdrive.enable";
    private static final String BACKUP_FILE_NAME = "backup";
    private JobRunnerImpl runner;
    private ExpenseActivityComponent expenseActivityComponent;


    public ExpenseActivityComponent getExpenseActivityComponent() {
        return expenseActivityComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Config.init(this);
        ConnectionUtils.init(this);
        Realm.init(this);
        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded().build());
        Configuration config = new Configuration.Builder(this)
                .customLogger(new PLog("JobManager"))
                .jobSerializer(new Task.JobSerializer())
                .build();
        runner = new JobRunnerImpl(new JobManager(config));
        TaskManager.init(runner);
        CalligraphyConfig.Builder configBuilder = new CalligraphyConfig.Builder().setDefaultFontPath(null);
        CalligraphyConfig.initDefault(configBuilder.build());
        expenseActivityComponent = DaggerExpenseActivityComponent.create();
    }

    public BackupManager getExpenseBackupManager() {
        return doGetBackupManager();
    }

    @NonNull
    private BackupManager doGetBackupManager() {
        LocalFileSystemStorage localStorage =
                new LocalFileSystemStorage(Config.getBackupDir());

        if (isGoogleDriveBackupEnabled()) {
            return BackupManager.getInstance(new LoggerImpl(OPERATION_LOG, injector, new LogEntryGsonSerializer(),
                    new MultipleStorage(localStorage, new GoogleDriveStorage(this))));
        }
        return BackupManager.getInstance(new LoggerImpl(OPERATION_LOG, injector,
                new LogEntryGsonSerializer(), localStorage));
    }

    public static boolean isGoogleDriveBackupEnabled() {
        return Config.getApplicationWidePrefs().getBoolean(ENABLE_GDRIVE_BACKUP, false);
    }

    public BackupManager getNewsBackupManager() {
        return doGetBackupManager();
    }

    static class JobRunnerImpl implements TaskManager.JobRunner {

        private final JobManager manager;

        public JobRunnerImpl(JobManager manager) {
            this.manager = manager;
        }

        @Override
        public String runJob(Task task) {
            manager.addJob(task);
            return task.getId();
        }

        @Override
        public void cancelJobs(String tag) {
            manager.cancelJobs(TagConstraint.ALL, tag);
        }

        @Override
        public void start() {
            manager.start();
        }
    }

    private final DependencyInjector injector = new DependencyInjector() {
        @Override
        public void inject(Operation operation) {
            throw new UnsupportedOperationException();
        }
    };
}
