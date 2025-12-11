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
//import com.github.mikephil.charting.components.Legend;
//import com.github.mikephil.charting.components.XAxis;
//import com.github.mikephil.charting.components.YAxis;
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
//public class GSR_Resistance extends AppCompatActivity {
//
//    private TextView gsrDataTextView;
//    private LineChart gsrChart;
//    private DatabaseReference databaseReference;
//    private FirebaseUser user;
//
//    private ArrayList<Entry> gsrEntries;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_gsr_resistance); // You'll need to create this layout file
//
//        gsrDataTextView = findViewById(R.id.gsrData);
//        gsrChart = findViewById(R.id.gsrChart);
//
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        String userId = user.getUid();
//        databaseReference = FirebaseDatabase.getInstance().getReference("users")
//                .child(userId)
//                .child("hardwareData");
//
//        gsrEntries = new ArrayList<>();
//
//        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//        fetchGSRData(todayDate);
//        updateGraph();
//    }
//
//    private void fetchGSRData(String selectedDate) {
//        gsrEntries.clear();
//        gsrDataTextView.setText("Fetching GSR data...");
//
//        if (user == null) {
//            Log.e("Firebase", "User not logged in!");
//            gsrDataTextView.setText("User not logged in!");
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
//                    gsrDataTextView.setText("No GSR data found for this date!");
//                    return;
//                }
//
//                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
//                    try {
//                        String rawJson = entrySnapshot.getValue().toString();
//                        JSONObject jsonObject = new JSONObject(rawJson);
//
//                        // Get GSR value
//                        if (jsonObject.has("GSR")) {
//                            float gsrValue = (float) jsonObject.getDouble("GSR");
//
//                            // Update Graph Data
//                            float index = gsrEntries.size();
//                            gsrEntries.add(new Entry(index, gsrValue));
//                        }
//
//                    } catch (JSONException e) {
//                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
//                    }
//                }
//
//                runOnUiThread(() -> {
//                    gsrDataTextView.setText("GSR data fetched for date: " + selectedDate);
//                    updateGraph();
//                });
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                Log.e("Firebase", "Database Error: " + error.getMessage());
//                gsrDataTextView.setText("Error fetching GSR data!");
//            }
//        });
//    }
//
//    private void updateGraph() {
//        if (gsrEntries.isEmpty()) {
//            Log.e("Graph", "No GSR data to update graph!");
//            return;
//        }
//
//        LineDataSet gsrDataSet = new LineDataSet(gsrEntries, "GSR (μS)");
//        gsrDataSet.setColor(Color.BLUE);
//        gsrDataSet.setValueTextSize(10f);
//        gsrDataSet.setDrawCircles(false);
//        gsrDataSet.setDrawValues(false);
//        gsrDataSet.setLineWidth(2f);
//
//        LineData lineData = new LineData(gsrDataSet);
//        gsrChart.setData(lineData);
//
//        // Customize chart appearance
//        gsrChart.notifyDataSetChanged();
//        gsrChart.moveViewToX(lineData.getEntryCount());
//
//        XAxis xAxis = gsrChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//        xAxis.setLabelRotationAngle(-45);
//
//        YAxis leftAxis = gsrChart.getAxisLeft();
//        leftAxis.setGranularity(0.1f); // GSR values might be small, so smaller granularity
//        leftAxis.setAxisMinimum(0f); // GSR values are typically positive
//        gsrChart.getAxisRight().setEnabled(false);
//
//        Legend legend = gsrChart.getLegend();
//        legend.setTextSize(12f);
//        legend.setForm(Legend.LegendForm.LINE);
//
//        gsrChart.getDescription().setText("GSR Values Over Time");
//        gsrChart.getDescription().setTextSize(12f);
//
//        gsrChart.invalidate();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        // Refresh data if needed
//        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//        fetchGSRData(todayDate);
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

public class GSR_Resistance extends AppCompatActivity {

    private TextView gsrResistanceData, tooltipTextView;
    ImageView backButton;
    private LineChart gsrResistanceChart;
    private DatabaseReference databaseReference;
    private FirebaseUser user;

    private ArrayList<Entry> gsrEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsr_resistance);

        // Initialize views
        gsrResistanceData = findViewById(R.id.gsrResistanceData);
        tooltipTextView = findViewById(R.id.tooltipTextView);
        gsrResistanceChart = findViewById(R.id.gsrResistanceChart);
        // Initialize the back button
        backButton = findViewById(R.id.back);

        // Set an onClickListener to navigate to the main activity when the back button is clicked
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to MainActivity
                Intent intent = new Intent(GSR_Resistance.this, MainActivity.class);
                startActivity(intent);
//                finish();  // Finish the current activity to prevent the user from going back to it
            }
        });


        // Initialize Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("hardwareData");

        gsrEntries = new ArrayList<>();

        // Fetch today's data
        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        fetchGSRResistanceData(todayDate);

        // Set chart listeners for user interaction
        setChartListeners();
    }

    private void fetchGSRResistanceData(String selectedDate) {
        gsrEntries.clear();
        gsrResistanceData.setText("Fetching...");

        // Ensure the user is logged in
        if (user == null) {
            Log.e("Firebase", "User not logged in!");
            gsrResistanceData.setText("User not logged in!");
            return;
        }

        DatabaseReference dateRef = databaseReference.child(selectedDate);

        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    Log.e("Firebase", "No data found for date: " + selectedDate);
                    gsrResistanceData.setText("No data found for this date!");
                    return;
                }

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        // Get GSR Resistance value
                        float gsr = (float) jsonObject.getDouble("GSR");

                        // Update GSR chart data
                        float index = gsrEntries.size();
                        gsrEntries.add(new Entry(index, gsr));

                    } catch (JSONException e) {
                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
                    }
                }

                // Update the UI and graph after all data is fetched
                runOnUiThread(() -> {
                    gsrResistanceData.setText("Data fetched for date: " + selectedDate);
                    updateGraph();  // Update chart after fetching data
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Database Error: " + databaseError.getMessage());
            }
        });
    }

    // Method to update the GSR Resistance graph
    private void updateGraph() {
        if (gsrEntries.isEmpty()) {
            Log.e("Graph", "No data to update GSR Resistance graph!");
            return;
        }

        // Create LineDataSet for GSR
        LineDataSet gsrDataSet = new LineDataSet(gsrEntries, "GSR Resistance");
        gsrDataSet.setColor(Color.RED);
        gsrDataSet.setValueTextSize(10f);
        gsrDataSet.setDrawCircles(false);
        gsrDataSet.setDrawValues(false);

        // Create LineData for chart
        LineData lineData = new LineData(gsrDataSet);
        gsrResistanceChart.setData(lineData);

        // Notify chart that data has changed
        gsrResistanceChart.notifyDataSetChanged();

        // Animate the chart
        gsrResistanceChart.animateX(500);

        // Enable user interaction
        gsrResistanceChart.setDragEnabled(true);
        gsrResistanceChart.setScaleEnabled(true);
        gsrResistanceChart.setPinchZoom(true);

        // Show only the last N points (e.g., 50)
        gsrResistanceChart.setVisibleXRangeMaximum(50);

        // Scroll to the latest entry
        gsrResistanceChart.moveViewToX(lineData.getEntryCount());

        // X-axis styling
        XAxis xAxis = gsrResistanceChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);
        xAxis.setDrawGridLines(false);

        // Y-axis styling
        YAxis leftAxis = gsrResistanceChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        gsrResistanceChart.getAxisRight().setEnabled(false);

        // Legend styling
        Legend legend = gsrResistanceChart.getLegend();
        legend.setTextSize(12f);

        // Refresh the chart
        gsrResistanceChart.invalidate();
    }

    // Method to set chart listeners for detecting value selection
    private void setChartListeners() {
        gsrResistanceChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                String value = "GSR Resistance: " + e.getY();
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
