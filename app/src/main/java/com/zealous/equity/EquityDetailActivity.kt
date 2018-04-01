package com.zealous.equity

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.util.LruCache
import android.support.v7.app.AppCompatActivity
import android.text.format.DateUtils
import android.text.format.DateUtils.formatDateTime
import com.github.mikephil.charting.data.Entry
import com.zealous.R
import com.zealous.stock.Equity
import com.zealous.utils.Config
import com.zealous.utils.PLog
import com.zealous.utils.TaskManager
import java.lang.System.currentTimeMillis
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit

const val EQUITY = "equity"
const val TAG = "EquityDetailActivity"

class EquityDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewModelProviders.of(this).get(EquityDetailViewModel::class.java)
                .setData(intent.getParcelableExtra(EQUITY)!!)
        setContentView(R.layout.activity_equity_details)
    }
}


class EquityDetailViewModel : ViewModel() {
    private var equity: MutableLiveData<Equity> = MutableLiveData()
    private var historicalRatesLive: MutableLiveData<Pair<Int, List<LineChartEntry>>> = MutableLiveData()
    private var loading: HashSet<Int> = HashSet(4)
    private var cache: LruCache<Int, List<LineChartEntry>> = android.support.v4.util.LruCache(4)
    private var days = arrayOf("Sun", "Mon", "Tue", "Wed", "Thur", "Fri", "Sat")
    private var months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    var selectedItem: Int = 0
        set(value) {
            if (field != value) {
                doLoadHistoricalRatesAsync(equity.value!!.symbol, value)
                historicalRatesLive.value = value to (cache[value] ?: emptyList())
                field = value
            }
        }

    fun setData(equity: Equity) {
        this.equity.value = equity
        historicalRatesLive.value = selectedItem to (cache[selectedItem] ?: emptyList())
        doLoadHistoricalRatesAsync(equity.symbol, selectedItem)
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
    fun doLoadHistoricalRatesAsync(symbol: String, index: Int) {
        if (loading.add(index)) {
            TaskManager.executeNow(false) {
                Thread.sleep(4000)
                val entries = ArrayList<LineChartEntry>(testGetNumOfDataPoints(index))
                0.until(testGetNumOfDataPoints(index)).mapTo(entries) {
                    LineChartEntry(getLabel(index, it),
                            (it.toFloat()), getNextStockPrice(), currentTimeMillis())
                }
                cache.put(index, entries)
                loading.remove(index)
                TaskManager.executeOnMainThread({
                    if (index == selectedItem) {
                        historicalRatesLive.value = index to entries
                    }
                })
            }
        }
    }

    private fun getLabel(index: Int, position: Int): String {
        return when (index) {
            0 -> {
                val calendar = GregorianCalendar.getInstance()
                calendar.time = Date(currentTimeMillis() - (TimeUnit.HOURS.toMillis(23 - position.toLong())))
                "${calendar.get(Calendar.HOUR_OF_DAY)}:00 GMT"
            }
            1 -> {
                val calendar = GregorianCalendar.getInstance()
                calendar.time = Date(currentTimeMillis() - (TimeUnit.DAYS.toMillis(6 - position.toLong())))
                days[calendar.get(Calendar.DAY_OF_WEEK) - 1] //
            }
            2 -> {
                formatDateTime(
                        Config.getApplicationContext(),
                        currentTimeMillis() - (TimeUnit.DAYS.toMillis(29 - position.toLong())),
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

    fun getNextStockPrice(): Float {
        val random = SecureRandom()
        //maximum of 10000 and minimum of 99999
        val num = Math.abs(random.nextDouble() * (3000 - 2900) + 2900)
        //we need an unsigned (+ve) number
        return Math.abs(num).toFloat()
    }

    fun testGetNumOfDataPoints(index: Int): Int {
        return when (index) {
            0 -> 24
            1 -> 7
            2 -> 30
            4 -> 12
            else -> 12
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
}

data class LineChartEntry(val label: String, private val valueX: Float, private val valueY: Float, val timestamp: Long) : Entry(valueX, valueY)
