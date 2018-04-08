package com.zealous.ui

import android.arch.lifecycle.LiveData
import com.zealous.equity.LineChartEntry
import com.zealous.stock.Equity
import com.zealous.utils.PLog
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Created by yaaminu on 4/6/18.
 */
class HomHistoricalEquities(var equities: List<Equity>) : LiveData<Map<String, List<LineChartEntry>>>() {
    private val map: HashMap<String, List<LineChartEntry>> = HashMap(4)
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
                    map[it.first] = it.second.toList()
                    value = map
                }, {
                    PLog.e(TAG, it.message, it)
                })
    }

    private fun loadLast30DaysQoutes(symbol: String): Observable<Pair<String, List<Double>>> {
        return StockLoader().doLoadLastNDays(symbol, 30)
    }

}