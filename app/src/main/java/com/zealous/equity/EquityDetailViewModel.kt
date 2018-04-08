package com.zealous.equity

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.support.v4.util.LruCache
import android.text.format.DateUtils
import com.zealous.stock.Equity
import com.zealous.ui.EquityStat
import com.zealous.ui.StockLoader
import com.zealous.utils.Config
import com.zealous.utils.PLog
import io.realm.Realm
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import java.util.concurrent.TimeUnit

class EquityDetailViewModel : ViewModel() {
    private var equity: MutableLiveData<Equity> = MutableLiveData()
    private var historicalRatesLive: MutableLiveData<Pair<Int, List<LineChartEntry>>> = MutableLiveData()
    private var loading: HashSet<Int> = HashSet(4)
    private var cache: LruCache<Int, List<LineChartEntry>> = LruCache(4)
    private var days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat")
    private var months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private var realm: Realm? = null

    var selectedItem: Int = 0
        set(value) {
            if (field != value) {
                doLoadHistoricalRatesAsync(equity.value!!.symbol, value)
                historicalRatesLive.value = value to (cache[value] ?: emptyList())
                field = value
            }
        }

    init {
        realm = Realm.getDefaultInstance()
    }

    override fun onCleared() {
        if (realm != null) {
            realm?.close()
            realm = null
        }
    }

    fun setData(equity: Equity) {
        this.equity.value = equity
        historicalRatesLive.value = selectedItem to (cache[selectedItem] ?: emptyList())
        doLoadHistoricalRatesAsync(equity.symbol, selectedItem)
        if (stats.value == null) {
            stats.doLoad(equity)
        }
    }

    fun getItem(): LiveData<Equity> {
        return equity
    }

    fun getHistoricalData(): LiveData<Pair<Int, List<LineChartEntry>>> {
        val data = cache[selectedItem] ?: emptyList()

        if (data.isEmpty()) { //load if it's neither loaded or loading
            doLoadHistoricalRatesAsync(equity.value!!.symbol, selectedItem)
        }
        return historicalRatesLive
    }

    //TODO move this to  it's own class
    private fun doLoadHistoricalRatesAsync(symbol: String, position: Int) {
        if (loading.add(position)) {
            Observable.just(Pair(symbol, position))
                    .flatMap {
                        StockLoader().loadHistorical(it.first, it.second)
                    }
                    .subscribeOn(Schedulers.io())
                    .map {
                        MutableList(it.second.size) { index ->
                            LineChartEntry(getLabel(position, index),
                                    index.toFloat(), it.second[index].toFloat(), System.currentTimeMillis())
                        }
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ lineChartEntries ->
                        cache.put(position, lineChartEntries)
                        loading.remove(position)
                        if (position == selectedItem) {
                            historicalRatesLive.value = position to lineChartEntries
                        }
                    }) {

                    }
        }
    }

    private fun getLabel(index: Int, position: Int): String {
        return when (index) {
            0 -> {
                val calendar = GregorianCalendar.getInstance()
                calendar.time = Date(System.currentTimeMillis() - (TimeUnit.HOURS.toMillis(23 - position.toLong())))
                "${calendar.get(Calendar.HOUR_OF_DAY)}:00 GMT"
            }
            1 -> {
                val calendar = GregorianCalendar.getInstance()
                calendar.time = Date(System.currentTimeMillis() - (TimeUnit.DAYS.toMillis(6 - position.toLong())))
                days[calendar.get(Calendar.DAY_OF_WEEK) - 1] //
            }
            2 -> {
                DateUtils.formatDateTime(
                        Config.getApplicationContext(),
                        System.currentTimeMillis() - (TimeUnit.DAYS.toMillis(29 - position.toLong())),
                        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_ALL)
            }
            3 -> {
                val cal = GregorianCalendar.getInstance()
                var tmp = cal.get(Calendar.MONTH) + (11 - position)
                if (tmp > cal.getMaximum(Calendar.MONTH)) {
                    tmp -= cal.getMaximum(Calendar.MONTH)
                }
                months[tmp]
            }
            4 -> {
                val cal = GregorianCalendar.getInstance()
                "${(cal.get(Calendar.YEAR) - position)}"
            }
            else -> {
                val cal = GregorianCalendar.getInstance()
                "${(cal.get(Calendar.YEAR) - position)}"
            }
        }
    }

    fun getxAxisLabel(value: Float): String {
        if (cache[selectedItem] != null) {
            try {
                return cache[selectedItem][value.toInt()].label
            } catch (e: IndexOutOfBoundsException) {
                PLog.d(TAG, "$value")
            }
        }
        return ""
    }

    fun describeIndex(index: Int): String {
        return when (index) {
            0 -> "Last 24 hours"
            1 -> "Last 7 days"
            2 -> "Last 30 days"
            4 -> "Last 12 months"
            5 -> "All"
            else -> ""
        }
    }

    fun updateFavorite(equity: Equity) {
        if (realm != null) {
            realm?.beginTransaction()
            equity.isFavorite = !equity.isFavorite
            val ret = realm?.copyToRealmOrUpdate(equity)
            realm?.commitTransaction()
            this.equity.value = realm?.copyFromRealm(ret)
        }
    }

    fun getStats(): LiveData<EquityStat> {
        return stats
    }


    @Suppress("UsePropertyAccessSyntax")
    private var stats = object : MutableLiveData<EquityStat>() {
        private var subscription: Subscription? = null
        override fun onActive() {
            super.onActive()
            val equityValue = equity.value
            if (equityValue != null) {
                doLoad(equityValue)
            }
            setValue(value)
        }

        fun doLoad(equity: Equity) {
            subscription = subscription ?: StockLoader().loadStats(equity.symbol)
                    .subscribeOn(Schedulers.io())
                    .retryWhen {
                        it.zipWith(Observable.range(1, 3), { _, num ->
                            num
                        }).flatMap {
                            Observable.timer(it * 3L, TimeUnit.SECONDS)
                        }
                    }.observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        value = it
                        subscription = null
                    }) {
                        subscription = null
                        PLog.e(TAG, it.message, it)
                    }
        }

        override fun onInactive() {
            if (subscription != null) {
                subscription?.unsubscribe()
                subscription = null
            }
            super.onInactive()
        }
    }
}
