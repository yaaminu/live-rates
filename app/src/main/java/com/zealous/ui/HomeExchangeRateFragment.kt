package com.zealous.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.util.ArrayMap
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.zealous.R
import com.zealous.adapter.BaseAdapter
import com.zealous.exchangeRates.ExchangeRate
import com.zealous.exchangeRates.ExchangeRateDetailActivity
import kotlinx.android.synthetic.main.fragment_home_exchange_rates.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


const val TAG = "HomeExchangeRateFragment"

class HomeExchangeRateFragment : BaseFragment() {
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
                    loadHistoricalData(it!!)
                })

        val xAxis = home_exchange_rate_line_chart.xAxis
        xAxis.apply {
            setAvoidFirstLastClipping(true)
            position = XAxis.XAxisPosition.BOTTOM
            valueFormatter = formatter
        }
        home_exchange_rate_line_chart.axisLeft.setDrawZeroLine(false)
        home_exchange_rate_line_chart.setDrawGridBackground(false)
        home_exchange_rate_line_chart.description = Description().apply { text = "" }
        home_exchange_rate_line_chart.clear()
    }

    val dateCache: ArrayMap<Int, String> = ArrayMap(30)
    private val dateFormatter = SimpleDateFormat("MMM dd", Locale.US)

    private val formatter = object : IAxisValueFormatter {
        override fun getFormattedValue(value: Float, axis: AxisBase?): String {
            var date = dateCache[value.toInt()]
            if (date == null) {
                date = dateFormatter.format(Date(System.currentTimeMillis() - (value.toInt() * TimeUnit.HOURS.toMillis(24))))
                dateCache[value.toInt()] = date
            }
            return date!!
        }

        override fun getDecimalDigits(): Int {
            return -1
        }


    }


    private fun loadHistoricalData(currencies: List<ExchangeRate>) {
        ViewModelProviders.of(parentFragment)
                .get(HomeViewModel::class.java)
                .getHistoricalRatesForWatchedCurrencies(currencies)
                .observe(this, Observer {
                    val datasets: MutableList<LineDataSet> = ArrayList<LineDataSet>()

                    if (it != null) {
                        for (key in it.keys) {
                            datasets.add(LineDataSet(it[key]!!, key).apply {
                                setDrawCircles(false)
                                setDrawValues(false)
                                lineWidth = 1.5f
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                                color = getLinColor(key)
                            })
                        }
                    }
                    home_exchange_rate_line_chart.clear()
                    home_exchange_rate_line_chart.data = LineData(datasets.toList())
                })
    }

    class DelegateImpl(val context: Context, var rates: List<ExchangeRate>) : HomeExchangeRateAdapter.Delegate {

        override fun context() = context

        override fun onItemClick(adapter: BaseAdapter<HomeExchangeRateAdapter.Holder, ExchangeRate>?, view: View?, position: Int, id: Long) {
            val intent = Intent(context, ExchangeRateDetailActivity::class.java)
            intent.putExtra(ExchangeRateDetailActivity.EXTRA_CURRENCY_SOURCE, "GHS")
            intent.putExtra(ExchangeRateDetailActivity.EXTRA_START_WITH, if (adapter!!.getItem(position).rate >= 1) "GHS" else adapter.getItem(position).currencyIso)
            intent.putExtra(ExchangeRateDetailActivity.EXTRA_CURRENCY_TARGET, adapter.getItem(position).currencyIso)
            context.startActivity(intent)
        }

        override fun onItemLongClick(adapter: BaseAdapter<HomeExchangeRateAdapter.Holder, ExchangeRate>?, view: View?, position: Int, id: Long) = true

        override fun dataSet(constrain: String?) = rates
    }

    @ColorRes
    private fun getLinColor(currency: String): Int {
        return when (currency) {
            "GBP" -> ContextCompat.getColor(context, R.color.green_dark)
            "EUR" -> ContextCompat.getColor(context, R.color.red)
            "USD" -> ContextCompat.getColor(context, R.color.dark_blue)
            "XOF" -> ContextCompat.getColor(context, R.color.dark_violet)
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