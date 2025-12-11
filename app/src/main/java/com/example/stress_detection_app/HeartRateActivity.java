package com.example.stress_detection_app;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class HeartRateActivity extends AppCompatActivity {

    private TextView bpmDataTextView;
    private LineChart bpmChart;
    private BarChart bpmBarChart;

    private DatabaseReference databaseReference;
    private FirebaseUser user;

    private ArrayList<Entry> bpmEntries;
    private ArrayList<BarEntry> barEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heart_rate);

        bpmDataTextView = findViewById(R.id.bpmData);
        bpmChart = findViewById(R.id.bpmChart);
        bpmBarChart = findViewById(R.id.bpmBarChart);


        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("hardwareData");

        bpmEntries = new ArrayList<>();
        barEntries = new ArrayList<>();


        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        fetchBPMData(todayDate);
        updateGraph();

        // Fetch and display the last 7 days average data
        fetchAndDisplayLast7DaysAverage();
    }

    private void fetchBPMData(String selectedDate) {
        bpmEntries.clear();
        bpmDataTextView.setText("Fetching heart rate data...");

        if (user == null) {
            Log.e("Firebase", "User not logged in!");
            bpmDataTextView.setText("User not logged in!");
            return;
        }

        DatabaseReference dateRef = databaseReference.child(selectedDate);

        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    Log.e("Firebase", "No data found for date: " + selectedDate);
                    bpmDataTextView.setText("No heart rate data found for this date!");
                    return;
                }

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        // Get BPM value - using different possible field names
                        if (jsonObject.has("Avg BPM")) {
                            float bpmValue = (float) jsonObject.optDouble("Avg BPM",
                                    jsonObject.optDouble("heartRate", 0));

                            // Only add valid heart rate values (typical range 40-200 bpm)
                            if (bpmValue > 40 && bpmValue < 200) {
                                float index = bpmEntries.size();
                                bpmEntries.add(new Entry(index, bpmValue));
                            }
                        }

                    } catch (JSONException e) {
                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
                    }
                }

                runOnUiThread(() -> {
                    bpmDataTextView.setText("Heart rate data fetched for date: " + selectedDate);
                    updateGraph();
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
                bpmDataTextView.setText("Error fetching heart rate data!");
            }
        });
    }

    private void updateGraph() {
        if (bpmEntries.isEmpty()) {
            Log.e("Graph", "No BPM data to update graph!");
            return;
        }

        LineDataSet bpmDataSet = new LineDataSet(bpmEntries, "Heart Rate (BPM)");
        bpmDataSet.setColor(Color.GREEN);
        bpmDataSet.setValueTextSize(10f);
        bpmDataSet.setDrawCircles(false);
        bpmDataSet.setDrawValues(false);
        bpmDataSet.setLineWidth(2f);

        LineData lineData = new LineData(bpmDataSet);
        bpmChart.setData(lineData);

        // Customize chart appearance
        bpmChart.notifyDataSetChanged();
        bpmChart.moveViewToX(lineData.getEntryCount());

        XAxis xAxis = bpmChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = bpmChart.getAxisLeft();
        leftAxis.setGranularity(5f); // 5 BPM intervals
        leftAxis.setAxisMinimum(40f); // Minimum reasonable heart rate
        leftAxis.setAxisMaximum(200f); // Maximum reasonable heart rate
        bpmChart.getAxisRight().setEnabled(false);

        Legend legend = bpmChart.getLegend();
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.LINE);

        bpmChart.getDescription().setText("Heart Rate Over Time");
        bpmChart.getDescription().setTextSize(12f);

        bpmChart.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity resumes
        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        fetchBPMData(todayDate);
        updateGraph();  // Update graph if necessary
        updateBarChart(); // Optionally update BarGraph
    }

    private void fetchAndDisplayLast7DaysAverage() {
        // Get the current date and calculate the last 7 days dynamically
        Calendar calendar = Calendar.getInstance();
        for (int i = 6; i >= 0; i--) {
            calendar.add(Calendar.DAY_OF_MONTH, -1); // Move back by one day
            String previousDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(calendar.getTime());

            // Fetch and calculate the average BPM for each day
            calculateDailyAverage(previousDate, i); // i will track the day index for the bar chart
        }
    }

    private void calculateDailyAverage(String date, final int dayIndex) {
        DatabaseReference dateRef = databaseReference.child(date);

        dateRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                float totalBPM = 0;
                int count = 0;

                // Iterate through the data for the given date
                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);
                        float bpmValue = (float) jsonObject.optDouble("BPM", 0);

                        // Only consider valid BPM values (within range)
                        if (bpmValue > 40 && bpmValue < 200) {
                            totalBPM += bpmValue;
                            count++;
                        }
                    } catch (JSONException e) {
                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
                    }
                }

                if (count > 0) {
                    float dailyAverage = totalBPM / count;
                    barEntries.add(new BarEntry(dayIndex, dailyAverage)); // Add the daily average BPM for this day

                    // Update the Bar Chart once all 7 days are processed
                    if (barEntries.size() == 7) {
                        runOnUiThread(() -> updateBarChart());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
            }
        });
    }


    private void updateBarChart() {
        if (barEntries.isEmpty()) {
            Log.e("BarChart", "No data to update bar chart!");
            return;
        }

        // Create a BarDataSet with the entries (7-day averages)
        BarDataSet barDataSet = new BarDataSet(barEntries, "7-Day Average BPM");
        barDataSet.setColor(Color.parseColor("#2F7D32")); // Custom green color

        // Set the BarData to the BarChart
        BarData barData = new BarData(barDataSet);
        bpmBarChart.setData(barData);

        // Customize chart appearance
        bpmBarChart.getDescription().setText("Average BPM Over the Last 7 Days");
        bpmBarChart.getDescription().setTextSize(12f);
        bpmBarChart.invalidate();  // Refresh the Bar Chart
    }




}