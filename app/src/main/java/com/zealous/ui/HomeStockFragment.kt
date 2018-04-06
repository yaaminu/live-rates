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
import com.zealous.stock.Equity
import com.zealous.utils.GenericUtils
import com.zealous.utils.PLog
import kotlinx.android.synthetic.main.fragment_home_stock.*
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class HomeStockFragment : BaseFragment() {
    private var subscription: Subscription? = null

    override fun getLayout() = R.layout.fragment_home_stock

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val delegate = DelegateImpl(context, emptyList())
        val adapter = HomeStockAdapter(delegate)
        home_stock_recycler_view.adapter = adapter
        ViewModelProviders.of(parentFragment)
                .get(HomeViewModel::class.java)
                .getWatchedStock()
                .observe(this, Observer {
                    delegate.equities = it ?: emptyList()
                    adapter.notifyDataChanged("")
                    subscription = loadHistoricalData(it!!)
                })

        val xAxis = home_stock_line_chart.xAxis
        xAxis.apply {
            setAvoidFirstLastClipping(true)
            position = XAxis.XAxisPosition.BOTTOM
        }
        home_stock_line_chart.axisLeft.setDrawZeroLine(false)
        home_stock_line_chart.setDrawGridBackground(false)
        home_stock_line_chart.description = Description().apply { text = "" }
    }


    private fun loadHistoricalData(equities: List<Equity>): Subscription {
        return ViewModelProviders.of(parentFragment)
                .get(HomeViewModel::class.java)
                .getHistoricalRatesForWatchedEquities(equities)
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

    private val onHistory = Action1<Pair<String, List<LineChartEntry>>> {
        GenericUtils.ensureConditionTrue(!it.second.isEmpty(), "can't be empty")
        val dataSet = LineDataSet(it.second, "")

        dataSet.apply {
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 1.5f
            color = R.color.colorPrimaryDark
        }

        if (home_stock_line_chart.data == null) {
            home_stock_line_chart.data = LineData(dataSet)
        } else {
            home_stock_line_chart.data.addDataSet(dataSet)
        }
    }
    private val onError = Action1<Throwable> {
        PLog.e(TAG, it.message, it)
        Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
    }

    class DelegateImpl(val context: Context, var equities: List<Equity>) : HomeStockAdapter.Delegate {

        override fun context() = context

        override fun onItemClick(adapter: BaseAdapter<HomeStockAdapter.Holder, Equity>?, view: View?, position: Int, id: Long) {
        }

        override fun onItemLongClick(adapter: BaseAdapter<HomeStockAdapter.Holder, Equity>?, view: View?, position: Int, id: Long) = true

        override fun dataSet(constrain: String?) = equities
    }

}


class HomeStockAdapter(delegate: Delegate) : BaseAdapter<HomeStockAdapter.Holder, Equity>(delegate) {

    override fun doBindHolder(holder: Holder?, position: Int) {
        val color = holder!!.context.resources.getColor(colors[position % colors.size])
        holder.sideBar.setBackgroundColor(color)
        holder.symbol.text = getItem(position).symbol
        holder.symbol.setTextColor(color)
        holder.price.text = getItem(position).price
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int) = Holder(inflater.inflate(R.layout.home_stock_list_item, parent, false))


    interface Delegate : BaseAdapter.Delegate<Holder, Equity>
    class Holder(view: View?) : BaseAdapter.Holder(view) {
        @BindView(R.id.side_bar)
        lateinit var sideBar: View
        @BindView(R.id.tv_symbol)
        lateinit var symbol: TextView
        @BindView(R.id.tv_price)
        lateinit var price: TextView

    }

    @ColorRes
    val colors = arrayOf(R.color.green_dark, R.color.red, R.color.light_blue, R.color.dark_violet)

}