package com.zealous.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.zealous.BuildConfig;
import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.exchangeRates.ExchangeRateManager;
import com.zealous.expense.BaseExpenditureProvider;
import com.zealous.expense.ExpenditureCategory;
import com.zealous.utils.Config;
import com.zealous.utils.TaskManager;
import com.zealous.utils.ThreadUtils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import io.realm.Realm;

public class SetupActivity extends AppCompatActivity {
    public static final String KEY_ZEALOUS_SETUP_COMPLETED = "zealous.setup.completed";
    private final Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
            //don't touch ui elements here
            if (ThreadUtils.isMainThread()) {
                gotoMainActivity();
            } else {
                if (!isSetup()) {
                    setupRates();
                    setupExpenditureCategories();
                    Config.getApplicationWidePrefs().edit()
                            .putBoolean(KEY_ZEALOUS_SETUP_COMPLETED, true)
                            .apply();
                    new Handler(Looper.getMainLooper())
                            .postDelayed(this, TimeUnit.SECONDS.toMillis(3));
                } else {
                    runOnUiThread(this);
                }
            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Config.getApplicationWidePrefs().getBoolean(KEY_ZEALOUS_SETUP_COMPLETED, false)) {
            TaskManager.executeNow(initRunnable, false);
        } else {
            setContentView(R.layout.activity_splash);

            TextView appVersion = ButterKnife.findById(this, R.id.app_version);
            ImageView appIcon = ButterKnife.findById(this, R.id.iv_app_icon);
            appVersion.setText(BuildConfig.VERSION_NAME);
            //animate the app icon,
            Animation set = AnimationUtils.loadAnimation(this, R.anim.fade_rotate),
                    fadeIn = AnimationUtils.loadAnimation(this, R.anim.move_up);
            fadeIn.setDuration(1000);

            set.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            ButterKnife.findById(this, R.id.tv_job_details).startAnimation(fadeIn);
            ButterKnife.findById(this, R.id.app_version).startAnimation(fadeIn);
            ButterKnife.findById(this, R.id.app_name).startAnimation(fadeIn);
            appIcon.startAnimation(set);
            //show app name,version, and copyright after animation end
            new Handler(Looper.getMainLooper())
                    .postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            TaskManager.executeNow(initRunnable, false);
                        }
                    }, TimeUnit.SECONDS.toMillis(9));

        }
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

    void setupExpenditureCategories() {
        BaseExpenditureProvider provider = new BaseExpenditureProvider();
        Realm realm = provider.getExpenditureRealm(provider.getConfiguration());
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("categories.json");
            JSONArray categories = new JSONArray(IOUtils.toString(inputStream));
            if (realm.where(ExpenditureCategory.class).count() == 0) {
                realm.beginTransaction();
                for (int i = 0; i < categories.length(); i++) {
                    JSONObject category = categories.getJSONObject(i);
                    realm.createOrUpdateObjectFromJson(ExpenditureCategory.class, category);
                }
                realm.commitTransaction();
            }
        } catch (IOException e) {
            throw new RuntimeException();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            realm.close();
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
