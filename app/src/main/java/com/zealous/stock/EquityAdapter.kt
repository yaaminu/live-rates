package com.zealous.stock

import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import butterknife.BindView
import com.zealous.R
import com.zealous.adapter.BaseAdapter
import com.zealous.utils.ViewUtils


class EquityAdapter(delegate: EquityAdapterDelegate) : BaseAdapter<EquityHolder, Equity>(delegate) {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EquityHolder {
        return EquityHolder(inflater.inflate(R.layout.stock_list_item, parent, false))
    }

    override fun doBindHolder(holder: EquityHolder?, position: Int) {
        if (holder != null) {
            val item = getItem(position)
            val bgResource = when {
                item.change.startsWith("-") -> R.drawable.stock_list_item_fall
                item.change.startsWith("+") -> R.drawable.stock_list_item_rise
                else -> R.drawable.equity_list_item_bg_no_change
            }

            holder.apply {
                symbol.text = item.symbol
                companyName.text = item.name
                price.text = context.getString(R.string.price_template, "GH₵", item.price)
                change.text = item.change
                volume.text = context.getString(R.string.day_volume, item.volume)
                itemView.setBackgroundResource(0)
                itemView.setBackgroundResource(bgResource)
                ViewUtils.showByFlag(item.isFavorite, isFavorite)
                isFavorite.setImageResource(R.drawable.ic_notifications_active_black_24dp)
            }
        }
    }

}

interface EquityAdapterDelegate : BaseAdapter.Delegate<EquityHolder, Equity> {
    override fun onItemLongClick(adapter: BaseAdapter<EquityHolder, Equity>?, view: View?, position: Int, id: Long): Boolean {
        return false
    }

}

class EquityHolder(view: View) : BaseAdapter.Holder(view) {
    @BindView(R.id.tv_symbol)
    lateinit var symbol: TextView
    @BindView(R.id.tv_company_name)
    lateinit var companyName: TextView
    @BindView(R.id.tv_price)
    lateinit var price: TextView
    @BindView(R.id.tv_change)
    lateinit var change: TextView
    @BindView(R.id.tv_market_volume)
    lateinit var volume: TextView
    @BindView(R.id.ib_is_favorite)
    lateinit var isFavorite: ImageButton
}