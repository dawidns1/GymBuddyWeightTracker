package com.example.gymbuddy_weighttracker.helpers

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.DateFormat
import java.util.*

class LineChartXAxisValueFormatter(private val refTime: Long) : IndexAxisValueFormatter() {
    override fun getFormattedValue(value: Float): String {
        val emissionsMilliSince1970Time = (value.toLong() + refTime) * 1000

        // Show time in local version
        val timeMilliseconds = Date(emissionsMilliSince1970Time)
        val dateTimeFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return dateTimeFormat.format(timeMilliseconds)
    }
}