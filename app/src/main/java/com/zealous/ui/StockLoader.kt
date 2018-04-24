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
import rx.exceptions.Exceptions
import rx.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by yaaminu on 4/7/18.
 */

const val BASE_URL = "https://live-rates.herokuapp.com/api/gse/"

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
                    .url("${BASE_URL}live")
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
            Completable.fromObservable(Observable.just("${BASE_URL}equities")
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
                            .url("${BASE_URL}equities/$symbol").build()).execute()
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


    private fun last24Hours(symbol: String): Observable<Pair<String, List<Double>>> {
        return Observable.just(symbol)
                .map { getHourlyJsonData(symbol) } //24 hours
                .flatMap { processData(symbol, it) }
    }

    private fun doLoadLastNMonths(symbol: String, months: Int): Observable<Pair<String, List<Double>>> {
        return Observable.just(Pair(symbol, months))
                .map {
                    getMonthlyJsonData(it.first, it.second)
                }.flatMap {
                    processData(symbol, it)
                }
    }

    fun doLoadLastNDays(symbol: String, days: Int): Observable<Pair<String, List<Double>>> {
        return Observable.just(Pair(symbol, days))
                .map {
                    getDailyJsonDataFromServer(it.first, it.second)
                }.flatMap {
                    processData(symbol, it)
                }
    }

    private fun processData(symbol: String, json: String): Observable<Pair<String, List<Double>>> {
        return Observable.just(JSONArray(json))
                .flatMap {
                    Observable.from(MutableList(it.length()) { index ->
                        val jsonObject = JSONObject(it.getString(index))
                        Pair(jsonObject.getDouble("price"), jsonObject.getLong("date"))
                    })
                }
                .doOnError(::println)
                .map { it.first }
                .toList()
                .map {
                    symbol to it
                }
    }

    private fun getDailyJsonDataFromServer(symbol: String, days: Int): String {
        //TODO replace this with a real network call
        val response = client.value.newCall(Request.Builder()
                .url("${BASE_URL}historical/daily/$symbol/$days").build())
                .execute()
        if (response.isSuccessful) {
            return response.body()!!.string()
        }
        throw IOException("Error retrieving response from server ${response.code()}")
    }

    private fun getMonthlyJsonData(symbol: String, months: Int): String {
        val response = client.value.newCall(Request.Builder()
                .url("${BASE_URL}historical/monthly/$symbol/$months").build())
                .execute()
        if (response.isSuccessful) {
            return response.body()!!.string()
        }
        throw IOException("Error retrieving response from server ${response.code()}")
    }

    private fun getHourlyJsonData(symbol: String): String {
        val response = client.value.newCall(Request.Builder()
                .url("${BASE_URL}historical/hourly/$symbol/").build())
                .execute()
        if (response.isSuccessful) {
            return response.body()!!.string()
        }
        throw IOException("Error retrieving response from server ${response.code()}")
    }

    fun loadHistorical(symbol: String, position: Int): Observable<Pair<String, List<Double>>> {
        return when (position) {
            0 -> last24Hours(symbol)
            1 -> doLoadLastNDays(symbol, 7) //last 7 days
            2 -> doLoadLastNDays(symbol, 30)//last 30 days
            3 -> doLoadLastNMonths(symbol, 12) //last 12 months
            else -> throw AssertionError()
        }
    }

    fun loadStats(symbol: String): Observable<EquityStat> {
        return Observable.just(symbol)
                .flatMap {
                    getStatsFromServer(it).subscribeOn(Schedulers.io())
                }.map {
                    EquityStat(it.getDouble("high"), it.getDouble("low"),
                            it.getDouble("open"), it.getDouble("close"))
                }.delay(3, TimeUnit.SECONDS)
    }

    private fun getStatsFromServer(symbol: String): Observable<JSONObject> {
        return Observable.just(symbol)
                .map {
                    client.value.newCall(
                            Request.Builder().url("${BASE_URL}live/stats/symbol").build()
                    ).execute()
                }.map {
                    if (!it.isSuccessful) Exceptions.propagate(IOException("Request failed with ${it.code()}, ${it.message()}"))
                    JSONObject(it.body()!!.string())
                }

    }

}

data class EquityStat(val dayHigh: Double, val dayLow: Double, val open: Double, val close: Double)