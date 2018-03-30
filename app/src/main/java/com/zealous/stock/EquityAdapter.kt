package com.zealous.stock

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zealous.adapter.BaseAdapter


class EquityAdapter(delegate: EquityAdapterDelegate) : BaseAdapter<EquityHolder, Equity>(delegate) {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): EquityHolder {
        return EquityHolder(inflater.inflate(android.R.layout.simple_list_item_1, parent, false))
    }

    override fun doBindHolder(holder: EquityHolder?, position: Int) {
        (holder?.itemView as TextView).text = getItem(position).name
    }

}

interface EquityAdapterDelegate : BaseAdapter.Delegate<EquityHolder, Equity>{
    override fun onItemLongClick(adapter: BaseAdapter<EquityHolder, Equity>?, view: View?, position: Int, id: Long): Boolean {
        return false
    }

}

class EquityHolder(view: View) : BaseAdapter.Holder(view)