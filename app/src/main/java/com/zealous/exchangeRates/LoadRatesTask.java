package com.zealous.exchangeRates;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.zealous.utils.Config;
import com.zealous.utils.PLog;
import com.zealous.utils.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Iterator;

import io.realm.Realm;

/**
 * Created by yaaminu on 12/23/16.
 */

public class LoadRatesTask extends Task {
    static final String JOB_TAG = "rates";
    private static final String TAG = "LoadRatesTask";

    public LoadRatesTask() {
    }

    private LoadRatesTask(Params params) {
        super(params);
    }

    public static LoadRatesTask create() {
        Params params = new Params(10);
        params.addTags(JOB_TAG);
        params.groupBy(JOB_TAG);
        params.setRequiresNetwork(true);
        params.setPersistent(false);
        return new LoadRatesTask(params);
    }

    @Override
    protected JSONObject toJSON() {
        return new JSONObject();
    }

    @Override
    protected Task fromJSON(JSONObject jsonObject) {
        return create();
    }

    @Override
    public void onRun() throws Throwable {
        synchronized (LoadRatesTask.class) {
            Realm realm = ExchangeRate.Realm(getApplicationContext());
            try {
                JSONObject jsonObject = ExchangeRateLoader.loadRates();
                realm.beginTransaction();
                try {
                    persistRates(realm, jsonObject);
                } catch (Exception e) {
                    realm.cancelTransaction();
                    throw e;
                }
                realm.commitTransaction();
                ExchangeRateManager.updateLastUpdated();
            } finally {
                realm.close();
            }
        }
    }

    private void persistRates(Realm realm, JSONObject jsonObject) throws JSONException {
        Iterator<String> currencies = jsonObject.keys();
        double baseRate = jsonObject.getDouble("GHS");
        if (realm.where(ExchangeRate.class).count() == 0) {
            ExchangeRateManager.initialiseRates(Config.getApplicationContext(), realm);
            if (realm.where(ExchangeRate.class).count() == 0) { //still not initialized?
                throw new RuntimeException("failed to initialize rates");
            }
        }
        while (currencies.hasNext()) {
            String key = currencies.next();
            ExchangeRate rate = realm.where(ExchangeRate.class)
                    .equalTo(ExchangeRate.FIELD_CURRENCY_ISO, key).findFirst();
            if (rate != null) {
                double tempRate = BigDecimal.ONE.divide(BigDecimal.valueOf(baseRate), MathContext.DECIMAL128)
                        .multiply(BigDecimal.valueOf(jsonObject.getDouble(key)), MathContext.DECIMAL128).doubleValue();
                rate.setRate(tempRate);
                realm.copyToRealmOrUpdate(rate);
            } else {
                PLog.f(TAG, "no data for currency %s", key);
            }
        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        PLog.d(TAG, "job failed with error: " + throwable.getMessage(), throwable);
        return RetryConstraint.createExponentialBackoff(runCount, 100);
    }

}
