package com.example.stress_detection_app.Services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class BluetoothService extends Service {

    private static final String TAG = "BluetoothService";
    private static final String DEVICE_NAME = "HC-05"; // Change to your Bluetooth module name
    private static final UUID DEVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    // Firebase References
    private DatabaseReference databaseReference;
    private String userId;

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the logged-in user's UID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("hardwareData");
        } else {
            showToast("User not logged in!");
            stopSelf();
            return;
        }

        showToast("Bluetooth Service Started");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Bluetooth not supported.");
            stopSelf();
        }

        connectToBluetooth();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void connectToBluetooth() {
        showToast("Attempting to connect to HC-05...");

        if (!bluetoothAdapter.isEnabled()) {
            showToast("Please enable Bluetooth first!");
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice targetDevice = null;

        for (BluetoothDevice device : pairedDevices) {
            if (DEVICE_NAME.equals(device.getName())) {
                targetDevice = device;
                showToast("HC-05 found. Connecting...");
                break;
            }
        }

        if (targetDevice == null) {
            showToast("HC-05 not paired. Pair manually first.");
            return;
        }

        try {
            bluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(DEVICE_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            showToast("Connected to HC-05!");
            listenForData();
        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
            showToast("Bluetooth connection failed!");
            stopSelf();
        }
    }

    private void listenForData() {
        showToast("Listening for data...");

        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        String receivedData = new String(buffer, 0, bytes).trim();
                        Log.d(TAG, "Data received: " + receivedData);
                        showToast("Data: " + receivedData);

                        // Push Data to Firebase
                        pushDataToFirebase(receivedData);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading data", e);
                    showToast("Error receiving data!");
                    break;
                }
            }
        }).start();
    }

    private void pushDataToFirebase(String data) {
        // Get Today's Date in YYYYMMDD Format
        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        // Push Data under `users/{userID}/sensorData/{YYYYMMDD}`
        databaseReference.child(todayDate).push().setValue(data)
                .addOnSuccessListener(aVoid -> showToast("Data sent to Firebase!"))
                .addOnFailureListener(e -> showToast("Failed to send data to Firebase!"));
    }

    private void showToast(String message) {
        new Handler(getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectBluetooth();
    }

    private void disconnectBluetooth() {
        try {
            if (inputStream != null) inputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
            showToast("Bluetooth disconnected.");
        } catch (IOException e) {
            Log.e(TAG, "Error disconnecting", e);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
