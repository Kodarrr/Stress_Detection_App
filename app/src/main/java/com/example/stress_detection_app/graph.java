package com.example.stress_detection_app;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.Random;

public class graph extends AppCompatActivity {

    private LineChart gyroChart;
    private LineDataSet xDataSet, yDataSet, zDataSet;
    private LineData lineData;
    private Handler handler;
    private int time = 0;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        // Initialize Chart
        gyroChart = findViewById(R.id.gyroChart);
        setupChart();

        // Generate random data
        handler = new Handler();
        random = new Random();
        startUpdatingGraph();
    }

    private void setupChart() {
        xDataSet = new LineDataSet(new ArrayList<>(), "X Axis");
        yDataSet = new LineDataSet(new ArrayList<>(), "Y Axis");
        zDataSet = new LineDataSet(new ArrayList<>(), "Z Axis");

        xDataSet.setColor(Color.RED);
        yDataSet.setColor(Color.GREEN);
        zDataSet.setColor(Color.BLUE);

        lineData = new LineData(xDataSet, yDataSet, zDataSet);
        gyroChart.setData(lineData);
    }

    private void startUpdatingGraph() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                time++;

                // Generate random gyroscope values (-5 to 5)
                float xValue = random.nextFloat() * 10 - 5;
                float yValue = random.nextFloat() * 10 - 5;
                float zValue = random.nextFloat() * 10 - 5;

                xDataSet.addEntry(new Entry(time, xValue));
                yDataSet.addEntry(new Entry(time, yValue));
                zDataSet.addEntry(new Entry(time, zValue));

                lineData.notifyDataChanged();
                gyroChart.notifyDataSetChanged();
                gyroChart.invalidate(); // Refresh chart

                // Call again after 500ms
                handler.postDelayed(this, 500);
            }
        }, 500);
    }
}
