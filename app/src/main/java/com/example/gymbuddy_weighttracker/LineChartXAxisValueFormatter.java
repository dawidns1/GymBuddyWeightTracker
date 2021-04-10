package com.example.gymbuddy_weighttracker;

import android.content.Context;

import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class LineChartXAxisValueFormatter extends IndexAxisValueFormatter {
    private long refTime;

    public LineChartXAxisValueFormatter(long refTime) {
        this.refTime = refTime;
    }

    @Override
    public String getFormattedValue(float value) {

        long emissionsMilliSince1970Time = ((long) value + refTime) * 1000;

        // Show time in local version
        Date timeMilliseconds = new Date(emissionsMilliSince1970Time);
        DateFormat dateTimeFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());

        return dateTimeFormat.format(timeMilliseconds);
    }
}
