package com.zealous.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.github.jlmd.animatedcircleloadingview.AnimatedCircleLoadingView;
import com.zealous.BuildConfig;
import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.exchangeRates.ExchangeRateManager;
import com.zealous.utils.Config;
import com.zealous.utils.TaskManager;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import io.realm.Realm;

public class SetupActivity extends AppCompatActivity {
    public static final String KEY_ZEALOUS_SETUP_COMPLETED = "zealous.setup.completed";
    private final Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            //don't touch ui elements here
            if (!isSetup()) {
                setupRates();
                new Handler(Looper.getMainLooper())
                        .post(new Runnable() {
                            @Override
                            public void run() {
                                progress.stopOk();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        completeSetup();

                                    }
                                }, TimeUnit.SECONDS.toMillis(3));
                            }
                        });
            } else {
                completeSetup();
            }
        }

    };
    private AnimatedCircleLoadingView progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isSetup()) {
            gotoMainActivity();
        } else {

            setContentView(R.layout.activity_splash);

            TextView appVersion = ButterKnife.findById(this, R.id.app_version);
            appVersion.setText(BuildConfig.VERSION_NAME);
            progress = findViewById(R.id.progress);
        }
    }

    private void completeSetup() {
        Config.getApplicationWidePrefs().edit()
                .putBoolean(KEY_ZEALOUS_SETUP_COMPLETED, true)
                .apply();
        gotoMainActivity();
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //animate the app icon,
        Animation
                fadeIn = AnimationUtils.loadAnimation(this, R.anim.move_up);
        fadeIn.setDuration(1000);

        progress.startIndeterminate();
        ButterKnife.findById(this, R.id.tv_job_details).startAnimation(fadeIn);
        ButterKnife.findById(this, R.id.app_version).startAnimation(fadeIn);
        ButterKnife.findById(this, R.id.app_name).startAnimation(fadeIn);

        new Handler()
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        TaskManager.executeNow(initRunnable, false);
                    }
                }, TimeUnit.SECONDS.toMillis(3));
    }

    public static boolean isSetup() {
        return Config.getApplicationWidePrefs().getBoolean(KEY_ZEALOUS_SETUP_COMPLETED, false);
    }

    void setupRates() {
        Realm realm = ExchangeRate.Realm(SetupActivity.this);
        try {
            if (realm.where(ExchangeRate.class).count() < 120) {
                ExchangeRateManager.initialiseRates(SetupActivity.this, realm);
            }
        } finally {
            realm.close();
        }
    }

}
