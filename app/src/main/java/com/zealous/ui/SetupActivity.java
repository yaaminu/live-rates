package com.zealous.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

public class SetupActivity extends AppCompatActivity {
    public static final String KEY_ZEALOUS_SETUP_COMPLETED = "zealous.setup.completed";
    @Bind(R.id.app_version)
    TextView appVersion;

    private final Runnable initRunnable = new Runnable() {
        @Override
        public void run() {
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
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        appVersion.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        TaskManager.executeNow(initRunnable, false);
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
