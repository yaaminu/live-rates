package com.zealous.stock

import android.support.v4.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import com.zealous.R
import com.zealous.adapter.BaseAdapter
import com.zealous.exchangeRates.ExchangeRate


class EquityAdapter(delegate: EquityAdapterDelegate) : BaseAdapter<EquityHolder, Equity>(delegate) {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EquityHolder {
        return EquityHolder(inflater.inflate(R.layout.equity_list_item, parent, false))
    }

    override fun doBindHolder(holder: EquityHolder?, position: Int) {
        if (holder != null) {
            val item = getItem(position)
            holder.apply {
                symbol.text = item.symbol
                companyName.text = item.name
                price.text = ExchangeRate.FORMAT.format(item.price)
                change.text = item.change
                change.setTextColor(ContextCompat.getColor(context, if (item.change.startsWith("-")) R.color.red else R.color.stock_up))
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
}