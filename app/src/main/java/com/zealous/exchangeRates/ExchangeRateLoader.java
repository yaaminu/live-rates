package com.zealous.exchangeRates;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.zealous.utils.Config;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.Observable;

/**
 * @author by yaaminu on 12/23/16.
 */

class ExchangeRateLoader {

    public static final String BASE_URL = "http://openexchangerates.org/api";
    private static final String TAG = "ExchangeRateLoader";
    public static final String PREF_CACHE_$_RATES_$$$$ = TAG + "Cache$rates$$$$";
    private static final String APP_ID = "?app_id=3853267c76224d5ca095496d9f19ec40";

    public static JSONObject loadRates() throws IOException {
        try {
            return doLoadRates("/historical/" + formatDate(new Date()) + ".json");
        } catch (JSONException e) {
            PLog.e(TAG, e.getMessage(), e);
            throw new IOException(e);
        } catch (IOException e) {
            PLog.e(TAG, e.getMessage(), e);
            throw e;
        }
    }

    @SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
    @NonNull
    private static JSONObject doLoadRates(String endPoint) throws IOException, JSONException {
        String json;
        final URL url = new URL(BASE_URL + endPoint + APP_ID);
        json = checkCache(url);
        if (GenericUtils.isEmpty(json)) {
            Response response = new OkHttpClient().newCall(
                    new Request.Builder().url(url.toExternalForm())
                            .build()).execute();
            // TODO: 3/24/17 check cache headers etc
            if (!response.isSuccessful()) {
                PLog.d(TAG, "url %s recieved %s response code", url, response.code());
                throw new IOException("server responded with non-200 response code. code is: " + response.code());
            }
            ResponseBody body = response.body();
            assert body != null;
            json = body.string();
            PLog.d(TAG, "loaded %s from %s", json, url);
            PLog.d(TAG, "caching resources");
            Config.getPreferences(PREF_CACHE_$_RATES_$$$$)
                    .edit().putString(url.toExternalForm(), json)
                    .commit();
        }
        return new JSONObject(json).getJSONObject("rates");
    }

    private static String checkCache(URL url) {
        final String cache = Config.getPreferences(PREF_CACHE_$_RATES_$$$$)
                .getString(url.toExternalForm(), null);
        if (!GenericUtils.isEmpty(cache)) {
            PLog.d(TAG, "counter: %s, cache hit for url %s", String.valueOf(counter.get()), url);
        } else {
            PLog.d(TAG, "cache miss for url %s", url);
        }
        return cache;
    }

    public static AtomicInteger counter = new AtomicInteger(0);

    public static Observable<JSONObject> loadHistoricalRate(final Date date) {
        return Observable.fromCallable(new Callable<JSONObject>() {
            @Override
            public JSONObject call() throws Exception {
                return doLoadRates("/historical/" + formatDate(date) + ".json");
            }
        });
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
    }
}
