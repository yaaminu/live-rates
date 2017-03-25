package com.zealous.exchangeRates;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.Context;
import android.support.annotation.NonNull;

import com.zealous.utils.Config;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;
import com.zealous.utils.TaskManager;
import com.zealous.utils.ThreadUtils;

import org.apache.commons.io.IOUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import rx.Observable;
import rx.Observer;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * Created by yaaminu on 12/23/16.
 */

public class ExchangeRateManager {

    private static final String RATES_PREFERENCES = "rates.preferences";
    private static final String RATES_LAST_UPDATED = "rates.last.updated";
    private final EventBus bus;
    private Observer<HistoricalRateTuple> observer = new Observer<HistoricalRateTuple>() {
        @Override
        public void onCompleted() {
            PLog.d(TAG, "historical rates loaded completely");
        }

        @Override
        public void onError(Throwable e) {
            PLog.e(TAG, e.getMessage(), e);
        }

        @Override
        public void onNext(HistoricalRateTuple o) {
            bus.post(o);
        }
    };

    public ExchangeRateManager(@NonNull EventBus bus) {
        GenericUtils.ensureNotNull(bus);
        this.bus = bus;
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

    public synchronized void loadRates() {
        if (System.currentTimeMillis() - lastUpdated() > AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15) {
            TaskManager.executeNow(new Runnable() {
                @Override
                public void run() {
                    TaskManager.runJob(LoadRatesTask.create());
                }
            }, false);
        }
    }

    public void loadHistoricalRates(final String from, final String to) {
        TaskManager.executeNow(new Runnable() {
            @Override
            public void run() {
                long date = System.currentTimeMillis();
                for (int i = 1; i <= 28; i++) {
                    map(new Date(date), i, to, from).subscribeOn(Schedulers.io())
                            .subscribe(observer);
                    date -= TimeUnit.DAYS.toMillis(1);
                }
            }
        }, true);
    }

    public void loadHistoricalRates(final String from, final String to, final int year, final int month, final int day) {
        TaskManager.execute(new Runnable() {
            @Override
            public void run() {
                Calendar calendar = new GregorianCalendar(year, month, day);
                for (int i = day; i > 0; i--) {
                    calendar.set(Calendar.DAY_OF_MONTH, i);
                    map(calendar.getTime(), i, to, from).subscribeOn(Schedulers.io()).subscribe(observer);
                }
            }
        }, true);
    }

    @NonNull
    private Observable<HistoricalRateTuple> map(Date date, final int i, final String to, final String from) {
        return ExchangeRateLoader.loadHistoricalRate(date).map(new Func1<JSONObject, HistoricalRateTuple>() {
            @Override
            public HistoricalRateTuple call(JSONObject jsonObject) {
                try {
                    double toRate = jsonObject.getDouble(to),
                            fromRate = jsonObject.getDouble(from);
                    double rate = BigDecimal.ONE.divide(BigDecimal.valueOf(fromRate), MathContext.DECIMAL128)
                            .multiply(BigDecimal.valueOf(toRate)).doubleValue();
                    return new HistoricalRateTuple(to, from, rate, i);
                } catch (JSONException e) {
                    throw Exceptions.propagate(e);
                }
            }
        });
    }
}
