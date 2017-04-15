package com.zealous.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.zealous.BuildConfig;
import com.zealous.R;
import com.zealous.exchangeRates.ExchangeRate;
import com.zealous.exchangeRates.ExchangeRateManager;
import com.zealous.expense.BaseExpenditureProvider;
import com.zealous.expense.ExpenditureCategory;
import com.zealous.utils.TaskManager;
import com.zealous.utils.ThreadUtils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

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
                    setupRates();
                    setupExpenditureCategories();
                    runOnUiThread(this);
                }
            }

            private void setupRates() {
                Realm realm = ExchangeRate.Realm(SetupActivity.this);
                try {
                    if (realm.where(ExchangeRate.class).count() < 120) {
                        ExchangeRateManager.initialiseRates(SetupActivity.this, realm);
                    }
                    runOnUiThread(this);
                } finally {
                    realm.close();
                }
            }
        }, false);
    }

    private void setupExpenditureCategories() {
        BaseExpenditureProvider provider = new BaseExpenditureProvider();
        Realm realm = provider.getExpenditureRealm(provider.getConfiguration());
        InputStream inputStream = null;
        try {
            inputStream = getAssets().open("categories.json");
            JSONArray categories = new JSONArray(IOUtils.toString(inputStream));
            if (realm.where(ExpenditureCategory.class).count() < categories.length()) {
                realm.beginTransaction();
                for (int i = 0; i < categories.length(); i++) {

                    JSONObject category = categories.getJSONObject(i);
                    ExpenditureCategory tmp = realm.where(ExpenditureCategory.class).equalTo(ExpenditureCategory.FIELD_NAME, category.getString(ExpenditureCategory.FIELD_NAME))
                            .findFirst();
                    if (tmp == null) { //don't overwrite
                        realm.createOrUpdateObjectFromJson(ExpenditureCategory.class, category);
                    }
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
