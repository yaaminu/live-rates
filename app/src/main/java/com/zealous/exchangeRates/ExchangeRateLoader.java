package com.zealous.exchangeRates;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;

import com.zealous.utils.Config;
import com.zealous.utils.GenericUtils;
import com.zealous.utils.PLog;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Subscriber;

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

    @SuppressLint("CommitPrefEdits")
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
            InputStream in = response.body().byteStream();
            json = IOUtils.toString(in);
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
            PLog.d(TAG, "cache hit for url ", url);
        } else {
            PLog.d(TAG, "cache miss for url ", url);
        }
        return cache;
    }

    public static Observable<JSONObject> loadHistoricalRate(final Date date) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(Subscriber<? super JSONObject> subscriber) {
                try {
                    subscriber.onNext(doLoadRates("/historical/" + formatDate(date) + ".json"));
                } catch (IOException | JSONException e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    private static String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);
    }
}
