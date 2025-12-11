package com.example.stress_detection_app.Model;

import android.graphics.drawable.Drawable;

// NotificationData.java
public class NotificationData {
    private String appName;
    private int notificationCount;
    private Drawable appIcon;

    public NotificationData(String appName, int notificationCount, Drawable appIcon) {
        this.appName = appName;
        this.notificationCount = notificationCount;
        this.appIcon = appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public int getNotificationCount() {
        return notificationCount;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }
}
