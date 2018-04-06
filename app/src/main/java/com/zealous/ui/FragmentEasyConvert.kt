package com.zealous.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zealous.R
import com.zealous.exchangeRates.ExchangeRate
import com.zealous.utils.GenericUtils
import kotlinx.android.synthetic.main.fragment_easy_convert.*

/**
 *  by yaaminu on 4/6/18.
 */
class FragmentEasyConvert : BaseFragment() {
    private var adapter = SpinnerAdapter(emptyList())

    override fun getLayout() = R.layout.fragment_easy_convert


    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Handler().postDelayed({
            init()
        }, 500)
        tv_currency.setOnClickListener { _ ->
            showDialog()
        }
        val viewModel = ViewModelProviders.of(parentFragment).get(HomeViewModel::class.java)

        et_input_exchange_rate.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                viewModel.updateInput(text.toString().trim())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })
    }

    private fun showDialog() {
        AlertDialog.Builder(context)
                .setAdapter(adapter, { _, which ->
                    ViewModelProviders.of(parentFragment)
                            .get(HomeViewModel::class.java)
                            .updateSelectedItem(which)
                }).setTitle("Choose Target Currency")
                .create()
                .show()
    }


    private fun init() {
        val viewModel = ViewModelProviders.of(parentFragment)
                .get(HomeViewModel::class.java)

        viewModel
                .getSelectedCurrencyIndex().observe(this, Observer {
                    val exchangeRateByIndex = viewModel.getExchangeRateByIndex(it ?: 0)!!
                    tv_currency.text = exchangeRateByIndex.currencyIso
                    et_input_exchange_rate.hint = getString(R.string.amount_in, exchangeRateByIndex.currencySymbol)
                    viewModel.updateSelectedItem(it!!)
                })

        viewModel.getConvertedValue().observe(this, Observer {
            tv_results.text = if (!GenericUtils.isEmpty(it)) getString(R.string.amount_results, it) else ""
        })

        viewModel.getWatchedCurrencies().observe(this, Observer {
            adapter.items = it!!
        })

    }
}

class SpinnerAdapter(var items: List<ExchangeRate>) : android.widget.BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val convertView = convertView ?: LayoutInflater.from(parent!!.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
        (convertView as TextView).text = getItem(position).currencyName
        return convertView
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return getView(position, convertView, parent)
    }

    override fun getItem(position: Int) = items[position]

    override fun getItemId(position: Int) = 0L

    override fun getCount() = items.size
}