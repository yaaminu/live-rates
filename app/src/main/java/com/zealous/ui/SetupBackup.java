package com.zealous.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.backup.BackupStats;
import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.zealous.BuildConfig;
import com.zealous.R;
import com.zealous.Zealous;
import com.zealous.backup.RestoreJob;
import com.zealous.backup.SyncServices;
import com.zealous.utils.Config;
import com.zealous.utils.FileUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.TaskManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.google.android.gms.common.ConnectionResult.API_UNAVAILABLE;
import static com.google.android.gms.common.ConnectionResult.NETWORK_ERROR;
import static com.google.android.gms.common.ConnectionResult.SERVICE_MISSING;
import static com.google.android.gms.common.ConnectionResult.TIMEOUT;
import static com.zealous.backup.RestoreJob.END;
import static com.zealous.backup.RestoreJob.EXPECTED;
import static com.zealous.backup.RestoreJob.RESTORED;
import static com.zealous.backup.RestoreJob.STATS;
import static com.zealous.ui.SetupActivity.KEY_ZEALOUS_SETUP_COMPLETED;

/**
 * Created by yaaminu on 5/31/17.
 */

public class SetupBackup extends BaseZealousActivity {
    private static final String TAG = "SetupBackup";

    private static final int REQUEST_CODE_SET_UP_GDRIVE_CLIENT = 1001;

    @BindView(R.id.bt_restore_google_drive)
    TextView btSetupBackup;
    @BindView(R.id.gdrive_notice)
    TextView notice;

    @BindView(R.id.tv_heading)
    TextView noticeTextView;
    @BindView(R.id.progress)
    AnimatedCircleLoadingView progress;

    private ProgressDialog progressDialog;
    private GoogleApiClient apiClient;
    private Action1<ConnectionResult> onConnected = new Action1<ConnectionResult>() {
        @Override
        public void call(ConnectionResult result) {
            progressDialog.dismiss();
            try {
                if (result.isSuccess()) {
                    Config.enableComponent(SyncServices.class);
                    Config.enableComponent(BootReceiver.class);

                    Config.getApplicationWidePrefs().edit()
                            .putBoolean(Zealous.ENABLE_GDRIVE_BACKUP, true)
                            .apply();

                    beginRestore(true);
                } else if (result.hasResolution()) {
                    result.startResolutionForResult(SetupBackup.this, REQUEST_CODE_SET_UP_GDRIVE_CLIENT);
                } else {
                    showMessage(getErrorMessage(result.getErrorCode()));
                }
            } catch (IntentSender.SendIntentException e) {
                showMessage(e.getMessage());
            }
        }
    };

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case API_UNAVAILABLE:
            case SERVICE_MISSING:
                return getString(R.string.play_services_not_available);
            case NETWORK_ERROR:
            case TIMEOUT:
                return getString(R.string.no_internet_connection);
            default:
                return getString(R.string.error_unknown);
        }
    }

    private boolean isRemoteRestore = true;

    private void beginRestore(final boolean remote) {
        progressDialog.dismiss();
        isRemoteRestore = remote;
        restoring = true;
        supportInvalidateOptionsMenu();
        //yay we are connected!!!, lets begin restore
        ButterKnife.findById(this, R.id.cloud_icon).setVisibility(View.GONE);
        ButterKnife.findById(this, R.id.layout_google_drive).setVisibility(View.GONE);
        ButterKnife.findById(this, R.id.bt_try_again).setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        progress.resetLoading();
        progress.startIndeterminate();
        noticeTextView.setText(R.string.preparing_restore);

        TaskManager.executeNow(new Runnable() {
            @Override
            public void run() {
                RestoreJob restoreJob = RestoreJob.create(remote);
                TaskManager.runJob(restoreJob);
            }
        }, true);
    }

    @Override
    protected void doCreate(@Nullable Bundle savedInstanceState) {
        super.doCreate(savedInstanceState);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle("");
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));
        progressDialog.setCancelable(false);

        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER).build();
        EventBus.getDefault()
                .register(this);
    }

    boolean restoring;

    @OnClick(R.id.bt_try_again)
    public void reAttemptRestore() {
        if (!BuildConfig.DEBUG) {
            throw new RuntimeException();
        }
        beginRestore(isRemoteRestore);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_skip).setVisible(!restoring);
        return super.onPrepareOptionsMenu(menu);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onEvent(Object event) {
        //noinspection unchecked
        Map<String, Object> mapEvent = ((Map<String, Object>) event);
        PLog.d(TAG, event.toString());
        if (mapEvent.containsKey(RestoreJob.STATS)) {
            BackupStats stats = (BackupStats) mapEvent.get(STATS);
            noticeTextView.setGravity(GravityCompat.START);
            noticeTextView
                    .setText(getString(R.string.backup_details,
                            new Date(stats.getLastModified()),
                            FileUtils.sizeInLowestPrecision(stats.getSize())));
            progress.startDeterminate();
            progress.setPercent(0);
        } else if (mapEvent.containsKey(EXPECTED)) {
            Long restored = (Long) mapEvent.get(RESTORED),
                    expected = (Long) mapEvent.get(EXPECTED);
            updateProgress(expected, restored);
        } else if (mapEvent.containsKey(END)) {
            final Throwable end = (Throwable) mapEvent.get(END);
            if (end == null) {
                noticeTextView.setText(R.string.restore_success);
                progress.setPercent(100);
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        completeSetup();
                    }
                }, 8000);
            } else {
                restoring = false;
                progress.stopFailure();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progress.resetLoading();
                        supportInvalidateOptionsMenu();
                        backupFailed(end);
                    }
                }, 1000);
            }
        }

    }

    private void updateProgress(Long expected, Long restored) {
        PLog.d(TAG, "restore progress %d/%d", restored, expected);
        progress.setPercent((int) ((restored * 100) / expected));
        noticeTextView.setText(getString(R.string.restoring_backup_progress, FileUtils.sizeInLowestPrecision(restored),
                FileUtils.sizeInLowestPrecision(expected)));
    }

    private void backupFailed(Throwable throwable) {
        ButterKnife.findById(this, R.id.progress).setVisibility(View.GONE);
        ImageView imageView = ButterKnife.findById(this, R.id.cloud_icon);
        imageView.setImageResource(R.drawable.ic_warning_black_24dp);
        imageView.setVisibility(View.VISIBLE);
        ButterKnife.findById(this, R.id.bt_try_again).setVisibility(View.VISIBLE);
        progressDialog.dismiss();
        noticeTextView.setText(getString(R.string.backup_failed_notice, throwable.getMessage()));
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault()
                .unregister(this);
        super.onDestroy();
    }

    private void connectApiClient() {
        progressDialog.show();
        checkPlayStoreAvailability()
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<Object, Observable<ConnectionResult>>() {
                    @Override
                    public Observable<ConnectionResult> call(Object o) {
                        return doConnect();
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onConnected, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        progressDialog.dismiss();
                        showMessage(throwable.getMessage());
                    }
                });
    }

    private Observable<?> checkPlayStoreAvailability() {
        return Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                int results = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(SetupBackup.this);
                if (results == ConnectionResult.SUCCESS) {
                    subscriber.onNext(results);
                } else {
                    PLog.d(TAG, GoogleApiAvailability.getInstance().getErrorString(results));
                    subscriber.onError(new Throwable(GoogleApiAvailability.getInstance().getErrorString(results)));
                }
            }
        });
    }

    private Observable<ConnectionResult> doConnect() {
        return Observable.create(new Observable.OnSubscribe<ConnectionResult>() {
            @Override
            public void call(Subscriber<? super ConnectionResult> subscriber) {
                subscriber.onNext(apiClient.blockingConnect());
            }
        });
    }

    @Override

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_skip) {
            if (isRemoteRestore) {
                requestLocalRestore();
            } else {
                completeSetup();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestLocalRestore() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    beginRestore(false);
                } else {
                    completeSetup();
                }
            }
        };
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(R.string.request_local_backup)
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, listener)
                .create().show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(100, R.id.action_skip, 100, getString(R.string.skip));
        MenuItemCompat.setShowAsAction(item, MenuItemCompat.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_setup_backup;
    }

    @OnClick(R.id.bt_restore_google_drive)
    public void onClick() {
        connectApiClient();
    }

    @Override
    protected boolean hasParent() {
        return false;
    }

    @Override
    public void onBackPressed() {
        if (!restoring) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, R.string.cannot_restore_backup, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_SET_UP_GDRIVE_CLIENT) {
            if (resultCode == RESULT_OK) {
                connectApiClient();
            } else {
                showMessage(getString(R.string.g_drive_seup_failed));
            }
        }
    }

    private void completeSetup() {
        Config.getApplicationWidePrefs().edit()
                .putBoolean(KEY_ZEALOUS_SETUP_COMPLETED, true)
                .apply();
        if (Config.getApplicationWidePrefs().getBoolean(Zealous.ENABLE_GDRIVE_BACKUP, false)) {
            Intent intent = new Intent(this, SyncServices.class);
            intent.setAction(SyncServices.ACTION_SETUP_ALARMS);
            startService(intent);
        }
        gotoMainActivity(this);
    }

    private void showMessage(String message) {
        showMessage(message, null);
    }

    private void showMessage(String message, DialogInterface.OnClickListener clickListener) {
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, clickListener)
                .create().show();
    }

    static void gotoMainActivity(Activity context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
        context.finish();
    }
}
