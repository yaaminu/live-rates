package com.zealous.ui

import android.arch.lifecycle.LiveData
import com.zealous.equity.LineChartEntry
import com.zealous.stock.Equity
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
                .map {
                    it.symbol
                }
                .flatMap {
                    loadLast30DaysQoutes(it)
                }.map {
                    val tmp: MutableList<LineChartEntry> = ArrayList(it.second.size)

                    for ((index, i) in it.second.withIndex()) {
                        tmp.add(LineChartEntry("${index + 1}", index.toFloat(), i.toFloat(), System.currentTimeMillis()))
                    }
                    it.first to tmp
                }.subscribeOn(Schedulers.io())
                .retryWhen {
                    Observable.range(1, 3).flatMap {
                        Observable.timer(3 * it.toLong(), TimeUnit.SECONDS)
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    map[it.first] = it.second.toList()
                    value = map
                }, {

                })
    }

    private fun loadLast30DaysQoutes(symbol: String): Observable<Pair<String, List<Double>>> {
        return Observable.error(Exception())
    }

}