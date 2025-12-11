package com.example.stress_detection_app;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

import java.util.ArrayList;
import java.util.Locale;

public class gsr extends AppCompatActivity {
    private TextView   gsrValueTextView;
    private LineChart  gsrChart;
    private DatabaseReference databaseReference, hardwareDataRef;
    private FirebaseUser user;
    private static final float GSR_STRESS_THRESHOLD = 2.5f;
    private static final float GSR_CALM_THRESHOLD = 1.0f;
    private static final float GSR_WEIGHT = 0.35f;
    private ArrayList<Entry> gsrEntries = new ArrayList<>();
    private float  gsr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gsr);


    }

    private void initializeFirebase() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            hardwareDataRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("hardwareData");
        }
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


                gsrEntries.clear();

                for (DataSnapshot entrySnapshot : dateSnapshot.getChildren()) {
                    try {
                        String rawJson = entrySnapshot.getValue().toString();
                        JSONObject jsonObject = new JSONObject(rawJson);


                        gsr = (float) jsonObject.optDouble("GSR", 0);

                        float index = gsrEntries.size();

                        gsrEntries.add(new Entry(index, gsr));

                        runOnUiThread(() -> {

                            gsrValueTextView.setText(String.format(Locale.getDefault(), "%.2f μS", gsr));

                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                updateChart(gsrChart, gsrEntries, "GSR", Color.BLUE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
               
            }
        });
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

}