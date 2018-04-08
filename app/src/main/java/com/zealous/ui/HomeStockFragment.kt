package com.zealous.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.zealous.R
import com.zealous.adapter.BaseAdapter
import com.zealous.equity.EQUITY
import com.zealous.equity.EquityDetailActivity
import com.zealous.stock.Equity
import kotlinx.android.synthetic.main.fragment_home_stock.*


@ColorRes
private val lineColors = arrayOf(R.color.green_dark, R.color.red, R.color.light_blue, R.color.dark_violet)

class HomeStockFragment : BaseFragment() {

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
                    loadHistoricalData(it!!)
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


    private fun loadHistoricalData(equities: List<Equity>) {
        val homeViewModel = ViewModelProviders.of(parentFragment)
                .get(HomeViewModel::class.java)
        homeViewModel
                .getHistoricalRatesForWatchedEquities(equities)
                .observe(this, Observer {
                    val datasets: MutableList<LineDataSet> = ArrayList<LineDataSet>()

                    if (it != null) {
                        for (key in it.keys) {
                            datasets.add(LineDataSet(it[key]!!, key.symbol).apply {
                                setDrawCircles(false)
                                setDrawValues(false)
                                lineWidth = 1.5f
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                                color = ContextCompat.getColor(context, lineColors[key.position % lineColors.size])
                            })
                        }
                    }
                    home_stock_recycler_view.smoothScrollToPosition(it?.keys?.iterator()?.next()?.position
                            ?: 0)
                    home_stock_line_chart.clear()
                    home_stock_line_chart.data = LineData(datasets.toList())
                })
    }


    class DelegateImpl(val context: Context, var equities: List<Equity>) : HomeStockAdapter.Delegate {

        override fun context() = context

        override fun onItemClick(adapter: BaseAdapter<HomeStockAdapter.Holder, Equity>?, view: View?, position: Int, id: Long) {
            context.startActivity(Intent(context, EquityDetailActivity::class.java)
                    .apply { putExtra(EQUITY, adapter!!.getItem(position)) })
        }

        override fun onItemLongClick(adapter: BaseAdapter<HomeStockAdapter.Holder, Equity>?, view: View?, position: Int, id: Long) = true

        override fun dataSet(constrain: String?) = equities
    }

}


class HomeStockAdapter(delegate: Delegate) : BaseAdapter<HomeStockAdapter.Holder, Equity>(delegate) {

    override fun doBindHolder(holder: Holder?, position: Int) {
        val color = holder!!.context.resources.getColor(lineColors[position % lineColors.size])
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

}