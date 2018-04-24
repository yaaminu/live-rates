package com.zealous.ui

import android.arch.lifecycle.LiveData
import com.zealous.equity.LineChartEntry
import com.zealous.exchangeRates.ExchangeRate
import com.zealous.exchangeRates.ExchangeRateRepo
import com.zealous.utils.PLog
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

/**
 * Created by yaaminu on 4/6/18.
 */

class HomeHistoricalExchangeRates(var currencies: List<ExchangeRate>) : LiveData<Map<String, List<LineChartEntry>>>() {
    val TAG = "HomeHistoricalExchangeRates"

    private val map: HashMap<String, List<LineChartEntry>> = HashMap(1)
    private val rates: MutableList<List<LineChartEntry>> = ArrayList(4)
    private var subscription: Subscription? = null
    private var timerSubscription: Subscription? = null

    override fun onActive() {
        super.onActive()
        //only run once, we detect we're already running if subscription  != null
        subscription = subscription ?: doLoad()
    }

    private fun doLoad(): Subscription {
        rates.clear()
        val observables = MutableList(currencies.size, {
            loadLast30DaysForCurrency(currencies[it].currencyIso)
        })
        return Observable.merge(observables)
                .map { it ->
                    val rates = it.reversed()
                    val tmp: MutableList<LineChartEntry> = MutableList(rates.size, { index ->
                        LineChartEntry(formatDate(rates[index].date), index.toFloat(), rates[index].rate.toFloat(), System.currentTimeMillis())
                    })
                    //we are sure that the returned list is not  empty
                    rates[0].currencyIso to tmp

                }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    subscription = null
                    rates.add(it.second.toList())
                    startTimer()
                }, {
                    PLog.e(TAG, it.message, it)
                    subscription = null
                }, {
                    PLog.d(TAG, "completed loading historical rates")
                })
    }

    private fun startTimer() {
        if (hasActiveObservers()) {
            timerSubscription = timerSubscription ?: Observable.interval(5, TimeUnit.SECONDS).startWith(0L)
                    .scan(0, { accum, _ -> accum + 1 }) //we need only the counts
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (!rates.isEmpty()) {
                            map.clear()
                            map[currencies[it % rates.size].currencyIso] = rates[it % rates.size]
                            value = map
                        }
                    }
        }
    }

    override fun onInactive() {
        if (subscription?.isUnsubscribed == false) {
            subscription?.unsubscribe()
            subscription = null
        }
        if (timerSubscription?.isUnsubscribed == false) {
            timerSubscription?.unsubscribe()
            timerSubscription = null
        }
        super.onInactive()
    }

    private fun formatDate(date: Date): String = SimpleDateFormat("MMM dd", Locale.US).format(date)
    private fun loadLast30DaysForCurrency(currency: String): Observable<List<ExchangeRateRepo.IndexCurrencyRate>> {
        return ExchangeRateRepo()
                .loadHistoricalRatesForLastNDays(currency, 30)
                .retryWhen {
                    it.zipWith(Observable.range(1, 3), { throwable, rangeIndex ->
                        PLog.e(TAG, throwable.message, throwable)
                        rangeIndex
                    }).flatMap {
                        Observable.timer(3L * it, TimeUnit.SECONDS)
                    }
                }

    }
}
