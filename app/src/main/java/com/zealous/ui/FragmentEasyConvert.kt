package com.zealous.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zealous.R
import com.zealous.exchangeRates.ExchangeRate

/**
 *  by yaaminu on 4/6/18.
 */
class FragmentEasyConvert : BaseFragment() {
    override fun getLayout() = R.layout.fragment_easy_convert


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewModelProviders.of(parentFragment)
                .get(HomeViewModel::class.java)
                .getWatchedCurrencies().observe(this, Observer {
                })
    }
}

class SpinnerAdapter(var items: List<ExchangeRate>) : android.widget.BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val convertView = convertView ?: LayoutInflater.from(parent!!.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        (convertView as TextView).text = getItem(position).currencyIso
        return convertView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getView(position, convertView, parent)
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = items.size
}