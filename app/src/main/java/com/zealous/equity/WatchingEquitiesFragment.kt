package com.zealous.equity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.zealous.R
import com.zealous.adapter.BaseAdapter
import com.zealous.stock.Equity
import com.zealous.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_watching_equities.*

class WatchingEquitiesFragment : BaseFragment() {
    override fun getLayout(): Int {
        return R.layout.fragment_watching_equities
    }

    private var equities = emptyList<Equity>()
    private var adapter: BaseAdapter<*, Equity>? = null

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = WatchingAdapter(delegate)
        watching_recycler_view.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = this@WatchingEquitiesFragment.adapter
        }
        ViewModelProviders.of(activity)
                .get(EquityViewModel::class.java)
                .getWatchedEquities().observe(this, Observer {
            equities = it ?: emptyList()
            adapter!!.notifyDataChanged("")
        })
    }

    private var delegate = object : BaseAdapter.Delegate<WatchingEquityHolder, Equity> {
        override fun context() = context

        override fun onItemClick(adapter: BaseAdapter<WatchingEquityHolder, Equity>?, view: View?, position: Int, id: Long) {}

        override fun onItemLongClick(adapter: BaseAdapter<WatchingEquityHolder, Equity>?, view: View?, position: Int, id: Long) = false

        override fun dataSet(constrain: String?): List<Equity> = equities

    }

}

class WatchingAdapter(delegate: BaseAdapter.Delegate<WatchingEquityHolder, Equity>)
    : BaseAdapter<WatchingEquityHolder, Equity>(delegate) {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): WatchingEquityHolder {
        return WatchingEquityHolder(inflater.inflate(R.layout.watched_equity_pager_item, parent, false))
    }

    override fun doBindHolder(holder: WatchingEquityHolder?, position: Int) {
        val item = getItem(position)
        holder!!.symbol.text = item.symbol.toUpperCase()
        holder.price.text = item.price
        holder.change.text = item.change
        holder.marketCap.text = Html.fromHtml("<b>Mkt. Cap:</b>: ${item.marketCapFormatted()}")
        holder.marketVolume.text = Html.fromHtml("<b>Volume:</b> ${item.volumeFormated()}")
//        holder.switch.isChecked = position % 2 == 0
    }

}


class WatchingEquityHolder(view: View) : com.zealous.adapter.BaseAdapter.Holder(view) {
    @BindView(R.id.tv_symbol)
    lateinit var symbol: TextView

    @BindView(R.id.tv_price)
    lateinit var price: TextView
    @BindView(R.id.tv_change)
    lateinit var change: TextView
    @BindView(R.id.tv_market_cap)
    lateinit var marketCap: TextView
    @BindView(R.id.tv_market_volume)
    lateinit var marketVolume: TextView
//    @BindView(R.id.sw_show_on_home_screen)
//    lateinit var switch: Switch
}
