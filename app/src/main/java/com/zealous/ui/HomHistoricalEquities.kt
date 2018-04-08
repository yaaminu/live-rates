package com.zealous.ui

import android.arch.lifecycle.LiveData
import com.zealous.equity.LineChartEntry
import com.zealous.stock.Equity
import com.zealous.utils.PLog
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by yaaminu on 4/6/18.
 */
class HomHistoricalEquities(var equities: List<Equity>) : LiveData<Map<SelectedStock, List<LineChartEntry>>>() {
    private val map: HashMap<SelectedStock, List<LineChartEntry>> = HashMap(1)
    private val stocks: MutableList<List<LineChartEntry>> = ArrayList(4)
    private var timerSubscription: Subscription? = null
    private var subscription: Subscription? = null

    override fun onActive() {
        super.onActive()
        Observable.from(equities)
                .flatMap {
                    loadLast30DaysQoutes(it.symbol).subscribeOn(Schedulers.io())
                }.map {
                    val tmp: MutableList<LineChartEntry> = ArrayList(it.second.size)

                    for ((index, i) in it.second.withIndex()) {
                        tmp.add(LineChartEntry("${index + 1}", index.toFloat(), i.toFloat(), System.currentTimeMillis()))
                    }
                    it.first to tmp
                }.retryWhen {
                    it.zipWith(Observable.range(1, 3), { _, num -> num }).flatMap {
                        Observable.timer(3 * it.toLong(), TimeUnit.SECONDS)
                    }
                }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    subscription = null
                    stocks.add(it.second.toList())
                    startTimer()
                }, {
                    PLog.e(TAG, it.message, it)
                    subscription = null
                }, {
                    PLog.d(TAG, "completed loading historical rates")
                })
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

    private fun loadLast30DaysQoutes(symbol: String): Observable<Pair<String, List<Double>>> {
        return StockLoader().doLoadLastNDays(symbol, 30)
    }

    private fun startTimer() {
        if (hasActiveObservers()) {
            timerSubscription = timerSubscription ?: Observable.interval(7, TimeUnit.SECONDS).startWith(0L)
                    .scan(0, { accum, _ -> accum + 1 }) //we need only the counts
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (!stocks.isEmpty()) {
                            map.clear()
                            val position = it % stocks.size
                            map[SelectedStock(equities[position].symbol, position)] = stocks[position]
                            value = map
                        }
                    }) {
                        PLog.e(TAG, it.message, it)
                        startTimer()
                    }
        }
    }
}

data class SelectedStock(val symbol: String, val position: Int)