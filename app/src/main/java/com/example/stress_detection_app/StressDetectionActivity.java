package com.example.stress_detection_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

public class StressDetectionActivity extends AppCompatActivity {

    private TextView stressStatusTextView, bpmValueTextView, gsrValueTextView, accelValueTextView, gyroValueTextView;
    private LineChart bpmChart, gsrChart, accelChart, gyroChart;
    private DatabaseReference databaseReference, hardwareDataRef;
    private FirebaseUser user;

    // Threshold values
    private static final int BPM_STRESS_THRESHOLD = 85;
    private static final int BPM_CALM_THRESHOLD = 65;
    private static final float GSR_STRESS_THRESHOLD = 2.5f;
    private static final float GSR_CALM_THRESHOLD = 1.0f;
    private static final float ACCEL_STRESS_THRESHOLD = 3.0f;
    private static final float ACCEL_CALM_THRESHOLD = 0.5f;
    private static final float GYRO_STRESS_THRESHOLD = 2.0f;
    private static final float GYRO_CALM_THRESHOLD = 0.3f;

    // Weights
    private static final float BPM_WEIGHT = 0.35f;
    private static final float GSR_WEIGHT = 0.35f;
    private static final float ACCEL_WEIGHT = 0.15f;
    private static final float GYRO_WEIGHT = 0.15f;

    private ArrayList<Entry> bpmEntries = new ArrayList<>();
    private ArrayList<Entry> gsrEntries = new ArrayList<>();
    private ArrayList<Entry> accelEntries = new ArrayList<>();
    private ArrayList<Entry> gyroEntries = new ArrayList<>();

    private float bpm, gsr, accelMagnitude, gyroMagnitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stress_detection);

        initializeViews();
        initializeFirebase();
        setupCharts();

        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        fetchSensorData(todayDate);
        fetchHardwareData(todayDate);
    }

    private void initializeViews() {
        stressStatusTextView = findViewById(R.id.stressStatusTextView);
        bpmValueTextView = findViewById(R.id.bpmValueTextView);
        gsrValueTextView = findViewById(R.id.gsrValueTextView);
        accelValueTextView = findViewById(R.id.accelValueTextView);
        gyroValueTextView = findViewById(R.id.gyroValueTextView);

        bpmChart = findViewById(R.id.bpmChart);
        gsrChart = findViewById(R.id.gsrChart);
        accelChart = findViewById(R.id.accelChart);
        gyroChart = findViewById(R.id.gyroChart);
    }

    private void initializeFirebase() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("sensorData");
            hardwareDataRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("hardwareData");
        }
    }

    private void setupCharts() {
        setupChart(bpmChart, "BPM");
        setupChart(gsrChart, "GSR (μS)");
        setupChart(accelChart, "Accel (m/s²)");
        setupChart(gyroChart, "Gyro (rad/s)");
    }

    private void setupChart(LineChart chart, String label) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setGranularity(1f);
        chart.getAxisRight().setEnabled(false);

        Legend legend = chart.getLegend();
        legend.setTextSize(12f);
    }

    private void fetchSensorData(String selectedDate) {
        if (user == null || databaseReference == null) {
            stressStatusTextView.setText("User not logged in!");
            return;
        }

        databaseReference.child(selectedDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    return;
                }

                accelEntries.clear();
                gyroEntries.clear();

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        float accelX = (float) jsonObject.optDouble("accelerometerX", 0);
                        float accelY = (float) jsonObject.optDouble("accelerometerY", 0);
                        float accelZ = (float) jsonObject.optDouble("accelerometerZ", 0);
                        accelMagnitude = (float) Math.sqrt(accelX*accelX + accelY*accelY + accelZ*accelZ);

                        float gyroX = (float) jsonObject.optDouble("gyroscopeX", 0);
                        float gyroY = (float) jsonObject.optDouble("gyroscopeY", 0);
                        float gyroZ = (float) jsonObject.optDouble("gyroscopeZ", 0);
                        gyroMagnitude = (float) Math.sqrt(gyroX*gyroX + gyroY*gyroY + gyroZ*gyroZ);

                        float index = accelEntries.size();
                        accelEntries.add(new Entry(index, accelMagnitude));
                        gyroEntries.add(new Entry(index, gyroMagnitude));

                        runOnUiThread(() -> {
                            accelValueTextView.setText(String.format(Locale.getDefault(), "%.2f m/s²", accelMagnitude));
                            gyroValueTextView.setText(String.format(Locale.getDefault(), "%.2f rad/s", gyroMagnitude));
                            updateStressLevel();
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                updateChart(accelChart, accelEntries, "Accel", Color.GREEN);
                updateChart(gyroChart, gyroEntries, "Gyro", Color.MAGENTA);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stressStatusTextView.setText("Error fetching sensor data");
            }
        });
    }

    private void fetchHardwareData(String selectedDate) {
        if (user == null || hardwareDataRef == null) {
            return;
        }

        hardwareDataRef.child(selectedDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    return;
                }

                bpmEntries.clear();
                gsrEntries.clear();

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        bpm = (float) jsonObject.optDouble("Avg BPM", 0);
                        gsr = (float) jsonObject.optDouble("GSR", 0);

                        float index = bpmEntries.size();
                        bpmEntries.add(new Entry(index, bpm));
                        gsrEntries.add(new Entry(index, gsr));

                        runOnUiThread(() -> {
                            bpmValueTextView.setText(String.format(Locale.getDefault(), "%.0f BPM", bpm));
                            gsrValueTextView.setText(String.format(Locale.getDefault(), "%.2f μS", gsr));
                            updateStressLevel();
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                updateChart(bpmChart, bpmEntries, "BPM", Color.RED);
                updateChart(gsrChart, gsrEntries, "GSR", Color.BLUE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stressStatusTextView.setText("Error fetching hardware data");
            }
        });
    }

    private void updateStressLevel() {
        int stressScore=determineStressLevel(bpm, gsr, accelMagnitude, gyroMagnitude);
        // Prepare data for MainActivity
        Intent intent = new Intent(StressDetectionActivity.this, MainActivity.class);
        intent.putExtra("stressLevel", stressStatusTextView.getText().toString());
        intent.putExtra("stressProgress", stressScore);  // Custom method to get progress value

        // Start MainActivity with the intent
        startActivity(intent);
    }



    private void updateChart(LineChart chart, ArrayList<Entry> entries, String label, int color) {
        if (entries.isEmpty()) return;

        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.invalidate();
    }

    private int determineStressLevel(float bpm, float gsr, float accelMagnitude, float gyroMagnitude) {
        float bpmContribution = normalizeStressContribution(bpm, BPM_CALM_THRESHOLD, BPM_STRESS_THRESHOLD, true);
        float gsrContribution = normalizeStressContribution(gsr, GSR_CALM_THRESHOLD, GSR_STRESS_THRESHOLD, true);
        float accelContribution = normalizeStressContribution(accelMagnitude, ACCEL_CALM_THRESHOLD, ACCEL_STRESS_THRESHOLD, true);
        float gyroContribution = normalizeStressContribution(gyroMagnitude, GYRO_CALM_THRESHOLD, GYRO_STRESS_THRESHOLD, true);

        float stressScore = (bpmContribution * BPM_WEIGHT) +
                (gsrContribution * GSR_WEIGHT) +
                (accelContribution * ACCEL_WEIGHT) +
                (gyroContribution * GYRO_WEIGHT);

        String status;
        int color;
        if (stressScore > 0.7) {
            status = "STRESSED";
            color = Color.RED;

        } else if (stressScore > 0.4) {
            status = "NORMAL";
            color = Color.YELLOW;
        } else {
            status = "CALM";
            color = Color.GREEN;

        }



        stressStatusTextView.setText(status);
        stressStatusTextView.setTextColor(color);

        if (stressScore > 0.7) {
            return 100;  // Stressed
        } else if (stressScore > 0.4) {
            return 50;   // Normal
        } else {
            return 0;    // Calm
        }
    }


    private float normalizeStressContribution(float value, float calmThreshold, float stressThreshold, boolean higherIsStressed) {
        if (higherIsStressed) {
            if (value >= stressThreshold) return 1.0f;
            if (value <= calmThreshold) return 0.0f;
            return (value - calmThreshold) / (stressThreshold - calmThreshold);
        } else {
            if (value <= stressThreshold) return 1.0f;
            if (value >= calmThreshold) return 0.0f;
            return (calmThreshold - value) / (calmThreshold - stressThreshold);
        }
    }
}