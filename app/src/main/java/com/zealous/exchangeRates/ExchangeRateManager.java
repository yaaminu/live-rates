package com.zealous.exchangeRates;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.os.SystemClock;

import com.zealous.utils.Config;
import com.zealous.utils.TaskManager;
import com.zealous.utils.ThreadUtils;

import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

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

    public static void loadHistoricalRates(final String from, final String to) {
        TaskManager.executeNow(new Runnable() {
            @Override
            public void run() {
                Realm realm = ExchangeRate.Realm(Config.getApplicationContext());
                double seed = 0.5 * (realm.where(ExchangeRate.class).equalTo(ExchangeRate.FIELD_CURRENCY_ISO, to)
                        .findFirst().getRate() +
                        realm.where(ExchangeRate.class).equalTo(ExchangeRate.FIELD_CURRENCY_ISO, from)
                                .findFirst().getRate());
                try {
                    List<ExchangeRate> rates = new ArrayList<>(27);
                    SecureRandom random = new SecureRandom();
                    for (int i = 0; i < 27; i++) {
                        ExchangeRate rate = new ExchangeRate(to, "Currency Name", "$", "Currency Name Plural", true);
                        rate.setRate(seed + random.nextDouble());
                        rates.add(rate);
                    }
                    SystemClock.sleep(2000);
                    EventBus.getDefault().post(rates);
                } finally {
                    realm.close();
                }
            }
        }, true);
    }

    public static void loadHistoricalRates(String from, String to, int year, int month, final int day) {
        TaskManager.executeNow(new Runnable() {
            @Override
            public void run() {
                List<HistoricalRateTuple> rates = new ArrayList<>(27);
                SecureRandom random = new SecureRandom();
                for (int i = 0; i < day; i++) {
                    rates.add(new HistoricalRateTuple(ExchangeRate.FORMAT.format(3 + random.nextDouble()), "" + day));
                }
                SystemClock.sleep(2000);
                EventBus.getDefault().post(rates);
            }
        }, true);
    }
}
