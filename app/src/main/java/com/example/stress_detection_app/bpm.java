//package com.example.stress_detection_app;
//
//import android.graphics.Color;
//import android.os.Bundle;
//import android.widget.TextView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
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
//public class bpm extends AppCompatActivity {
//    private TextView  bpmValueTextView;
//
//    private LineChart bpmChart;
//    private DatabaseReference databaseReference, hardwareDataRef;
//    private FirebaseUser user;
//    private ArrayList<Entry> bpmEntries = new ArrayList<>();
//    private static final float BPM_WEIGHT = 0.35f;
//    private float bpm;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_bpm);
//
//        bpmChart=findViewById(R.id.bpmChart);
//        initializeFirebase();
//        setupChart(bpmChart,"BPM");
//
//        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
//        fetchHardwareData(todayDate);
//
//
//
//    }
//
//    private void setupChart(LineChart chart, String label) {
//        chart.getDescription().setEnabled(false);
//        chart.setTouchEnabled(false);
//        chart.setDragEnabled(false);
//        chart.setScaleEnabled(false);
//        chart.setPinchZoom(false);
//
//        XAxis xAxis = chart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setGranularity(1f);
//
//        YAxis leftAxis = chart.getAxisLeft();
//        leftAxis.setGranularity(1f);
//        chart.getAxisRight().setEnabled(false);
//
//        Legend legend = chart.getLegend();
//        legend.setTextSize(12f);
//    }
//
//
//    private void initializeFirebase() {
//        user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user != null) {
//
//            hardwareDataRef = FirebaseDatabase.getInstance().getReference("users")
//                    .child(user.getUid())
//                    .child("hardwareData");
//        }
//    }
//
//    private void fetchHardwareData(String selectedDate) {
//        if (user == null || hardwareDataRef == null) {
//            return;
//        }
//
//        hardwareDataRef.child(selectedDate).addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
//                if (!dateSnapshot.exists()) {
//                    return;
//                }
//
//                bpmEntries.clear();
//
//
//                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
//                    try {
//                        String rawJson = entrySnapshot.getValue().toString();
//                        JSONObject jsonObject = new JSONObject(rawJson);
//
//                        bpm = (float) jsonObject.optDouble("Avg BPM", 0);
//
//
//                        float index = bpmEntries.size();
//                        bpmEntries.add(new Entry(index, bpm));
//
//
//                        runOnUiThread(() -> {
//                            bpmValueTextView.setText(String.format(Locale.getDefault(), "%.0f BPM", bpm));
//                           // gsrValueTextView.setText(String.format(Locale.getDefault(), "%.2f μS", gsr));
//
//                        });
//
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                updateChart(bpmChart, bpmEntries, "BPM", Color.RED);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//               // stressStatusTextView.setText("Error fetching hardware data");
//            }
//        });
//    }
//
//    private void updateChart(LineChart chart, ArrayList<Entry> entries, String label, int color) {
//        if (entries.isEmpty()) return;
//
//        LineDataSet dataSet = new LineDataSet(entries, label);
//        dataSet.setColor(color);
//        dataSet.setValueTextSize(10f);
//        dataSet.setDrawCircles(false);
//        dataSet.setDrawValues(false);
//
//        LineData lineData = new LineData(dataSet);
//        chart.setData(lineData);
//        chart.invalidate();
//    }
//
//
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

public class bpm extends AppCompatActivity {

    private TextView tooltipTextView;
    private LineChart irChart, bpmChart;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    ImageView backButton;

    private ArrayList<Entry> irEntries, bpmEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bpm);

        // Initialize views
        irChart = findViewById(R.id.irChart);
        bpmChart = findViewById(R.id.bpmChart);
        tooltipTextView = findViewById(R.id.tooltipTextView);

        // Initialize Firebase
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("hardwareData");

        irEntries = new ArrayList<>();
        bpmEntries = new ArrayList<>();

        // Initialize the back button
        backButton = findViewById(R.id.back);

        // Set an onClickListener to navigate to the main activity when the back button is clicked
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to MainActivity
                Intent intent = new Intent(bpm.this, MainActivity.class);
                startActivity(intent);
//                finish();  // Finish the current activity to prevent the user from going back to it
            }
        });

        // Fetch data for the current date
        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        fetchSensorData(todayDate);

        // Set chart listeners for user interaction
        setChartListeners();
    }

    private void fetchSensorData(String selectedDate) {
        irEntries.clear();
        bpmEntries.clear();

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

                        // Get IR and BPM values
                        float irValue = (float) jsonObject.getDouble("irValue");
                        float bpm = (float) jsonObject.getDouble("BPM");

                        // Update chart data
                        irEntries.add(new Entry(irEntries.size(), irValue));
                        bpmEntries.add(new Entry(bpmEntries.size(), bpm));

                    } catch (JSONException e) {
                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
                    }
                }

                // Update the UI and graph after all data is fetched
                runOnUiThread(() -> {
                    updateGraphs();  // Update charts with new data
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("Firebase", "Database Error: " + databaseError.getMessage());
            }
        });
    }

    // Update the IR and BPM graphs
    private void updateGraphs() {
        if (irEntries.isEmpty() || bpmEntries.isEmpty()) {
            Log.e("Graph", "No data to update graphs!");
            return;
        }

        // IR Chart
        LineDataSet irDataSet = new LineDataSet(irEntries, "IR Value");
        irDataSet.setColor(Color.RED);
        irDataSet.setValueTextSize(10f);
        irDataSet.setDrawCircles(false);
        irDataSet.setDrawValues(false);

        // BPM Chart
        LineDataSet bpmDataSet = new LineDataSet(bpmEntries, "BPM");
        bpmDataSet.setColor(Color.BLUE);
        bpmDataSet.setValueTextSize(10f);
        bpmDataSet.setDrawCircles(false);
        bpmDataSet.setDrawValues(false);

        // Create LineData for both charts
        LineData irLineData = new LineData(irDataSet);
        LineData bpmLineData = new LineData(bpmDataSet);

        // Set data to charts
        irChart.setData(irLineData);
        bpmChart.setData(bpmLineData);

        // Notify charts that data has changed
        irChart.notifyDataSetChanged();
        bpmChart.notifyDataSetChanged();

        // Refresh charts
        irChart.invalidate();
        bpmChart.invalidate();
    }

    // Method to set chart listeners for detecting value selection
    private void setChartListeners() {
        irChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                String value = "IR Value: " + e.getY();
                tooltipTextView.setText(value);
                tooltipTextView.setVisibility(TextView.VISIBLE);  // Show the tooltip
            }

            @Override
            public void onNothingSelected() {
                tooltipTextView.setVisibility(TextView.INVISIBLE);  // Hide the tooltip when no point is selected
            }
        });

        bpmChart.setOnChartValueSelectedListener(new com.github.mikephil.charting.listener.OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, com.github.mikephil.charting.highlight.Highlight h) {
                String value = "BPM: " + e.getY();
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
