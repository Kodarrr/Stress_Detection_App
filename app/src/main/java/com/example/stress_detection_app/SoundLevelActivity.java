package com.example.stress_detection_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.stress_detection_app.Services.SensorBackgroundService;
import com.example.stress_detection_app.Services.UnifiedService;
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
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
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
import java.util.Queue;

public class SoundLevelActivity extends AppCompatActivity {

    private TextView soundLevelData;
    private LineChart soundLevelChart;
    private BarChart noiseBarChart;

    private DatabaseReference databaseReference;
    Queue<String> queue;

    private FirebaseUser user;
    private ArrayList<Entry> soundLevelEntries;
    private ArrayList<BarEntry> noiseBarEntries;
    ImageView backButton;
    private ArrayList<String> dateLabels; // Stores last 7 days for X-Axis

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_level);

        soundLevelData = findViewById(R.id.soundLevelData);
        soundLevelChart = findViewById(R.id.soundLevelChart);
        noiseBarChart =   findViewById(R.id.noiseBarChart);


        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("sensorData");

        soundLevelEntries = new ArrayList<>();
        queue= UnifiedService.keyQueue;
        // Initialize the back button
        backButton = findViewById(R.id.back);

        // Set an onClickListener to navigate to the main activity when the back button is clicked
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to MainActivity
                Intent intent = new Intent(SoundLevelActivity.this, MainActivity.class);
                startActivity(intent);
//                finish();  // Finish the current activity to prevent the user from going back to it
            }
        });

        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()); // Example: "20240217"
        fetchPastWeekNoiseData();

        fetchNoiseLevelData(todayDate);
        updateGraph();
    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    private void fetchNoiseLevelData(String selectedDate) {
        soundLevelEntries.clear();
        soundLevelData.setText("Fetching...");

        if (user == null) {
            Log.e("Firebase", "User not logged in!");
            soundLevelData.setText("User not logged in!");
            return;
        }

        DatabaseReference dateRef = databaseReference.child(selectedDate);

        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    Log.e("Firebase", "No data found for date: " + selectedDate);
                    soundLevelData.setText("No data found for this date!");
                    return;
                }

                // Clear the previous data
                soundLevelEntries.clear();

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        // ✅ Get Noise Level value
                        String rawNoiseLevel = jsonObject.getString("noiseLevel");  // e.g., "12.55 dB"
                        float noiseLevel = 0.0f;
                        try {
                            // Remove " dB" from the string
                            String cleanNoiseLevel = rawNoiseLevel.replace(" dB", "").trim();

                            // Convert the remaining string to a float
                            noiseLevel = Float.parseFloat(cleanNoiseLevel);
                        } catch (NumberFormatException e) {
                            Log.e("Firebase", "Error parsing noise level: " + e.getMessage());
                            noiseLevel = 0.0f;  // Default value if parsing fails
                        }

                        // ✅ Add noise level to entries for the chart
                        int index = soundLevelEntries.size();
                        soundLevelEntries.add(new Entry(index, noiseLevel));

                    } catch (Exception e) {
                        Log.e("Firebase", "Error parsing data: " + e.getMessage());
                    }
                }

                runOnUiThread(() -> {
                    soundLevelData.setText("Data fetched for date: " + selectedDate);
                    updateGraph(); // 🔥 Update graph after fetching all entries
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
            }
        });
    }

    /**
     * Fetches the last 7 days' average noise level for the Bar Chart.
     */
    private void fetchPastWeekNoiseData() {
        DatabaseReference noiseDatabase = FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid())
                .child("sensorData");

        noiseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noiseBarEntries.clear();
                dateLabels.clear();

                int index = 0; // Bar chart X-axis index

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey(); // Get date (e.g., "20250224")

                    float sumNoise = 0;
                    int count = 0;

                    // Iterate through all noise records for the day
                    for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                        try {
                            String rawJson = entrySnapshot.getValue().toString();
                            JSONObject jsonObject = new JSONObject(rawJson);

                            // ✅ Extract Noise Level
                            String rawNoiseLevel = jsonObject.getString("noiseLevel");
                            float noiseLevel = Float.parseFloat(rawNoiseLevel.replace(" dB", "").trim());

                            sumNoise += noiseLevel; // Sum noise levels
                            count++;

                        } catch (JSONException | NumberFormatException e) {
                            Log.e("NoiseChart", "Error parsing noise level: " + e.getMessage());
                        }
                    }

                    // ✅ Compute Average Noise Level for the Day
                    if (count > 0) {
                        float avgNoise = sumNoise / count;
                        noiseBarEntries.add(new BarEntry(index, avgNoise));
                        dateLabels.add(formatDate(date)); // Format "yyyyMMdd" to "Feb 24"

                        index++; // Increment chart index
                    }
                }

                // 🔥 If no data is available, avoid crashes
                if (noiseBarEntries.isEmpty()) {
                    Log.e("NoiseChart", "No past noise level data found.");
                    return;
                }

                Log.d("NoiseChart", "Entries Count: " + noiseBarEntries.size());
                setupBarChart();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("NoiseChart", "Database Error: " + error.getMessage());
            }
        });
    }


    private void updateGraph() {
        if (soundLevelEntries.isEmpty()) {
            Log.e("Graph", "No data to update graph!");
            return;
        }

        // Create a dataset for sound level
        LineDataSet soundLevelDataSet = new LineDataSet(soundLevelEntries, "Noise Level (dB)");
        soundLevelDataSet.setColor(Color.RED);
        soundLevelDataSet.setValueTextSize(10f);
        soundLevelDataSet.setDrawCircles(false);
        soundLevelDataSet.setDrawValues(false);

        // Create line data object
        LineData lineData = new LineData(soundLevelDataSet);
        soundLevelChart.setData(lineData);

        // Notify the chart that the data has changed
        soundLevelChart.notifyDataSetChanged();

        // Scroll to the last entry to simulate real-time update (optional)
        soundLevelChart.moveViewToX(lineData.getEntryCount());

//        // Customize chart
//        soundLevelChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
//        soundLevelChart.getAxisLeft().setGranularity(1f);
//        soundLevelChart.getAxisRight().setEnabled(false);

        XAxis xAxis = soundLevelChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = soundLevelChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        soundLevelChart.getAxisRight().setEnabled(false);

        // Customize legend
        Legend legend = soundLevelChart.getLegend();
        legend.setTextSize(12f);
        // Refresh the chart
        soundLevelChart.invalidate();
    }

    /**
     * Updates the past 7 days' average noise level Bar Chart.
     */
    private void setupBarChart() {
        BarDataSet dataSet = new BarDataSet(noiseBarEntries, "Avg Noise Level (dB)");
        dataSet.setColor(Color.GREEN);
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        noiseBarChart.setData(barData);
        noiseBarChart.setFitBars(true);
        noiseBarChart.getDescription().setEnabled(false);

        XAxis xAxis = noiseBarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(dateLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = noiseBarChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        noiseBarChart.getAxisRight().setEnabled(false);

        noiseBarChart.invalidate();
    }

    private String formatDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            Date d = sdf.parse(date);
            return new SimpleDateFormat("MMM dd", Locale.getDefault()).format(d);
        } catch (Exception e) {
            return date;
        }
    }
}
