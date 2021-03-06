package com.zealous.equity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.Html
import android.view.View
import com.zealous.R
import com.zealous.exchangeRates.ExchangeRate
import com.zealous.stock.Equity
import com.zealous.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_equity_basic_info.*

class EquityBasicInfoFragment : BaseFragment() {
    override fun getLayout(): Int {
        return R.layout.fragment_equity_basic_info
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel = ViewModelProviders.of(activity)
                .get(EquityDetailViewModel::class.java)
        viewModel
                .getItem().observe(this, Observer { equity: Equity? ->

                    tv_company_name.text = equity!!.name
                    tv_change.text = equity.change
                    tv_price.text = equity.price
                    tv_symbol.text = equity.symbol
                    ib_fav_equity.setImageResource(if (equity.isFavorite) R.drawable.ic_notifications_active_black_24dp else R.drawable.ic_notifications_none_black_24dp)
                    market_cap.text = Html.fromHtml("<b>Market Cap</b>   ${ExchangeRate.FORMAT.format(equity.marketCap)}")
                    volume.text = Html.fromHtml("<b>24H Volume</b>  ${equity.volume}")

                    back.setOnClickListener {
                        activity.finish()
                    }

                    ib_fav_equity.setOnClickListener {
                        viewModel.updateFavorite(equity)
                    }
                })
        viewModel.getStats().observe(this, Observer {
            if (it == null) {
                _24_hour_high.text = Html.fromHtml("<b>24 Hour Low</b>  ??")
                _24_hour_low.text = Html.fromHtml("<b>24 Hour High</b> ??")
                open.text = Html.fromHtml("<b>Today Open</b>  ??")
                close.text = Html.fromHtml("<b>Previous close</b>  ??")
            } else {
                _24_hour_high.text = Html.fromHtml("<b>24 Hour Low</b>  ${ExchangeRate.FORMAT.format(it.dayLow)}")
                _24_hour_low.text = Html.fromHtml("<b>24 Hour High</b> ${ExchangeRate.FORMAT.format(it.dayHigh)}")
                open.text = Html.fromHtml("<b>Today Open</b>  ${ExchangeRate.FORMAT.format(it.open)}")
                close.text = Html.fromHtml("<b>Previous close</b>  ${ExchangeRate.FORMAT.format(it.close)}")
            }
        })
    }
}
