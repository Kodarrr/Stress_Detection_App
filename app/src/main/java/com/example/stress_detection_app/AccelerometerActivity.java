//package com.example.stress_detection_app;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.github.mikephil.charting.charts.LineChart;
//import com.github.mikephil.charting.charts.BarChart;
//import com.github.mikephil.charting.components.Legend;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.components.YAxis;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
//import com.github.mikephil.charting.data.BarData;
//import com.github.mikephil.charting.data.BarDataSet;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.Locale;
//
//public class AccelerometerActivity extends AppCompatActivity {
//
//    private TextView accelData;
//    private LineChart accelChart;
//    private BarChart accelBarChart;
//    private DatabaseReference databaseReference;
//    FirebaseUser user;
//
//    private ArrayList<Entry> xEntries, yEntries, zEntries;
//    private ArrayList<com.github.mikephil.charting.data.BarEntry> xBarEntries, yBarEntries, zBarEntries;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_accelerometer);
//
//        accelData = findViewById(R.id.accelData);
//        accelChart = findViewById(R.id.accelChart); // LineChart for accelerometer
//        accelBarChart = findViewById(R.id.acccelBarChart); // BarChart for accelerometer
//
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        String userId = user.getUid();
//        databaseReference = FirebaseDatabase.getInstance().getReference("users")
//                .child(userId)
//                .child("sensorData");
//
//        xEntries = new ArrayList<>();
//        yEntries = new ArrayList<>();
//        zEntries = new ArrayList<>();
//        xBarEntries = new ArrayList<>();
//        yBarEntries = new ArrayList<>();
//        zBarEntries = new ArrayList<>();
//
//        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//        fetchAccelerometerData(todayDate);
//        updateGraph();
//    }
//
//    private void fetchAccelerometerData(String selectedDate) {
//        xEntries.clear();
//        yEntries.clear();
//        zEntries.clear();
//        accelData.setText("Fetching...");
//
//        if (user == null) {
//            Log.e("Firebase", "User not logged in!");
//            accelData.setText("User not logged in!");
//            return;
//        }
//
//        DatabaseReference dateRef = databaseReference.child(selectedDate);
//
//        dateRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
//                if (!dateSnapshot.exists()) {
//                    Log.e("Firebase", "No data found for date: " + selectedDate);
//                    accelData.setText("No data found for this date!");
//                    return;
//                }
//
//                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
//                    try {
//                        String rawJson = entrySnapshot.getValue().toString();
//                        JSONObject jsonObject = new JSONObject(rawJson);
//
//                        // Get Accelerometer values
//                        float ax = (float) jsonObject.getDouble("accelerometerX");
//                        float ay = (float) jsonObject.getDouble("accelerometerY");
//                        float az = (float) jsonObject.getDouble("accelerometerZ");
//
//                        // Update Graph Data
//                        float index = xEntries.size();
//                        xEntries.add(new Entry(index, ax));
//                        yEntries.add(new Entry(index, ay));
//                        zEntries.add(new Entry(index, az));
//
//                        // Add BarChart data for each direction (X, Y, Z)
//                        xBarEntries.add(new com.github.mikephil.charting.data.BarEntry(index, ax));
//                        yBarEntries.add(new com.github.mikephil.charting.data.BarEntry(index, ay));
//                        zBarEntries.add(new com.github.mikephil.charting.data.BarEntry(index, az));
//
//                    } catch (JSONException e) {
//                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
//                    }
//                }
//
//                runOnUiThread(() -> {
//                    accelData.setText("Data fetched for date: " + selectedDate);
//                    updateGraph(); // Update graph after fetching accelerometer entries
//                    updateBarGraph(); // Update bar graph after fetching accelerometer data
//                });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("Firebase", "Database Error: " + error.getMessage());
//            }
//        });
//    }
//
//    // Method to update the Accelerometer LineChart
//    private void updateGraph() {
//        if (xEntries.isEmpty() || yEntries.isEmpty() || zEntries.isEmpty()) {
//            Log.e("Graph", "No data to update accelerometer graph!");
//            return;
//        }
//
//        LineDataSet xDataSet = new LineDataSet(xEntries, "X-Axis");
//        xDataSet.setColor(Color.RED);
//        xDataSet.setValueTextSize(10f);
//        xDataSet.setDrawCircles(false);
//        xDataSet.setDrawValues(false);
//
//        LineDataSet yDataSet = new LineDataSet(yEntries, "Y-Axis");
//        yDataSet.setColor(Color.GREEN);
//        yDataSet.setValueTextSize(10f);
//        yDataSet.setDrawCircles(false);
//        yDataSet.setDrawValues(false);
//
//        LineDataSet zDataSet = new LineDataSet(zEntries, "Z-Axis");
//        zDataSet.setColor(Color.BLUE);
//        zDataSet.setValueTextSize(10f);
//        zDataSet.setDrawCircles(false);
//        zDataSet.setDrawValues(false);
//
//        LineData lineData = new LineData(xDataSet, yDataSet, zDataSet);
//        accelChart.setData(lineData);
//
//        accelChart.notifyDataSetChanged();
//        accelChart.moveViewToX(lineData.getEntryCount());
//
//        XAxis xAxis = accelChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//
//        YAxis leftAxis = accelChart.getAxisLeft();
//        leftAxis.setGranularity(1f);
//        accelChart.getAxisRight().setEnabled(false);
//
//        Legend legend = accelChart.getLegend();
//        legend.setTextSize(12f);
//
//        accelChart.invalidate();
//    }
//
//    // Method to update the Accelerometer BarChart
//    private void updateBarGraph() {
//        if (xBarEntries.isEmpty() || yBarEntries.isEmpty() || zBarEntries.isEmpty()) {
//            Log.e("BarGraph", "No data to update accelerometer bar graph!");
//            return;
//        }
//
//        BarDataSet xBarDataSet = new BarDataSet(xBarEntries, "X-Axis");
//        xBarDataSet.setColor(Color.RED);
//
//        BarDataSet yBarDataSet = new BarDataSet(yBarEntries, "Y-Axis");
//        yBarDataSet.setColor(Color.GREEN);
//
//        BarDataSet zBarDataSet = new BarDataSet(zBarEntries, "Z-Axis");
//        zBarDataSet.setColor(Color.BLUE);
//
//        BarData barData = new BarData(xBarDataSet, yBarDataSet, zBarDataSet);
//        accelBarChart.setData(barData);
//        accelBarChart.invalidate();
//    }
//}
package com.example.stress_detection_app;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AccelerometerActivity extends AppCompatActivity {

    private TextView accelData,tooltipTextView;
    private LineChart accelChartX, accelChartY, accelChartZ;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    private ArrayList<Entry> accelEntriesX, accelEntriesY, accelEntriesZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

      //  accelData = findViewById(R.id.accelerometerData);
        accelChartX = findViewById(R.id.accelChartX);
        accelChartY = findViewById(R.id.accelChartY);
        accelChartZ = findViewById(R.id.accelChartZ);
        tooltipTextView=findViewById(R.id.tooltipTextView);

        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("sensorData");

        accelEntriesX = new ArrayList<>();
        accelEntriesY = new ArrayList<>();
        accelEntriesZ = new ArrayList<>();

        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        fetchAccelerometerData(todayDate);
        // Set OnChartValueSelectedListener for each chart
        setChartListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void fetchAccelerometerData(String selectedDate) {
        accelEntriesX.clear();
        accelEntriesY.clear();
        accelEntriesZ.clear();
      //  accelData.setText("Fetching...");

        // Ensure user is logged in
        if (user == null) {
            Log.e("Firebase", "User not logged in!");
          //  accelData.setText("User not logged in!");
            return;
        }

        DatabaseReference dateRef = databaseReference.child(selectedDate);

        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    Log.e("Firebase", "No data found for date: " + selectedDate);
                 //   accelData.setText("No data found for this date!");
                    return;
                }

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        // Get Accelerometer values (X, Y, Z)
                        float accelX = (float) jsonObject.getDouble("accelerometerX");
                        float accelY = (float) jsonObject.getDouble("accelerometerY");
                        float accelZ = (float) jsonObject.getDouble("accelerometerZ");

                        // Update Graph Data
                        float index = accelEntriesX.size();
                        accelEntriesX.add(new Entry(index, accelX));
                        accelEntriesY.add(new Entry(index, accelY));
                        accelEntriesZ.add(new Entry(index, accelZ));

                    } catch (JSONException e) {
                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
                    }
                }

                // Update UI and graph after all data is fetched
                runOnUiThread(() -> {
                 //   accelData.setText("Data fetched for date: " + selectedDate);
                    updateGraph(); // Update graph after fetching accelerometer entries
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
            }
        });
    }

    // Method to update the Accelerometer graph
    private void updateGraph() {
        if (accelEntriesX.isEmpty() || accelEntriesY.isEmpty() || accelEntriesZ.isEmpty()) {
            Log.e("Graph", "No data to update accelerometer graph!");
            return;
        }

        // Line chart for X-axis
        LineDataSet xDataSet = new LineDataSet(accelEntriesX, "X-Axis");
        xDataSet.setColor(Color.RED);
        xDataSet.setValueTextSize(10f);
        xDataSet.setDrawCircles(false);
        xDataSet.setDrawValues(false);

        // Line chart for Y-axis
        LineDataSet yDataSet = new LineDataSet(accelEntriesY, "Y-Axis");
        yDataSet.setColor(Color.GREEN);
        yDataSet.setValueTextSize(10f);
        yDataSet.setDrawCircles(false);
        yDataSet.setDrawValues(false);

        // Line chart for Z-axis
        LineDataSet zDataSet = new LineDataSet(accelEntriesZ, "Z-Axis");
        zDataSet.setColor(Color.BLUE);
        zDataSet.setValueTextSize(10f);
        zDataSet.setDrawCircles(false);
        zDataSet.setDrawValues(false);

        // Create separate data for each chart
        LineData lineDataX = new LineData(xDataSet);
        LineData lineDataY = new LineData(yDataSet);
        LineData lineDataZ = new LineData(zDataSet);

        // Set data to each chart
        accelChartX.setData(lineDataX);
        accelChartY.setData(lineDataY);
        accelChartZ.setData(lineDataZ);

        // Notify each chart that the data has changed
        accelChartX.notifyDataSetChanged();
        accelChartY.notifyDataSetChanged();
        accelChartZ.notifyDataSetChanged();

        // Scroll to the last entry to simulate real-time update (optional)
        accelChartX.moveViewToX(lineDataX.getEntryCount());
        accelChartY.moveViewToX(lineDataY.getEntryCount());
        accelChartZ.moveViewToX(lineDataZ.getEntryCount());

        // Customize charts
        XAxis xAxisX = accelChartX.getXAxis();
        XAxis xAxisY = accelChartY.getXAxis();
        XAxis xAxisZ = accelChartZ.getXAxis();

        xAxisX.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisY.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisZ.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxisX.setGranularity(1f);
        xAxisY.setGranularity(1f);
        xAxisZ.setGranularity(1f);

        YAxis leftAxisX = accelChartX.getAxisLeft();
        YAxis leftAxisY = accelChartY.getAxisLeft();
        YAxis leftAxisZ = accelChartZ.getAxisLeft();

        leftAxisX.setGranularity(1f);
        leftAxisY.setGranularity(1f);
        leftAxisZ.setGranularity(1f);

        accelChartX.getAxisRight().setEnabled(false);
        accelChartY.getAxisRight().setEnabled(false);
        accelChartZ.getAxisRight().setEnabled(false);

        // Customize the legends
        Legend legendX = accelChartX.getLegend();
        Legend legendY = accelChartY.getLegend();
        Legend legendZ = accelChartZ.getLegend();

        legendX.setTextSize(12f);
        legendY.setTextSize(12f);
        legendZ.setTextSize(12f);

        // Refresh the charts
        accelChartX.invalidate();
        accelChartY.invalidate();
        accelChartZ.invalidate();
    }
// Inside AccelerometerActivity.java

    // Method to set chart listeners for detecting value selection
    // Method to set chart listeners for detecting value selection
    private void setChartListeners() {
        accelChartX.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                // Show the accelerometer value of the selected point (X, Y, Z)
                String value = "Accelerometer (X): " + accelEntriesX.get((int) e.getX()).getY() +
                        ", (Y): " + accelEntriesY.get((int) e.getX()).getY() +
                        ", (Z): " + accelEntriesZ.get((int) e.getX()).getY();
                tooltipTextView.setText(value);
                tooltipTextView.setVisibility(TextView.VISIBLE);  // Show the tooltip
            }

            @Override
            public void onNothingSelected() {
                tooltipTextView.setVisibility(TextView.INVISIBLE);  // Hide the tooltip when no point is selected
            }
        });

        accelChartY.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                String value = "Accelerometer (X): " + accelEntriesX.get((int) e.getX()).getY() +
                        ", (Y): " + accelEntriesY.get((int) e.getX()).getY() +
                        ", (Z): " + accelEntriesZ.get((int) e.getX()).getY();
                tooltipTextView.setText(value);
                tooltipTextView.setVisibility(TextView.VISIBLE);  // Show the tooltip
            }

            @Override
            public void onNothingSelected() {
                tooltipTextView.setVisibility(TextView.INVISIBLE);  // Hide the tooltip when no point is selected
            }
        });

        accelChartZ.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                String value = "Accelerometer (X): " + accelEntriesX.get((int) e.getX()).getY() +
                        ", (Y): " + accelEntriesY.get((int) e.getX()).getY() +
                        ", (Z): " + accelEntriesZ.get((int) e.getX()).getY();
                tooltipTextView.setText(value);
                tooltipTextView.setVisibility(TextView.VISIBLE);  // Show the tooltip
            }

            @Override
            public void onNothingSelected() {
                tooltipTextView.setVisibility(TextView.INVISIBLE);  // Hide the tooltip when no point is selected
            }
        });
    }
}
