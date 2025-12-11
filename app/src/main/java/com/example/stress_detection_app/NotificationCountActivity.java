package com.example.stress_detection_app;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stress_detection_app.Adapter.NotificationAdapter;
import com.example.stress_detection_app.Listener.NotificationListener;
import com.example.stress_detection_app.Model.NotificationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
// NotificationCountActivity.java
public class NotificationCountActivity extends AppCompatActivity {
    private TextView notificationCountTextView;
    private RecyclerView recyclerView;
    private Handler handler = new Handler();
    private Runnable updateNotificationCountRunnable;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_count);

        notificationCountTextView = findViewById(R.id.totalNotificationsTextView);
        recyclerView = findViewById(R.id.notificationsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the back button
        backButton = findViewById(R.id.back);

        // Set an onClickListener to navigate to the main activity when the back button is clicked
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to go back to MainActivity
                Intent intent = new Intent(NotificationCountActivity.this, MainActivity.class);
                startActivity(intent);
//                finish();  // Finish the current activity to prevent the user from going back to it
            }
        });

        setupNotificationCountChecker();
        requestNotificationListenerPermission();
    }

    private void setupNotificationCountChecker() {
        updateNotificationCountRunnable = new Runnable() {
            @Override
            public void run() {
                int count = NotificationListener.notificationCount; // Get total count
                notificationCountTextView.setText("Total Notifications: " + count);

                // Display source apps and their counts
                List<NotificationData> notificationDataList = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : NotificationListener.notificationSources.entrySet()) {
                    String appName = entry.getKey();
                    int notificationCount = entry.getValue();
                    Drawable appIcon = getAppIcon(appName); // Implement a method to get app icon
                    notificationDataList.add(new NotificationData(appName, notificationCount, appIcon));
                }

                NotificationAdapter adapter = new NotificationAdapter(notificationDataList);
                recyclerView.setAdapter(adapter);

                // Schedule the next update
                handler.postDelayed(this, 10000); // Update every 10 seconds
            }
        };
        handler.post(updateNotificationCountRunnable);
    }

    // Method to get app icon by package name
    private Drawable getAppIcon(String packageName) {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, 0);
            return packageManager.getApplicationIcon(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return getResources().getDrawable(R.drawable.ic_launcher_foreground); // Default icon in case of error
        }
    }

    private void requestNotificationListenerPermission() {
        // Prompt user to enable notification listener
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateNotificationCountRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(updateNotificationCountRunnable);
    }
}
