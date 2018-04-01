package com.zealous.equity

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.zealous.R
import com.zealous.ui.BaseFragment
import kotlinx.android.synthetic.main.fragment_equity_overview.*

class EquityOverviewFragment : BaseFragment(), IAxisValueFormatter {
    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return equityDetailViewModel.getxAxisLabel(value)
    }

    override fun getDecimalDigits(): Int {
        return 0
    }

    override fun getLayout(): Int {
        return R.layout.fragment_equity_overview
    }

    private lateinit var equityDetailViewModel: EquityDetailViewModel

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = listOf(
                "1D", "1W", "1M", "1Y", "MAX"
        )

        equityDetailViewModel = ViewModelProviders.of(activity)
                .get(EquityDetailViewModel::class.java)

        for (child in 0.until(duration_parent_layout.childCount)) {
            (duration_parent_layout.getChildAt(child) as TextView).apply {
                text = title[child]
                setOnClickListener(onChildClicked)
                tag = child
            }
        }

        equityDetailViewModel.getHistoricalData().observe(this, Observer {
            renderGraph(equityDetailViewModel.describeIndex(it!!.first), "", it.second)
        })

        duration_parent_layout.getChildAt(equityDetailViewModel.selectedItem).isSelected = true
        val xAxis = line_chart.xAxis
        xAxis.valueFormatter = this
        line_chart.axisLeft.setDrawZeroLine(false)
        line_chart.setDrawGridBackground(false)
        line_chart.description = Description().apply { text = "" }
    }

    private val onChildClicked = { view: View ->
        duration_parent_layout.getChildAt(equityDetailViewModel.selectedItem)?.isSelected = false
        if (!view.isSelected) {
            equityDetailViewModel.selectedItem = view.tag as Int
            view.isSelected = true
        }
    }

    private fun renderGraph(xaxisLabel: String, description: String, entries: List<LineChartEntry>) {
        line_chart.clear()
        if (!entries.isEmpty()) {
            val lineDataSet = LineDataSet(entries, xaxisLabel)
            lineDataSet.apply {
                setDrawFilled(true)
                setDrawCircles(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                setDrawValues(false)
                lineWidth = 2f
                fillColor = resources.getColor(R.color.stock_up)
                color = resources.getColor(R.color.green_dark)
            }

            val lineData = LineData(lineDataSet)
            line_chart.data = lineData
        }
    }
}


