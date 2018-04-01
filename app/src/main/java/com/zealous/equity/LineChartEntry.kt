package com.zealous.equity

import com.github.mikephil.charting.data.Entry

data class LineChartEntry(val label: String, private val valueX: Float, private val valueY: Float, val timestamp: Long) : Entry(valueX, valueY)