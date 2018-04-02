package com.zealous.equity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.zealous.R
import com.zealous.exchangeRates.ExchangeRateFragment
import com.zealous.ui.BaseFragment
import kotlinx.android.synthetic.main.simple_bar_exchange_rates.*

class ExchangeRateFragmentParent : BaseFragment() {
    override fun getLayout(): Int {
        return R.layout.fragment_exchange_rate_parent
    }

    private lateinit var homeExchangeRates: Fragment
    private lateinit var allRatesFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        homeExchangeRates = ExchangeRateFragmentHome()
        allRatesFragment = ExchangeRateFragment()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        home_exchange_rate.setOnClickListener {
            all_rates.isSelected = false
            it.isSelected = true
            (it as TextView).setTextColor(ContextCompat.getColor(activity, R.color.white))
            (all_rates as TextView).setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            childFragmentManager.beginTransaction()
                    .replace(R.id.container, homeExchangeRates, "watching")
                    .commit()
        }

        all_rates.setOnClickListener {
            home_exchange_rate.isSelected = false
            it.isSelected = true

            (it as TextView).setTextColor(ContextCompat.getColor(activity, R.color.white))
            (home_exchange_rate as TextView).setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))

            childFragmentManager.beginTransaction()
                    .replace(R.id.container, allRatesFragment, "watching")
                    .commit()
        }
        home_exchange_rate.performClick()
    }
}

class ExchangeRateFragmentHome : BaseFragment() {
    override fun getLayout(): Int {
        return R.layout.fragment_exchange_rate_home
    }
}
