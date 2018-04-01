package com.zealous.equity

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.zealous.R

const val EQUITY = "equity"
const val TAG = "EquityDetailActivity"

class EquityDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewModelProviders.of(this).get(EquityDetailViewModel::class.java)
                .setData(intent.getParcelableExtra(EQUITY)!!)
        setContentView(R.layout.activity_equity_details)
    }
}


