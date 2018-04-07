package com.zealous.equity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.zealous.R
import com.zealous.adapter.BaseAdapter
import com.zealous.stock.Equity
import com.zealous.stock.EquityAdapter
import com.zealous.stock.EquityAdapterDelegate
import com.zealous.stock.EquityHolder
import com.zealous.ui.BaseFragment
import kotlinx.android.synthetic.main.gse_fragment_parent.*


/**
 * Created by yaaminu on 4/28/17.
 *
 * Responsible for showing list of stocks
 */
class EquityListFragment : BaseFragment() {


    /**
     * @inheritDoc
     */
    override fun getLayout(): Int {
        return R.layout.gse_fragment_parent
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val delegate = DelegateImpl(emptyList())
        val equityAdapter = EquityAdapter(delegate)
        equity_recycler_view.apply {
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            adapter = equityAdapter
            layoutManager = LinearLayoutManager(context)
        }
        ViewModelProviders.of(this).get(EquityViewModel::class.java).getEquities()
                .observe(this, Observer {
                    delegate.equities = it ?: emptyList()
                    equityAdapter.notifyDataChanged("")
                })
    }

    private inner class DelegateImpl(var equities: List<Equity>) : EquityAdapterDelegate {

        /**
         * @inheritDoc
         */
        override fun dataSet(constrain: String?) = equities

        /**
         * @inheritDoc
         */
        override fun context(): Context = activity

        /**
         * @inheritDoc
         */
        override fun onItemClick(adapter: BaseAdapter<EquityHolder, Equity>?, view: View?, position: Int, id: Long) {
            startActivity(Intent(context, EquityDetailActivity::class.java)
                    .apply { putExtra(EQUITY, adapter!!.getItem(position)) })
        }

    }
}
