package com.zealous.equity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.zealous.R
import com.zealous.ui.BaseFragment
import kotlinx.android.synthetic.main.simple_bar.*

class EquityFragmentParent : BaseFragment() {
    override fun getLayout(): Int {
        return R.layout.equity_fragment_parent
    }

    private lateinit var watchingFragment: Fragment
    private lateinit var allEquityFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        watchingFragment = WatchingEquitiesFragment()
        allEquityFragment = EquityListFragment()
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        watching.setOnClickListener {
            all_equities.isSelected = false
            it.isSelected = true
            (it as TextView).setTextColor(ContextCompat.getColor(activity, R.color.white))
            (all_equities as TextView).setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))
            childFragmentManager.beginTransaction()
                    .replace(R.id.container, watchingFragment, "watching")
                    .commit()
        }

        all_equities.setOnClickListener {
            watching.isSelected = false
            it.isSelected = true

            (it as TextView).setTextColor(ContextCompat.getColor(activity, R.color.white))
            (watching as TextView).setTextColor(ContextCompat.getColor(activity, R.color.colorPrimary))

            childFragmentManager.beginTransaction()
                    .replace(R.id.container, allEquityFragment, "watching")
                    .commit()
        }
        watching.performClick()
    }
}