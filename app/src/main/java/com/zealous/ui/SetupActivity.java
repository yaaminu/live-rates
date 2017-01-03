package com.zealous.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zealous.BuildConfig;
import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.exchangeRates.ExchangeRateManager;
import com.zealous.utils.TaskManager;
import com.zealous.utils.ThreadUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.realm.Realm;

public class SetupActivity extends AppCompatActivity {
    @Bind(R.id.app_version)
    TextView appVersion;

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
        TaskManager.executeNow(new Runnable() {
            @Override
            public void run() {
                if (ThreadUtils.isMainThread()) {
                    gotoMainActivity();
                } else {
                    Realm realm = ExchangeRate.Realm(SetupActivity.this);
                    try {
                        if (realm.where(ExchangeRate.class).count() < 120) {
                            ExchangeRateManager.initialiseRates(SetupActivity.this, realm);
                        }
                        runOnUiThread(this);
                    } finally {
                        realm.close();
                    }
                    runOnUiThread(this);
                }
            }
        }, false);
    }

    private void gotoMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
