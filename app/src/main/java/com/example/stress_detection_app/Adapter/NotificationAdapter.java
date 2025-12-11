package com.example.stress_detection_app.Adapter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stress_detection_app.Model.NotificationData;
import com.example.stress_detection_app.R;

import java.util.List;
import java.util.Map;
// NotificationAdapter.java
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private List<NotificationData> notificationDataList;

    public NotificationAdapter(List<NotificationData> notificationDataList) {
        this.notificationDataList = notificationDataList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotificationData data = notificationDataList.get(position);
        holder.appNameTextView.setText(data.getAppName());
        holder.notificationCountTextView.setText(String.valueOf(data.getNotificationCount()));
        holder.appIconImageView.setImageDrawable(data.getAppIcon());
    }

    @Override
    public int getItemCount() {
        return notificationDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView appIconImageView;
        public TextView appNameTextView;
        public TextView notificationCountTextView;

        public ViewHolder(View view) {
            super(view);
            appIconImageView = view.findViewById(R.id.appIconImageView);
            appNameTextView = view.findViewById(R.id.appNameTextView);
            notificationCountTextView = view.findViewById(R.id.notificationCountTextView);
        }
    }
}
