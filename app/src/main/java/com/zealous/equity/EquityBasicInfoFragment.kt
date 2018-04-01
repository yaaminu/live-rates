package com.zealous.equity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import com.zealous.R
import com.zealous.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_equity_basic_info.*

class EquityBasicInfoFragment : BaseFragment() {
    override fun getLayout(): Int {
        return R.layout.fragment_equity_basic_info
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val equity = ViewModelProviders.of(activity)
                .get(EquityDetailViewModel::class.java)
                .getItem().value!!
        tv_company_name.text = equity.name
        tv_change.text = equity.change
        tv_price.text = "${equity.price}"
        tv_symbol.text = equity.symbol
    }
}
