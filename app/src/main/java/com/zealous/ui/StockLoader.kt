package com.zealous.ui

import com.zealous.exchangeRates.ExchangeRate
import com.zealous.stock.Equity
import com.zealous.utils.PLog
import io.realm.Realm
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import rx.Completable
import rx.Observable
import rx.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by yaaminu on 4/7/18.
 */

class StockLoader {
    private val client: Lazy<OkHttpClient> = lazy { OkHttpClient() }

    /**
     * loads the latest stock  data. If this's the first time we're loading stock data,
     * it loads
     */
    fun doLoad(): Completable {
        return loadCompanyDataIfRequired()
                .concatWith(loadStockData())
                .retryWhen {
                    it.zipWith(Observable.range(1, 3), { _, t2 ->
                        t2
                    }).flatMap { Observable.timer(5L * it, TimeUnit.SECONDS) }
                }
    }

    private fun loadStockData(): Completable {
        return Completable.fromEmitter {
            val request = Request.Builder()
                    .url("https://dev.kwayisi.org/apis/gse/live/")
                    .get().build()
            val response = client.value.newCall(request)
                    .execute()
            if (response.isSuccessful) {
                val json = response.body()?.string()
                processJson(json!!)
                it.onCompleted()
            } else {
                PLog.d(TAG, "response code: ${response.code()}, message: ${response.message()}")
                it.onError(IOException("server responded with a non-2xx error code"))
            }
        }
    }

    private fun loadCompanyDataIfRequired(): Completable {
        val count = Realm.getDefaultInstance().use { it ->
            it.where(Equity::class.java)
                    .count()
        }
        return if (count == 0L) {
            Completable.fromObservable(Observable.just("https://dev.kwayisi.org/apis/gse/equities")
                    .map { url ->
                        client.value.newCall(Request.Builder()
                                .url(url).build()).execute()
                    }
                    .map {
                        if (!it.isSuccessful) throw IOException("request failed with a non 2xx error code ${it.code()}")
                        JSONArray(it.body()!!.string())
                    }.flatMap {
                        val list: MutableList<Observable<Equity>> = MutableList(it.length(), { index ->
                            getEquityWithBasicCompanyInfo(it.getJSONObject(index).getString("name")).subscribeOn(Schedulers.io())
                        })
                        Observable.merge(list)
                    }.toList()
                    .doOnNext({
                        Realm.getDefaultInstance().use { realm ->
                            realm.beginTransaction()
                            realm.copyToRealmOrUpdate(it)
                            realm.commitTransaction()
                        }
                    })
            )
        } else {
            Completable.fromEmitter { it.onCompleted() }
        }
    }


    private fun getEquityWithBasicCompanyInfo(symbol: String): Observable<Equity> {
        return Observable.just(symbol)
                .map {
                    val response = client.value.newCall(Request.Builder()
                            .url("https://dev.kwayisi.org/apis/gse/equities/$symbol").build()).execute()
                    if (!response.isSuccessful) throw IOException("request failed with a non 2xx error code ${response.code()}")
                    JSONObject(response.body()!!.string())

                }.map {
                    val equity = Equity(it.getString("name"), it.getJSONObject("company").getString("name"),
                            "", ExchangeRate.FORMAT.format(it.getDouble("price")), it.getLong("shares"),
                            0.0, 0.0, it.getDouble("capital"), 0.0,
                            0, false)
                    equity
                }
    }

    private fun processJson(payload: String) {

        Realm.getDefaultInstance().use { realm ->
            val json = JSONArray(payload)
            realm.beginTransaction()
            0.until(json.length()).forEach {
                val item = json.getJSONObject(it)
                val equity = realm.where(Equity::class.java).equalTo("symbol", item.getString("name")).findFirst()!!
                val rawStockChange = item.getDouble("change")
                val rawPrice = item.getDouble("price")
                equity.change = "${ExchangeRate.FORMAT.format(rawStockChange)} (${getChangeInPercent(rawPrice, rawStockChange)})"
                if (rawStockChange > 0) {
                    equity.change = "+${equity.change}"
                }
                equity.volume = item.getInt("volume")
                equity.price = ExchangeRate.FORMAT.format(rawPrice)
            }
            realm.commitTransaction()
        }
    }

    private fun getChangeInPercent(price: Double, change: Double): String {
        val previousPrice = price - (Math.abs(change)) //Math.abs() helps us deal with negative changes
        return "${ExchangeRate.FORMAT.format((change / previousPrice) * 100)}%"
    }
}