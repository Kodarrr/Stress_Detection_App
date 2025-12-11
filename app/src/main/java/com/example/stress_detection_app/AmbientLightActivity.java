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

public class AmbientLightActivity extends AppCompatActivity {

    private TextView lightData;
    private LineChart lightChart;
    private DatabaseReference databaseReference;
    FirebaseUser user;
    ImageView backButton;

    private ArrayList<Entry> lightEntries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambient_light);

        lightData = findViewById(R.id.lightData);
        lightChart = findViewById(R.id.lightChart); // Change this ID to a dedicated Gyroscope chart

        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("sensorData");

        lightEntries = new ArrayList<>();

        // Initialize the back button
        backButton = findViewById(R.id.back);

        // Set an onClickListener to navigate to the main activity when the back button is clicked
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to MainActivity
                Intent intent = new Intent(AmbientLightActivity.this, MainActivity.class);
                startActivity(intent);
//                finish();  // Finish the current activity to prevent the user from going back to it
            }
        });

        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        fetchLightData(todayDate);
        updateGraph();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void fetchLightData(String selectedDate) {
        lightEntries.clear();

        lightData.setText("Fetching...");

        // Ensure user is logged in
        if (user == null) {
            Log.e("Firebase", "User not logged in!");
            lightData.setText("User not logged in!");
            return;
        }

        DatabaseReference dateRef = databaseReference.child(selectedDate);

        dateRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                if (!dateSnapshot.exists()) {
                    Log.e("Firebase", "No data found for date: " + selectedDate);
                    lightData.setText("No data found for this date!");
                    return;
                }

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);

                        // Get Light sensor values
                        float light = (float) jsonObject.getDouble("ambientLight");


                        // Update Graph Data
                        float index = lightEntries.size();
                        lightEntries.add(new Entry(index, light));


                    } catch (JSONException e) {
                        Log.e("Firebase", "JSON Parsing Error: " + e.getMessage());
                    }
                }

                // Update UI and graph after all data is fetched
                runOnUiThread(() -> {
                    lightData.setText("Data fetched for date: " + selectedDate);
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
        if (lightEntries.isEmpty()) {
            Log.e("Graph", "No data to update gyroscope graph!");
            return;
        }

        LineDataSet xDataSet = new LineDataSet(lightEntries, "Ambient Light Level (lx)");
        xDataSet.setColor(Color.RED);
        xDataSet.setValueTextSize(10f);
        xDataSet.setDrawCircles(false);
        xDataSet.setDrawValues(false);



        LineData lineData = new LineData(xDataSet);
        lightChart.setData(lineData);

        // Notify the chart that the data has changed
        lightChart.notifyDataSetChanged();

        // Scroll to the last entry to simulate real-time update (optional)
        lightChart.moveViewToX(lineData.getEntryCount());

        // Customize chart
        XAxis xAxis = lightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelRotationAngle(-45);

        YAxis leftAxis = lightChart.getAxisLeft();
        leftAxis.setGranularity(1f);
        lightChart.getAxisRight().setEnabled(false);

        Legend legend = lightChart.getLegend();
        legend.setTextSize(12f);

        lightChart.invalidate();

    }
}