package com.zealous;

import android.app.Application;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.birbit.android.jobqueue.config.Configuration;
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
        TaskManager.init(new JobRunnerImpl(new JobManager(config)));
        CalligraphyConfig.Builder configBuilder = new CalligraphyConfig.Builder().setDefaultFontPath(null);
        CalligraphyConfig.initDefault(configBuilder.build());
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
