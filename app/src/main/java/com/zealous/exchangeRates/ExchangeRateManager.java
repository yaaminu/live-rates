package com.zealous.exchangeRates;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;

import com.zealous.utils.Config;
import com.zealous.utils.TaskManager;
import com.zealous.utils.ThreadUtils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;

/**
 * Created by yaaminu on 12/23/16.
 */

public class ExchangeRateManager {

    private static final String RATES_PREFERENCES = "rates.preferences";
    private static final String RATES_LAST_UPDATED = "rates.last.updated";

    public static synchronized void loadRates() {
        if (System.currentTimeMillis() - lastUpdated() > AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15) {
            TaskManager.executeNow(new Runnable() {
                @Override
                public void run() {
                    TaskManager.runJob(LoadRatesTask.create());
                }
            }, false);
        }
    }

    @SuppressLint("CommitPrefEdits")
    static void updateLastUpdated() {
        Config.getPreferences(RATES_PREFERENCES).edit()
                .putLong(RATES_LAST_UPDATED, System.currentTimeMillis())
                .commit();
    }

    public static long lastUpdated() {
        return Config.getPreferences(RATES_PREFERENCES)
                .getLong(RATES_LAST_UPDATED, 0);
    }

    public static void initialiseRates(Context context, Realm realm) {
        ThreadUtils.ensureNotMain();
        try {
            InputStream inputStream = context.getAssets().open("currencies.json");
            JSONArray array = new JSONArray(IOUtils.toString(inputStream));
            realm.beginTransaction();
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.getJSONObject(i);
                ExchangeRate rate = jsonObjectToExchangeRate(entry);
                realm.copyToRealmOrUpdate(rate);
            }
            realm.commitTransaction();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private static ExchangeRate jsonObjectToExchangeRate(JSONObject entry) throws JSONException {
        String code = entry.getString("code");
        boolean watching = (code.equals("USD") || code.equals("GBP") ||
                code.equals("EUR") || code.equals("CAD") || code.equals("XOF") || code.equals("NGN"));
        return new ExchangeRate(code, entry.getString("name"),
                entry.getString("symbol"), entry.getString("name_plural"), watching);
    }
}
