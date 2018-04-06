package com.zealous.ui

import android.arch.lifecycle.LiveData
import com.zealous.equity.LineChartEntry
import com.zealous.stock.Equity
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.security.SecureRandom
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
                    loadLast7DaysQoutes(it)
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

    private fun loadLast7DaysQoutes(symbol: String): Observable<Pair<String, List<Double>>> {
        val list: MutableList<Double> = ArrayList(7)
        0.until(7).forEach {
            list.add(getRandomRate().toDouble())
        }
        return Observable.just(symbol to list)
    }

    fun getRandomRate(): Float {
        val random = SecureRandom()
        //maximum of 10000 and minimum of 99999
        val num = Math.abs(random.nextDouble() * (8 - 4) + 4)
        //we need an unsigned (+ve) number
        return Math.abs(num).toFloat()
    }
}