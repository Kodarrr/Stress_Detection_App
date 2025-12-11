package com.example.stress_detection_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.stress_detection_app.Services.BatteryService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class BatteryActivity extends AppCompatActivity {

    private TextView batteryStatusTextView, batteryDetailsTextView, dndStatusTextView;
    private BarChart batteryChart;

    private BroadcastReceiver batteryDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int batteryLevel = intent.getIntExtra("batteryLevel", -1);
            String batteryStatus = intent.getStringExtra("batteryStatus");
            String dndStatus = intent.getStringExtra("dndStatus");

            // Update existing views
            batteryStatusTextView.setText(batteryLevel + "%");
            batteryDetailsTextView.setText(batteryStatus); // Or format this as needed
            dndStatusTextView.setText(dndStatus);

            // Update the battery chart
            updateBatteryChart(batteryLevel);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);

        // Initialize existing views
        batteryStatusTextView = findViewById(R.id.batteryStatusTextView);
        batteryDetailsTextView = findViewById(R.id.batteryDetailsTextView);
        dndStatusTextView = findViewById(R.id.dndStatusTextView);

        // Initialize and configure the bar chart
        batteryChart = findViewById(R.id.batteryChart);
        setupBatteryChart();
    }

    private void setupBatteryChart() {
        // Basic chart configuration
        batteryChart.getDescription().setEnabled(false);
        batteryChart.setDrawGridBackground(false);
        batteryChart.setDrawBarShadow(false);
        batteryChart.setFitBars(true);

        // Configure X axis (only showing battery level)
        XAxis xAxis = batteryChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(false); // No labels needed for single bar

        // Configure Y axis (0-100%)
        YAxis leftAxis = batteryChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setGranularity(10f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int)value + "%";
            }
        });

        batteryChart.getAxisRight().setEnabled(false);
        batteryChart.getLegend().setEnabled(false);
        batteryChart.setExtraBottomOffset(10f); // Add some padding
    }

    private void updateBatteryChart(int batteryLevel) {
        // Create single bar entry
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, batteryLevel));

        BarDataSet dataSet = new BarDataSet(entries, "Battery Level");

        // Set color based on battery level
        if (batteryLevel <= 20) {
            dataSet.setColor(Color.RED);
        } else if (batteryLevel <= 50) {
            dataSet.setColor(Color.YELLOW);
        } else {
            dataSet.setColor(Color.GREEN);
        }

        dataSet.setValueTextSize(12f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int)value + "%";
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.5f); // Make the bar thinner

        batteryChart.setData(barData);
        batteryChart.invalidate(); // Refresh chart
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(batteryDataReceiver,
                new IntentFilter(BatteryService.ACTION_BATTERY_UPDATE),
                Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(batteryDataReceiver);
    }
}