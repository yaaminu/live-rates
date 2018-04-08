package com.zealous.ui

import com.google.gson.GsonBuilder
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
import java.security.SecureRandom
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


    fun last24Hours(symbol: String): Observable<Pair<String, List<Double>>> {
        return Observable.just(symbol)
                .map { getJsonFromServer(symbol, 24) } //24 hours
                .flatMap { processData(symbol, it) }
    }

    fun doLoadLastNMonths(symbol: String, months: Int): Observable<Pair<String, List<Double>>> {
        return Observable.just(Pair(symbol, months))
                .map {
                    getJsonFromServer(it.first, it.second)
                }.flatMap {
                    processData(symbol, it)
                }
    }

    fun doLoadLastNDays(symbol: String, days: Int): Observable<Pair<String, List<Double>>> {
        return Observable.just(Pair(symbol, days))
                .map {
                    getJsonFromServer(it.first, it.second)
                }.flatMap {
                    processData(symbol, it)
                }
    }

    private fun processData(symbol: String, json: String): Observable<Pair<String, List<Double>>> {
        return Observable.just(JSONArray(json))
                .flatMap {
                    Observable.from(MutableList(it.length()) { index ->
                        Pair(it.getJSONObject(index).getDouble("price"), it.getJSONObject(index).getLong("date"))
                    })
                }.distinct { it.second }
                .map { it.first }
                .toList()
                .map {
                    symbol to it
                }
    }

    private fun getJsonFromServer(symbol: String, days: Int): String {
        //TODO replace this with a real network call
        return GsonBuilder().create()
                .toJson(MutableList(days) {
                    MockData(symbol, System.currentTimeMillis()
                            - TimeUnit.HOURS.toMillis(days.toLong() - it), getRandomRate(), 0.03)
                })
    }

    fun getRandomRate(): Double {
        val random = SecureRandom()
        //maximum of 10000 and minimum of 99999
        val num = Math.abs(random.nextDouble() * (2.8 - 1.5) + 1.5)
        //we need an unsigned (+ve) number
        return Math.abs(num)
    }

    private data class MockData(var
                                symbol: String, var
                                date: Long, var
                                price: Double, var
                                change: Double)

    fun loadHistorical(symbol: String, position: Int): Observable<Pair<String, List<Double>>> {
        return when (position) {
            0 -> last24Hours(symbol)
            1 -> doLoadLastNDays(symbol, 7) //last 7 days
            2 -> doLoadLastNDays(symbol, 30)//last 30 days
            3 -> doLoadLastNMonths(symbol, 12) //last 12 months
            else -> throw AssertionError()
        }
    }
}