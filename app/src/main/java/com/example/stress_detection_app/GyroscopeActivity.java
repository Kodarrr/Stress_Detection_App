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
//import com.github.mikephil.charting.charts.BarChart;
//import com.github.mikephil.charting.charts.LineChart;
//import com.github.mikephil.charting.components.Legend;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.components.YAxis;
//import com.github.mikephil.charting.data.BarData;
//import com.github.mikephil.charting.data.BarDataSet;
//import com.github.mikephil.charting.data.BarEntry;
//import com.github.mikephil.charting.data.Entry;
//import com.github.mikephil.charting.data.LineData;
//import com.github.mikephil.charting.data.LineDataSet;
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
//public class GyroscopeActivity extends AppCompatActivity {
//
//    private TextView gyroscopeData;
//    private LineChart gyroscopeChart;
//    private BarChart gyroscopeBarChart;
//
//    private DatabaseReference databaseReference;
//    FirebaseUser user;
//
//    private ArrayList<Entry> xEntries, yEntries, zEntries;
//    private ArrayList<BarEntry> xBarEntries, yBarEntries, zBarEntries;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_gyroscope);
//
//        gyroscopeData = findViewById(R.id.gyroscopeData);
//        gyroscopeChart = findViewById(R.id.gyroscopeChart); // Change this ID to a dedicated Gyroscope chart
//        gyroscopeBarChart = findViewById(R.id.gyroscopeBarChart); // Add BarChart initialization
//
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
//        fetchGyroscopeData(todayDate);
//        updateGraph();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//        fetchGyroscopeData(todayDate); // Re-fetch the data when the activity is resumed
//        updateGraph();  // Update graph if necessary
//        updateBarGraph(); // Optionally update BarGraph
//
//    }
//
//    private void fetchGyroscopeData(String selectedDate) {
//        xEntries.clear();
//        yEntries.clear();
//        zEntries.clear();
//        gyroscopeData.setText("Fetching...");
//
//        // Ensure user is logged in
//        if (user == null) {
//            Log.e("Firebase", "User not logged in!");
//            gyroscopeData.setText("User not logged in!");
//            return;
//        }
//
//        DatabaseReference dateRef = databaseReference.child(selectedDate);
//
//        dateRef.addValueEventListener(new ValueEventListener(){
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
//                if (!dateSnapshot.exists()) {
//                    Log.e("Firebase", "No data found for date: " + selectedDate);
//                    gyroscopeData.setText("No data found for this date!");
//                    return;
//                }
//
//                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
//                    try {
//                        String rawJson = entrySnapshot.getValue().toString();
//                        JSONObject jsonObject = new JSONObject(rawJson);
//
//                        // Get Gyroscope values
//                        float gx = (float) jsonObject.getDouble("gyroscopeX");
//                        float gy = (float) jsonObject.getDouble("gyroscopeY");
//                        float gz = (float) jsonObject.getDouble("gyroscopeZ");
//
//                        // Update Graph Data
//                        float index = xEntries.size();
//                        xEntries.add(new Entry(index, gx));
//                        yEntries.add(new Entry(index, gy));
//                        zEntries.add(new Entry(index, gz));
//
//                    } catch (JSONException e) {
//                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
//                    }
//                }
//
//                // Update UI and graph after all data is fetched
//                runOnUiThread(() -> {
//                    gyroscopeData.setText("Data fetched for date: " + selectedDate);
//                    updateGraph(); // Update graph after fetching gyroscope entries
//                    updateBarGraph();
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
//    // Method to update the Gyroscope graph
//    private void updateGraph() {
//        if (xEntries.isEmpty() || yEntries.isEmpty() || zEntries.isEmpty()) {
//            Log.e("Graph", "No data to update gyroscope graph!");
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
//        gyroscopeChart.setData(lineData);
//
//        gyroscopeChart.notifyDataSetChanged();
//
//        // Animate horizontally
//        gyroscopeChart.animateX(500);
//
//        // Enable user interaction
//        gyroscopeChart.setDragEnabled(true);
//        gyroscopeChart.setScaleEnabled(true);
//        gyroscopeChart.setPinchZoom(true);
//
//        // Show only last N points (e.g., 50)
//        gyroscopeChart.setVisibleXRangeMaximum(50);
//
//        // Scroll to the latest entry
//        gyroscopeChart.moveViewToX(lineData.getEntryCount());
//
//        // X-axis styling
//        XAxis xAxis = gyroscopeChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//        xAxis.setLabelRotationAngle(-45);
//        xAxis.setDrawGridLines(false);
//
//        // Y-axis styling
//        YAxis leftAxis = gyroscopeChart.getAxisLeft();
//        leftAxis.setGranularity(1f);
//        gyroscopeChart.getAxisRight().setEnabled(false);
//
//        // Legend styling
//        Legend legend = gyroscopeChart.getLegend();
//        legend.setTextSize(12f);
//
//        gyroscopeChart.invalidate();
//    }
//
//
//    // Method to update the Gyroscope BarChart
//    private void updateBarGraph() {
//        if (xBarEntries.isEmpty() || yBarEntries.isEmpty() || zBarEntries.isEmpty()) {
//            Log.e("BarGraph", "No data to update gyroscope bar graph!");
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
//        gyroscopeBarChart.setData(barData);
//        gyroscopeBarChart.invalidate();
//    }
//}

package com.example.stress_detection_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

public class GyroscopeActivity extends AppCompatActivity {

    private TextView gyroData, tooltipTextView;
    private LineChart gyroChartX, gyroChartY, gyroChartZ;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    ImageView backButton;

    private ArrayList<Entry> gyroEntriesX, gyroEntriesY, gyroEntriesZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        // Initialize views
        gyroChartX = findViewById(R.id.gyroChartX);
        gyroChartY = findViewById(R.id.gyroChartY);
        gyroChartZ = findViewById(R.id.gyroChartZ);
        tooltipTextView = findViewById(R.id.tooltipTextView);



        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("sensorData");

        gyroEntriesX = new ArrayList<>();
        gyroEntriesY = new ArrayList<>();
        gyroEntriesZ = new ArrayList<>();

        // Initialize the back button
        backButton = findViewById(R.id.back);

        // Set an onClickListener to navigate to the main activity when the back button is clicked
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to MainActivity
                Intent intent = new Intent(GyroscopeActivity.this, MainActivity.class);
                startActivity(intent);
//                finish();  // Finish the current activity to prevent the user from going back to it
            }
        });

        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        fetchGyroscopeData(todayDate);
        // Set OnChartValueSelectedListener for each chart
        setChartListeners();
    }

    private void fetchGyroscopeData(String selectedDate) {
        gyroEntriesX.clear();
        gyroEntriesY.clear();
        gyroEntriesZ.clear();

        // Ensure user is logged in
        if (user == null) {
            Log.e("Firebase", "User not logged in!");
            return;
        }

        DatabaseReference dateRef = databaseReference.child(selectedDate);

        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    Log.e("Firebase", "No data found for date: " + selectedDate);
                    return;
                }

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        // Get Gyroscope values (X, Y, Z)
                        float gyroX = (float) jsonObject.getDouble("gyroscopeX");
                        float gyroY = (float) jsonObject.getDouble("gyroscopeY");
                        float gyroZ = (float) jsonObject.getDouble("gyroscopeZ");

                        // Update Graph Data
                        float index = gyroEntriesX.size();
                        gyroEntriesX.add(new Entry(index, gyroX));
                        gyroEntriesY.add(new Entry(index, gyroY));
                        gyroEntriesZ.add(new Entry(index, gyroZ));

                    } catch (JSONException e) {
                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
                    }
                }

                // Update UI and graph after all data is fetched
                runOnUiThread(() -> {
                    updateGraph(); // Update graph after fetching gyroscope entries
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
            }
        });
    }

    // Method to update the Gyroscope graph
    private void updateGraph() {
        if (gyroEntriesX.isEmpty() || gyroEntriesY.isEmpty() || gyroEntriesZ.isEmpty()) {
            Log.e("Graph", "No data to update gyroscope graph!");
            return;
        }

        // Line chart for X-axis
        LineDataSet xDataSet = new LineDataSet(gyroEntriesX, "X-Axis");
        xDataSet.setColor(Color.RED);
        xDataSet.setValueTextSize(10f);
        xDataSet.setDrawCircles(false);
        xDataSet.setDrawValues(false);

        // Line chart for Y-axis
        LineDataSet yDataSet = new LineDataSet(gyroEntriesY, "Y-Axis");
        yDataSet.setColor(Color.GREEN);
        yDataSet.setValueTextSize(10f);
        yDataSet.setDrawCircles(false);
        yDataSet.setDrawValues(false);

        // Line chart for Z-axis
        LineDataSet zDataSet = new LineDataSet(gyroEntriesZ, "Z-Axis");
        zDataSet.setColor(Color.BLUE);
        zDataSet.setValueTextSize(10f);
        zDataSet.setDrawCircles(false);
        zDataSet.setDrawValues(false);

        // Create separate data for each chart
        LineData lineDataX = new LineData(xDataSet);
        LineData lineDataY = new LineData(yDataSet);
        LineData lineDataZ = new LineData(zDataSet);

        // Set data to each chart
        gyroChartX.setData(lineDataX);
        gyroChartY.setData(lineDataY);
        gyroChartZ.setData(lineDataZ);

        // Notify each chart that the data has changed
        gyroChartX.notifyDataSetChanged();
        gyroChartY.notifyDataSetChanged();
        gyroChartZ.notifyDataSetChanged();

        // Scroll to the last entry to simulate real-time update (optional)
        gyroChartX.moveViewToX(lineDataX.getEntryCount());
        gyroChartY.moveViewToX(lineDataY.getEntryCount());
        gyroChartZ.moveViewToX(lineDataZ.getEntryCount());

        // Customize charts
        XAxis xAxisX = gyroChartX.getXAxis();
        XAxis xAxisY = gyroChartY.getXAxis();
        XAxis xAxisZ = gyroChartZ.getXAxis();

        xAxisX.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisY.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxisZ.setPosition(XAxis.XAxisPosition.BOTTOM);

        xAxisX.setGranularity(1f);
        xAxisY.setGranularity(1f);
        xAxisZ.setGranularity(1f);

        YAxis leftAxisX = gyroChartX.getAxisLeft();
        YAxis leftAxisY = gyroChartY.getAxisLeft();
        YAxis leftAxisZ = gyroChartZ.getAxisLeft();

        leftAxisX.setGranularity(1f);
        leftAxisY.setGranularity(1f);
        leftAxisZ.setGranularity(1f);

        gyroChartX.getAxisRight().setEnabled(false);
        gyroChartY.getAxisRight().setEnabled(false);
        gyroChartZ.getAxisRight().setEnabled(false);

        // Customize the legends
        Legend legendX = gyroChartX.getLegend();
        Legend legendY = gyroChartY.getLegend();
        Legend legendZ = gyroChartZ.getLegend();

        legendX.setTextSize(12f);
        legendY.setTextSize(12f);
        legendZ.setTextSize(12f);

        // Refresh the charts
        gyroChartX.invalidate();
        gyroChartY.invalidate();
        gyroChartZ.invalidate();
    }

    // Method to set chart listeners for detecting value selection
    private void setChartListeners() {
        gyroChartX.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                String value = "Gyroscope (X): " + gyroEntriesX.get((int) e.getX()).getY() +
                        ", (Y): " + gyroEntriesY.get((int) e.getX()).getY() +
                        ", (Z): " + gyroEntriesZ.get((int) e.getX()).getY();
                tooltipTextView.setText(value);
                tooltipTextView.setVisibility(TextView.VISIBLE);  // Show the tooltip
            }

            @Override
            public void onNothingSelected() {
                tooltipTextView.setVisibility(TextView.INVISIBLE);  // Hide the tooltip when no point is selected
            }
        });

        gyroChartY.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                String value = "Gyroscope (X): " + gyroEntriesX.get((int) e.getX()).getY() +
                        ", (Y): " + gyroEntriesY.get((int) e.getX()).getY() +
                        ", (Z): " + gyroEntriesZ.get((int) e.getX()).getY();
                tooltipTextView.setText(value);
                tooltipTextView.setVisibility(TextView.VISIBLE);  // Show the tooltip
            }

            @Override
            public void onNothingSelected() {
                tooltipTextView.setVisibility(TextView.INVISIBLE);  // Hide the tooltip when no point is selected
            }
        });

        gyroChartZ.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                String value = "Gyroscope (X): " + gyroEntriesX.get((int) e.getX()).getY() +
                        ", (Y): " + gyroEntriesY.get((int) e.getX()).getY() +
                        ", (Z): " + gyroEntriesZ.get((int) e.getX()).getY();
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
