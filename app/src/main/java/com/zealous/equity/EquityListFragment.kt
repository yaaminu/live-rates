package com.zealous.equity

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
import io.realm.RealmResults
import kotlinx.android.synthetic.main.gse_fragment_parent.*


/**
 * Created by yaaminu on 4/28/17.
 */
class EquityListFragment : BaseFragment() {


    private val changeListener: (RealmResults<Equity>) -> Unit = { _ ->
        equity_recycler_view.adapter.notifyDataSetChanged()
    }

    override fun getLayout(): Int {
        return R.layout.gse_fragment_parent
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val equities = ViewModelProviders.of(this).get(EquityViewModel::class.java).getEquities()
        equity_recycler_view.apply {
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
            adapter = EquityAdapter(DelegateImpl(equities))
            layoutManager = LinearLayoutManager(context)
        }
        equities.addChangeListener(changeListener)
    }

    private inner class DelegateImpl(val equities: RealmResults<Equity>) : EquityAdapterDelegate {

        override fun dataSet(constrain: String?) = equities

        override fun context(): Context = activity

        override fun onItemClick(adapter: BaseAdapter<EquityHolder, Equity>?, view: View?, position: Int, id: Long) {
            startActivity(Intent(context, EquityDetailActivity::class.java)
                    .apply { putExtra(EQUITY, adapter!!.getItem(position)) })
        }

    }

    override fun onDestroyView() {
        ViewModelProviders.of(this).get(EquityViewModel::class.java).getEquities().removeChangeListener(changeListener)
        super.onDestroyView()
    }
}
