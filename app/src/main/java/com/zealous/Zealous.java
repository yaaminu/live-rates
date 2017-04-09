package com.zealous;

import android.app.Application;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.birbit.android.jobqueue.config.Configuration;
import com.zealous.expense.DaggerExpenseActivityComponent;
import com.zealous.expense.ExpenseActivityComponent;
import com.zealous.utils.Config;
import com.zealous.utils.ConnectionUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.Task;
import com.zealous.utils.TaskManager;

import io.realm.Realm;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * by yaaminu on 12/14/16.
 */

public class Zealous extends Application {

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
}
