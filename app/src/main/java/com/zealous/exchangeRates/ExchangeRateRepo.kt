package com.zealous.exchangeRates

import com.zealous.utils.PLog
import rx.Observable
import rx.schedulers.Schedulers
import java.math.BigDecimal
import java.math.MathContext
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by yaaminu on 4/7/18.
 */
const val TAG = "ExchangeRateRepo"

class ExchangeRateRepo {

    fun loadHistoricalRatesForLastNDays(currencyIso: String, days: Int): Observable<List<IndexCurrencyRate>> {
        return Observable.range(0, days)
                .map { System.currentTimeMillis() - (TimeUnit.HOURS.toMillis(24) * it) }
                .flatMap {
                    loadRatesForDate(currencyIso, Date(it)).subscribeOn(Schedulers.io())
                }.toSortedList({ rhs, lhs ->
                    if (rhs.date.time < lhs.date.time) 1 else if (rhs.date.time > lhs.date.time) -1 else 0
                })
    }

    private fun loadRatesForDate(currencyIso: String, date: Date): Observable<IndexCurrencyRate> {
        return Observable.just(IndexCurrencyRate(date, currencyIso, 0.0))
                .flatMap {
                    //                    Observable.just(IndexCurrencyRate(date, currencyIso, 1.0))
                    ExchangeRateLoader.loadHistoricalRate(it.date).map { jsonObject ->
                        PLog.d(TAG, "processing ${it.date}:${it.currencyIso}")
                        val ghsRate = jsonObject.getDouble("GHS")
                        var rate = BigDecimal.ONE.divide(BigDecimal.valueOf(jsonObject.getDouble(it.currencyIso)), MathContext.DECIMAL128)
                                .multiply(BigDecimal.valueOf(ghsRate), MathContext.DECIMAL128).toDouble()
                        when {
                            rate <= 0.01 -> rate *= 1000
                            rate <= 0.1 -> rate *= 100
                        }
                        IndexCurrencyRate(it.date, it.currencyIso, rate)
                    }
                }
    }

    data class IndexCurrencyRate(val date: Date, val currencyIso: String, val rate: Double = 0.0)
}