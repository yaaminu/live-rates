package com.zealous.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.annotation.ColorRes
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.zealous.R
import com.zealous.adapter.BaseAdapter
import com.zealous.equity.LineChartEntry
import com.zealous.exchangeRates.ExchangeRate
import com.zealous.utils.GenericUtils
import com.zealous.utils.PLog
import kotlinx.android.synthetic.main.fragment_home_exchange_rates.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit


const val TAG = "HomeExchangeRateFragment"

class HomeExchangeRateFragment : BaseFragment() {
    private var subscription: Subscription? = null
    override fun getLayout() = R.layout.fragment_home_exchange_rates


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val delegate = DelegateImpl(context, emptyList())
        val adapter = HomeExchangeRateAdapter(delegate)
        home_exchange_rate_recycler_view.adapter = adapter


        ViewModelProviders.of(parentFragment)
                .get(HomeViewModel::class.java)
                .getWatchedCurrencies()
                .observe(this, Observer {
                    delegate.rates = it ?: emptyList()
                    adapter.notifyDataChanged("")
                    subscription = loadHistoricalData(it!!)
                })

        val xAxis = home_exchange_rate_line_chart.xAxis
        xAxis.apply {
            setAvoidFirstLastClipping(true)
            position = XAxis.XAxisPosition.BOTTOM
        }
        home_exchange_rate_line_chart.axisLeft.setDrawZeroLine(false)
        home_exchange_rate_line_chart.setDrawGridBackground(false)
        home_exchange_rate_line_chart.description = Description().apply { text = "" }
        home_exchange_rate_line_chart.clear()
    }

    private fun loadHistoricalData(currencies: List<ExchangeRate>): Subscription {
        return ViewModelProviders.of(parentFragment)
                .get(HomeViewModel::class.java)
                .getHistoricalRatesForWatchedCurrencies(currencies)
                .subscribeOn(Schedulers.io())
                .retryWhen {
                    Observable.range(1, 3).flatMap {
                        Observable.timer(3 * it.toLong(), TimeUnit.SECONDS)
                    }
                }
                .take(3)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onHistory, onError)
    }

    override fun onDestroyView() {
        subscription?.unsubscribe()
        super.onDestroyView()
    }


    val xVals = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
            "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30")

    private val onHistory = Action1<Pair<String, List<LineChartEntry>>> {
        GenericUtils.ensureConditionTrue(!it.second.isEmpty(), "can't be empty")
        val dataSet = LineDataSet(it.second, "")

        dataSet.apply {
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 1.5f
            color = getLinColor(it.first)
        }

        if (home_exchange_rate_line_chart.data == null) {
            home_exchange_rate_line_chart.data = LineData(dataSet)
        } else {
            home_exchange_rate_line_chart.data.addDataSet(dataSet)
        }
    }
    private val onError = Action1<Throwable> {
        PLog.e(TAG, it.message, it)
        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
    }

    class DelegateImpl(val context: Context, var rates: List<ExchangeRate>) : HomeExchangeRateAdapter.Delegate {

        override fun context() = context

        override fun onItemClick(adapter: BaseAdapter<HomeExchangeRateAdapter.Holder, ExchangeRate>?, view: View?, position: Int, id: Long) {
        }

        override fun onItemLongClick(adapter: BaseAdapter<HomeExchangeRateAdapter.Holder, ExchangeRate>?, view: View?, position: Int, id: Long) = true

        override fun dataSet(constrain: String?) = rates
    }

    @ColorRes
    fun getLinColor(currency: String): Int {
        return when (currency) {
            "GBP" -> R.color.green_dark
            "EUR" -> R.color.red
            "USD" -> R.color.dark_blue
            else -> R.color.dark_violet
        }
    }
}


class HomeExchangeRateAdapter(delegate: Delegate) : BaseAdapter<HomeExchangeRateAdapter.Holder, ExchangeRate>(delegate) {

    override fun doBindHolder(holder: Holder?, position: Int) {
        val color = holder!!.context.resources.getColor(colors[position % colors.size])
        holder.sideBar.setBackgroundColor(color)
        holder.currency.text = getItem(position).currencyIso
        holder.currency.setTextColor(color)
        holder.rate.text = getItem(position).rateValue
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = Holder(inflater.inflate(R.layout.home_exchange_rate_list_item, parent, false))


    interface Delegate : BaseAdapter.Delegate<Holder, ExchangeRate>
    class Holder(view: View?) : BaseAdapter.Holder(view) {
        @BindView(R.id.side_bar)
        lateinit var sideBar: View
        @BindView(R.id.tv_currency_name)
        lateinit var currency: TextView
        @BindView(R.id.tv_currency_rate)
        lateinit var rate: TextView

    }

    @ColorRes
    private val colors = arrayOf(R.color.green_dark, R.color.red, R.color.light_blue, R.color.dark_violet)

}