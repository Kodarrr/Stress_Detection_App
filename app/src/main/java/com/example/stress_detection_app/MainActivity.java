package com.example.stress_detection_app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.stress_detection_app.Listener.NotificationListener;
import com.example.stress_detection_app.Model.StressDetectionData;
import com.example.stress_detection_app.Services.BatteryService;
import com.example.stress_detection_app.Services.BluetoothService;
import com.example.stress_detection_app.Services.PhoneRingerService;
import com.example.stress_detection_app.Services.SensorBackgroundService;
import com.example.stress_detection_app.Services.SoundLevelService;
import com.example.stress_detection_app.Services.UnifiedService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Map<String, Object> batteryData = new HashMap<>();
    private Handler handler = new Handler();
    private Runnable dataCollectionTask, dataUploadRunnable;
    private static final int INTERVAL = 3000; // 3 seconds
    private static final int PERMISSION_REQUEST_CODE = 1;
    private Button startButton, stopButton, logoutButton;
    public TextView connectionStatusTextView;
    private TextView stressStatusTextView, bpmValueTextView, gsrValueTextView, accelValueTextView, gyroValueTextView;
    private LineChart bpmChart, gsrChart, accelChart, gyroChart;
    private DatabaseReference databaseReference2, hardwareDataRef;
    private FirebaseUser user;

    // Threshold values
    private static final int BPM_STRESS_THRESHOLD = 85;
    private static final int BPM_CALM_THRESHOLD = 65;
    private static final float GSR_STRESS_THRESHOLD = 2.5f;
    private static final float GSR_CALM_THRESHOLD = 1.0f;
    private static final float ACCEL_STRESS_THRESHOLD = 0.3f;
    //0.3-1.5 hocche stressed
    private static final float ACCEL_CALM_THRESHOLD = 0.2f;
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

    private CardView notificationCountCard, batteryCard, phoneringerCard, accelerometerCard, gyroscopeCard,lightCard, soundCard,gsrCard,bpmCard;
    private DatabaseReference databaseReference;
    private static final String TAG = "UnifiedService";
    BluetoothDevice arduinoBTModule = null;
    private final String DEVICE_NAME = "HC-05";
    UUID arduinoUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private Handler stressLevelHandler;
    private Runnable stressLevelRunnable;
    private static final long STRESS_LEVEL_INTERVAL = 60000; // 1 minute in milliseconds

//    private FirebaseUser user;

    private TextView phoneCallStatusValueTextView , ringerModeValueTextView , screenStatusValueTextView;
    // Variable to track the connection status
    private boolean isDeviceConnected = true;
    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction().equals("LocationUpdate")) {
                String address = intent.getStringExtra("address");
//                Toast.makeText(MainActivity.this, "Location: " + address, Toast.LENGTH_SHORT).show();
            }
        }
    };

//    private final BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
//                // Retrieve the Bluetooth device from the Intent
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//
//                if (device != null) {
//                    // Get the device's name and address
//
//                    @SuppressLint("MissingPermission") String deviceName = String.valueOf(device.getName()); // Can be null if the device doesn't broadcast its name
//                    //Toast.makeText(this,deviceName,Toast.LENGTH_SHORT).show();
//                    //textView.setText(deviceName);
//                    String deviceAddress = device.getAddress(); // MAC address of the device
//
//                    // Log the discovered device
//                    Log.d("BluetoothDevice", "Found: " + (deviceName != null ? deviceName : "Unknown") + " [" + deviceAddress + "]");
//                } else {
//                    Log.d("BluetoothDevice", "Device is null.");
//                }
//            }
//        }
//    };

//    private void startDiscovery() {
//        Log.d("DEBUG", "discovery started");
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//            //
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        if (bluetoothAdapter.isDiscovering()) {
//            bluetoothAdapter.cancelDiscovery();
//        }
//        //textview2.setText("discovering");
//
//        // Register the BroadcastReceiver
//        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//        registerReceiver(receiver, filter);
//
//
//
//        // Start discovering devices
//        bluetoothAdapter.startDiscovery();
//        Toast.makeText(MainActivity.this, "hiiiiiii", Toast.LENGTH_SHORT).show();
//
//        Log.d("BluetoothDevice", "Discovery started...");
//    }

//    private void connectToDevice() {
//
//        if (!bluetoothAdapter.isEnabled()) {
//            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Check and request Bluetooth permissions for Android 12+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
//                return;
//            }
//        }
//
//        Toast.makeText(this, "Connecting to HC-05...", Toast.LENGTH_LONG).show();
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
//        BluetoothDevice targetDevice = null;
//
//        // Check if there are any paired devices
//        if (pairedDevices.isEmpty()) {
//            Toast.makeText(this, "No paired Bluetooth devices found!", Toast.LENGTH_LONG).show();
//            Log.d("BluetoothDebug", "No paired devices found.");
//            return;
//        }
//
//        // Find the target device
//        for (BluetoothDevice device : pairedDevices) {
//            if ("HC-05".equals(device.getName())) {
//                targetDevice = device;
//                break;
//            }
//        }
//
//        // If the device was not found
//        if (targetDevice == null) {
//            Toast.makeText(this, "Device not found. Pair with it first.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // Cancel discovery before attempting to connect
//        if (bluetoothAdapter.isDiscovering()) {
//            bluetoothAdapter.cancelDiscovery();
//        }
//
//        try {
//            // Connect to the device
//            bluetoothSocket = targetDevice.createRfcommSocketToServiceRecord(
//                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard UUID for SPP
//            );
//            bluetoothSocket.connect();
//
//            // Set connection status to true
//            isDeviceConnected = true;
//
//            Toast.makeText(this, "Connected to HC-05!", Toast.LENGTH_SHORT).show();
//            //To show in real time that hardware is connected
//            runOnUiThread(() -> {connectionStatusTextView.setText("Connected");
//                connectionStatusTextView.setTextColor(Color.parseColor("#D3D3D3"));}
//            );
//
//            // Get InputStream and start listening for data
//            InputStream inputStream = bluetoothSocket.getInputStream();
//            listenForData(inputStream);
//
//        } catch (Exception e) {
//            Log.e("BluetoothConnection", "Connection failed", e);
//            Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
//
//            // Set connection status to false in case of failure
//            isDeviceConnected = false;
//
//            //To show in real time that hardware is not connected
//
//            runOnUiThread(() -> {connectionStatusTextView.setText("Not Connected");
//                                connectionStatusTextView.setTextColor(Color.parseColor("#2F7D32"));
//
//            }
//            );
//        }
//    }
//    private void listenForData(final InputStream inputStream) {
//        new Thread(() -> {
//            try {
//                byte[] buffer = new byte[1024]; // Buffer for incoming data
//                int bytes; // Number of bytes read
//
//                while (bluetoothSocket.isConnected()) { // Keep reading while connected
//                    bytes = inputStream.read(buffer);
//                    if (bytes > 0) { // Ensure valid data is received
//                        final String receivedData = new String(buffer, 0, bytes).trim();
//
//                        Log.d("BluetoothReceivedData", "Received: " + receivedData);
//
//                        // Broadcast the received data (optional if you want to send it to another component)
//                        Intent broadcastIntent = new Intent("com.example.stress_detection_app.BLUETOOTH_DATA");
//                        broadcastIntent.putExtra("receivedData", receivedData);
//                        sendBroadcast(broadcastIntent);
//
//                        // Update UI with received data
//                        runOnUiThread(() -> {
//                            //Toast.makeText(MainActivity.this, "Data: " + receivedData, Toast.LENGTH_SHORT).show();
//                            // textView2.setText("Received: " + receivedData); // Uncomment if using a TextView
//                        });
//                    }
//                }
//            } catch (IOException e) {
//                Log.e("BluetoothReceiveData", "Error receiving data", e);
//            } finally {
//                try {
//                    if (bluetoothSocket != null) {
//                        bluetoothSocket.close(); // Close socket when done
//                        Log.d("BluetoothReceiveData", "Bluetooth socket closed.");
//                    }
//                } catch (IOException ex) {
//                    Log.e("BluetoothReceiveData", "Error closing socket", ex);
//                }
//            }
//        }).start();
//    }
//




//    private void listenForData() {
//        new Thread(() -> {
//            try {
//                if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
//                    byte[] buffer = new byte[1024]; // Buffer for incoming data
//                    int bytes; // Number of bytes read
//
//                    while (true) {
//                        bytes = bluetoothSocket.getInputStream().read(buffer); // Read data
//                        String receivedData = new String(buffer, 0, bytes); // Convert to string
//                        //textview2.setText(receivedData);
//                        Log.d("BluetoothReceivedData", "Data received: " + receivedData);
//                        Intent broadcastIntent = new Intent("com.example.stress_detection_app.BLUETOOTH_DATA");
//                        broadcastIntent.putExtra("receivedData", receivedData);
//                        sendBroadcast(broadcastIntent);
//
//                        // Update the UI with the received data
//                        //runOnUiThread(() -> textview2.setText("Received: " + receivedData));
//
//                        //runOnUiThread(()->Toast.makeText(MainActivity.this, "data: " + receivedData, Toast.LENGTH_SHORT).show());
//                    }
//                }
//            } catch (Exception e) {
//                Log.e("BluetoothReceiveData", "Error receiving data", e);
//            }
//        }).start();
//    }


    private void requestLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        databaseReference = FirebaseDatabase.getInstance().getReference("StressData");
        FirebaseApp.initializeApp(this);

        IntentFilter filter = new IntentFilter(BatteryService.ACTION_BATTERY_UPDATE);
        registerReceiver(new BatteryReceiver(), filter, Context.RECEIVER_NOT_EXPORTED);

        connectionStatusTextView=findViewById(R.id.connectionStatusTextView);
        startButton = findViewById(R.id.startButton);
        stopButton = findViewById(R.id.stopButton);
        logoutButton = findViewById(R.id.logoutButton);
        gsrCard= findViewById(R.id.gsrCard);
        bpmCard = findViewById(R.id.bpmCard);
        notificationCountCard = findViewById(R.id.NotificationCountCard);
        batteryCard = findViewById(R.id.batteryCard);
        phoneringerCard = findViewById(R.id.phoneringerCard);
        accelerometerCard = findViewById(R.id.accelerometerCard);
        gyroscopeCard = findViewById(R.id.gyroscopeCard);
        lightCard = findViewById(R.id.lightCard);
        soundCard = findViewById(R.id.soundCard);
        phoneCallStatusValueTextView= findViewById(R.id.phoneStatusValueTextView);
        ringerModeValueTextView = findViewById(R.id.ringerModeValueTextView);
//        screenStatusValueTextView = findViewById(R.id.screenStatusValueTextView);
        stressStatusTextView=findViewById(R.id.stressLevel);
        connectionStatusTextView=findViewById(R.id.connectionStatusTextView);
//        stressLevelHandler = new Handler(Looper.getMainLooper());
//        stressLevelRunnable = new Runnable() {
//            @Override
//            public void run() {
//                stressLevelShow();
//                // Schedule the next execution after 1 minute
//                stressLevelHandler.postDelayed(this, STRESS_LEVEL_INTERVAL);
//            }
//        };

        // Initialize Firebase Auth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {
            // If not logged in, redirect to login screen
            Intent loginIntent = new Intent(MainActivity.this, Login.class);
            startActivity(loginIntent);
            finish();  //used bcz user cant go back to it
        }

            checkPermissionsAndStartServices();
        checkNotificationListenerPermission();
        checkAndRequestDNDPermission();
        checkAndRequestAudioPermission();
        startButton.setOnClickListener(v -> {

            NotificationListener.resetNotifications();
//            connectToDevice();
            startService(new Intent(this, UnifiedService.class)); // Start the service
            startService(new Intent(this, BluetoothService.class)); // Start the service
            runOnUiThread(() -> {connectionStatusTextView.setText("Connected");
                connectionStatusTextView.setTextColor(Color.parseColor("#2F7D32"));}
            );
            startButton.setText("Started");
            stopButton.setBackgroundColor(Color.RED);
            startButton.setEnabled(false);

//            stressLevelHandler.postDelayed(stressLevelRunnable, STRESS_LEVEL_INTERVAL);
//            stopButton.setTextColor(Color.RED);
//            startButton.setTextColor(Color.RED);
            // Connect to Bluetooth device when Start is clicked
//            stressLevelShow();
            // Get the stress level and progress from the intent



        });
//        connectButton.setOnClickListener(v -> {
//            //NotificationListener.resetNotifications();
//            startService(new Intent(this, BluetoothService.class)); // Start the service
//        });
        //stopButton.setOnClickListener(v -> stopService(new Intent(this, UnifiedService.class)));
        logoutButton.setOnClickListener(v -> performLogout());
        stopButton.setOnClickListener(v -> {
            runOnUiThread(() -> {connectionStatusTextView.setText("Not Connected");
                                connectionStatusTextView.setTextColor(Color.parseColor("#2F7D32"));

            }
            );
            stopService(new Intent(this, UnifiedService.class));
            stopService(new Intent(this, BluetoothService.class));
            startButton.setText("Start");
//            startButton.setTextColor(Color.WHITE);
//            stopButton.backgro
            startButton.setEnabled(true);
            stopButton.setTextColor(Color.WHITE);
        });






//        connectButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Log.d("DEBUG", "Connect button clicked");
////                startDiscovery();
////                Log.d("DEBUG", "discovery done");
////                connectToDevice();
//                Intent serviceIntent = new Intent(MainActivity.this, BluetoothService.class);
//                startService(serviceIntent);
//                //Toast.makeText(MainActivity.this, "Bluetooth Service Started", Toast.LENGTH_SHORT).show();
//            }
//        });




        disableBatteryOptimizations();
//        initializeDataCollectionTask();

        setupCardViewListeners();

        //adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            // Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            Toast.makeText(MainActivity.this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();

            finish();
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(enableBtIntent, 1);
        }

        //Bluetooth code start here

        requestLocationPermission();

//        Toast.makeText(MainActivity.this,
//                "mew mew",
//                Toast.LENGTH_SHORT).show();


        //Fetch Foreground App

//        Intent intent = new Intent(MainActivity.this, LocationActivity.class);
//        startActivityForResult(intent, 1);

//        Intent serviceIntent = new Intent(this, LocationService.class);
//        startForegroundService(serviceIntent);



//        if (isUsageAccessGranted()) {
////
//
//            //startLoggingForegroundApp();
//
//            Intent serviceIntent2= new Intent(MainActivity.this,ForegroundAppLoggerService.class);
//            startForegroundService(serviceIntent2);
//
//        } else {
//            // Prompt user to enable Usage Access
//            requestUsageAccessPermission();
//        }




    }
    @Override
    protected void onStart() {
        super.onStart();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter("LocationUpdate");
        registerReceiver(locationReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister the BroadcastReceiver
        unregisterReceiver(locationReceiver);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Retrieve data from LocationActivity
            String resultData = data.getStringExtra("resultData");
            // Handle the data or update UI as necessary
        }
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut(); // Log out the user

        // Redirect to Login Activity
        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear activity stack
        startActivity(intent);

        // Show a toast message
        Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // Helper function to check permissions
    private boolean hasPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void checkPermissionsAndStartServices() {
        if (hasPermissions()) {
            startAllServices();
        } else {
            // Request the necessary permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_CODE);
        }
    }

    private void setupCardViewListeners() {
        gsrCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, GSR_Resistance.class));
            } else {
                showPermissionDeniedToast();
            }
        });

        bpmCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, bpm.class));
            } else {
                showPermissionDeniedToast();
            }
        });
        notificationCountCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, NotificationCountActivity.class));
            } else {
                showPermissionDeniedToast();
            }
        });

        batteryCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, StressDetectionActivity.class));
            } else {
                showPermissionDeniedToast();
            }
        });

        phoneringerCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, PhoneRingerActivity.class));
            } else {
                showPermissionDeniedToast();
            }
        });

        accelerometerCard.setOnClickListener(v -> {
            if (hasPermissions()) {
                startActivity(new Intent(MainActivity.this, AccelerometerActivity.class));
            } else {
                showPermissionDeniedToast();
            }
        });

        gyroscopeCard.setOnClickListener(
                v -> {
                    if (hasPermissions()) {
                        startActivity(new Intent(MainActivity.this, GyroscopeActivity.class));
                    } else {
                        showPermissionDeniedToast();
                    }
                }
        );




        lightCard.setOnClickListener(v -> {
                if (hasPermissions()) {
                    startActivity(new Intent(MainActivity.this, AmbientLightActivity.class));
                }
                else {
                    showPermissionDeniedToast();
                }
                }
        );

        soundCard.setOnClickListener(v -> {
//            if (hasPermissions()) {
//                startActivity(new Intent(MainActivity.this, SoundLevelActivity.class));
//            } else {
//                showPermissionDeniedToast();
//            }
            stressLevelShow();
        });
    }

    private void startAllServices() {
        if (hasPermissions()) {
            startService(new Intent(this, BatteryService.class));
            startService(new Intent(this, PhoneRingerService.class));
            startService(new Intent(this, SensorBackgroundService.class));
            startService(new Intent(this, SoundLevelService.class));
        } else {
            Toast.makeText(this, "Required permissions are not granted.", Toast.LENGTH_SHORT).show();
        }
    }


    private void stopAllServices() {
        stopService(new Intent(this, BatteryService.class));
        stopService(new Intent(this, PhoneRingerService.class));
        stopService(new Intent(this, SensorBackgroundService.class));
        stopService(new Intent(this, SoundLevelService.class));
    }

    private void showPermissionDeniedToast() {
        Toast.makeText(this, "Permission is required to access this feature.", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            boolean audioPermissionGranted = false;

            // Iterate through permissions to check their results
            for (int i = 0; i < permissions.length; i++) {
                if (permissions[i].equals(Manifest.permission.RECORD_AUDIO)) {
                    // Check if RECORD_AUDIO permission was granted
                    audioPermissionGranted = (grantResults[i] == PackageManager.PERMISSION_GRANTED);
                }

                // Check if any permission was denied
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                }
            }

            // Handle permissions based on results
            if (allPermissionsGranted) {
                // Start all services if all required permissions are granted
                startAllServices();
            } else {
                // Handle RECORD_AUDIO-specific scenario
                if (!audioPermissionGranted) {
                    Toast.makeText(this, "Audio recording permission is required for noise level detection.", Toast.LENGTH_SHORT).show();
                }
                // Show general permission denied toast
                showPermissionDeniedToast();
            }
        }
    }
    private void initializeDataCollectionTask() {
        dataCollectionTask = new Runnable() {
            @Override
            public void run() {
                // Broadcast an intent to trigger data collection in services
                Intent intent = new Intent("com.example.stress_detection_app.COLLECT_DATA");
                sendBroadcast(intent);

                // Schedule the next execution
                handler.postDelayed(this, INTERVAL);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(dataCollectionTask); // Start periodic data collection

        // Register the receiver to listen for data updates from services
        IntentFilter filter = new IntentFilter("com.example.stress_detection_app.DATA_UPDATE");
        registerReceiver(dataReceiver,filter);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(new BatteryReceiver(), filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(new BatteryReceiver(), filter);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(dataCollectionTask); // Stop periodic data collection
        try {
            unregisterReceiver(dataReceiver);
        } catch (IllegalArgumentException e) {
            Log.e("ReceiverError", "Receiver not registered", e);
        }
    }

    public class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getExtras() != null) {
                int batteryLevel = intent.getIntExtra("batteryLevel", -1);
                String batteryStatus = intent.getStringExtra("batteryStatus");
                String dndStatus = intent.getStringExtra("dndStatus");

                batteryData.put("batteryLevel", batteryLevel);
                batteryData.put("batteryStatus", batteryStatus != null ? batteryStatus : "Unknown");
                batteryData.put("dndStatus", dndStatus != null ? dndStatus : "Unknown");
            } else {
                Log.e("BatteryReceiver", "Intent or extras are null");
            }
        }
    }

    public Map<String, Object> getBatteryData() {
        return batteryData;
    }



    private void stopDataCollection() {
        handler.removeCallbacks(dataUploadRunnable);
    }

    private void sendToFirebase(Map<String, Object> data) {
        if (!data.isEmpty()) {
            databaseReference.push().setValue(data)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Data uploaded successfully"))
                    .addOnFailureListener(e -> Log.d("Firebase", "Failed to upload data", e));
        } else {
            Toast.makeText(this, "No data to upload", Toast.LENGTH_SHORT).show();
        }
    }
    private void uploadDataToFirebase(StressDetectionData data) {
        String uniqueKey = databaseReference.push().getKey();
        if (uniqueKey != null) {
            databaseReference.child(uniqueKey).setValue(data)
                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Data uploaded successfully", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> {
                        Toast.makeText(MainActivity.this, "Failed to upload data. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseError", "Error uploading data", e);
                    });
        }
    }


    private BroadcastReceiver dataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Collect data from the broadcast
            String batteryData = intent.getStringExtra("batteryData");
            String notificationStatus = intent.getStringExtra("notificationStatus");
            String phoneStatus = intent.getStringExtra("phoneStatus");
            String sensors = intent.getStringExtra("sensors");
            String noiseLevel = intent.getStringExtra("noiseLevel");
            String dndstatus = intent.getStringExtra("dndStatus");
            float accelerometerX = intent.getFloatExtra("accelerometerX", 0f);
            float accelerometerY = intent.getFloatExtra("accelerometerY", 0f);
            float accelerometerZ = intent.getFloatExtra("accelerometerZ", 0f);

            float gyroscopeX = intent.getFloatExtra("gyroscopeX", 0f);
            float gyroscopeY = intent.getFloatExtra("gyroscopeY", 0f);
            float gyroscopeZ = intent.getFloatExtra("gyroscopeZ", 0f);

            float ambientLight = intent.getFloatExtra("ambientLight", -1f);

            int phoneCallStatusValue = intent.getIntExtra("phoneCallStatusValue", -1);
            String statusText = mapPhoneCallStatus(phoneCallStatusValue);

            int ringerModeValue = intent.getIntExtra("ringerModeValue", -1);
            String ringerModeText = mapRingerMode(ringerModeValue);

            int screenStatusValue =  intent.getIntExtra("screenStatusValue", -1);
            String screenStatusText = mapScreenStatus(screenStatusValue);


           // phoneCallStatusValueTextView.setText(statusText);
            runOnUiThread(() -> {
                        phoneCallStatusValueTextView.setText(statusText);
                        ringerModeValueTextView.setText(ringerModeText);
//                        screenStatusValueTextView.setText(screenStatusText);
                    }
            );

            // Generate a timestamp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Combine data into a StressDetectionData object
            StressDetectionData data = new StressDetectionData();
            data.setTimestamp(timestamp);
            data.setBatteryData(batteryData);
            data.setNotificationStatus(notificationStatus);
            data.setPhoneStatus(phoneStatus);
            data.setSensors(sensors);
            data.setNoiseLevel(noiseLevel);
            data.setDndstatus(dndstatus);

            data.setAccelerometerX(accelerometerX);
            data.setAccelerometerY(accelerometerY);
            data.setAccelerometerZ(accelerometerZ);

            data.setGyroscopeX(gyroscopeX);
            data.setGyroscopeY(gyroscopeY);
            data.setGyroscopeZ(gyroscopeZ);

            data.setAmbientLight(ambientLight);

            // Send the data to Firebase
            uploadDataToFirebase(data);



        }
    };

    private boolean isUsageAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
            List<UsageStats> stats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY, System.currentTimeMillis() - 1000 * 60 *6, System.currentTimeMillis());

            return stats != null && !stats.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    private void requestUsageAccessPermission() {
        Toast.makeText(this, "Please enable Usage Access for this app", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }


    private void disableBatteryOptimizations() {
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void checkNotificationListenerPermission() {
        if (!NotificationListener.isEnabled(this)) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }



    private void checkAndRequestDNDPermission() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && !notificationManager.isNotificationPolicyAccessGranted()) {
            // Guide the user to grant the required permission
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }


    }

    private void checkAndRequestAudioPermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, guide the user to app settings
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.d(TAG, "Please enable RECORD_AUDIO permission in app settings.");
        } else {
            Log.d(TAG, "RECORD_AUDIO permission already granted.");
        }
    }

    /**
     * Maps the integer phone call status to a descriptive string.
     */
    private String mapPhoneCallStatus(int status) {
        switch (status) {
            case 0:
                return "Idle";
            case 1:
                return "Ringing";
            case 2:
                return "In Call";
            default:
                return "Unknown";
        }
    }
    private String mapRingerMode(int mode) {
        switch (mode) {
            case 0:
                return "Silent";
            case 1:
                return "Vibrate";
            case 2:
                return "Normal";
            default:
                return "Unknown";
        }
    }

    private String mapScreenStatus(int status) {
        switch (status) {
            case 0:
                return "Screen Off";
            case 1:
                return "Screen On";
            default:
                return "Unknown";
        }
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

    public void stressLevelShow() {
        Toast.makeText(getApplicationContext(), "done", Toast.LENGTH_SHORT).show();
        stressStatusTextView.setText("Calculating...");
        // Get the stress level and progress from the intent
//        Intent intent = getIntent();
//        String stressLevel = intent.getStringExtra("stressLevel"); // Stress level status (e.g., STRESSED, NORMAL, CALM)
//        int stressProgress = intent.getIntExtra("stressProgress", 0);  // Stress progress value (0 - 100)
//
//        // Find the TextView and ProgressBar in MainActivity layout
//        TextView stressStatusTextView = findViewById(R.id.stressStatusTextView);
//        TextView stressLevelTextView = findViewById(R.id.stressLevel);
//        ProgressBar stressLevelProgressBar = findViewById(R.id.stressLevelProgressBar);
//
//        // Update the TextView with the received stress level
//        stressStatusTextView.setText("Current Stress Level: " + stressLevel);
//        stressLevelTextView.setText(stressLevel); // Displaying the stress level (e.g., CALM, NORMAL, STRESSED)
//
//        // Update the ProgressBar with the received stress progress
//        stressLevelProgressBar.setProgress(stressProgress); // Update progress from 0 to 100
        initializeFirebase();
        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        fetchSensorData(todayDate);
        fetchHardwareData(todayDate);
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

//                        runOnUiThread(() -> {
////                            accelValueTextView.setText(String.format(Locale.getDefault(), "%.2f m/s²", accelMagnitude));
////                            gyroValueTextView.setText(String.format(Locale.getDefault(), "%.2f rad/s", gyroMagnitude));
//                            updateStressLevel();
//                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

//                updateChart(accelChart, accelEntries, "Accel", Color.GREEN);
//                updateChart(gyroChart, gyroEntries, "Gyro", Color.MAGENTA);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stressStatusTextView.setText("Error fetching sensor data");
            }
        });
    }
    private void determineStressLevel(float bpm, float gsr, float accelMagnitude, float gyroMagnitude) {
//
        final float BPM_HIGH = 90f;

        final float BPM_LOW = 60f;       // Low heart rate threshold
        final float GSR_HIGH = 2.4f;     // High skin resistance threshold
        final float GSR_LOW = 0.5f;      // Low skin resistance threshold
        final float ACCEL_HIGH = 2.4f;   // High accelerometer magnitude
        final float ACCEL_LOW = 0.3f;    // Low accelerometer magnitude
        final float GYRO_HIGH = 4.19f;   // High gyroscope magnitude
        final float GYRO_LOW = 2.62f;

        String status="Normal";
        int color;
        color =Color.YELLOW;
        if(bpm>=BPM_HIGH && gsr>= GSR_LOW && gsr<=GSR_HIGH)
        {
            status="Normal";
        }
        else if(bpm>BPM_HIGH && gsr>GSR_HIGH)
        {
            status="Stressed";
        }
        else if(bpm>=BPM_HIGH && gsr <=GSR_LOW && accelMagnitude>(ACCEL_HIGH+ACCEL_LOW)/2.0 && gyroMagnitude> (GYRO_HIGH+GYRO_LOW)/2.0){
            status="Normal";
        }
        else if(bpm>=BPM_HIGH && gsr <=GSR_LOW && accelMagnitude <(ACCEL_HIGH+ACCEL_LOW)/2.0 && gyroMagnitude < (GYRO_HIGH+GYRO_LOW)/2.0){
            status="Stressed";
        }
        else if(bpm>=BPM_LOW&&bpm<=BPM_HIGH && gsr>GSR_HIGH+5.0){
            status="Stressed";
        }
        else if(bpm>=BPM_LOW&&bpm<=BPM_HIGH &&gsr>GSR_HIGH && gsr<GSR_HIGH+5.0){
            status="Normal";
        }
        else if(bpm>=BPM_LOW&&bpm<=BPM_HIGH && gsr<(GSR_LOW+GSR_HIGH)/2.0 && accelMagnitude<(ACCEL_HIGH+ACCEL_LOW)/2.0 &&gyroMagnitude <(GYRO_HIGH+GYRO_LOW)/2.0)
        {
            status="Calm";
        }
        else if(bpm>=BPM_LOW&&bpm<=BPM_HIGH && gsr<(GSR_LOW+GSR_HIGH)/2.0 && accelMagnitude>(ACCEL_HIGH+ACCEL_LOW)/2.0 &&gyroMagnitude >(GYRO_HIGH+GYRO_LOW)/2.0)
        {
            status="Calm";
        }
        else if(bpm < (BPM_LOW+BPM_HIGH)/2.0 && gsr >GSR_HIGH+5.0)
        {
            status ="Stressed";
        }
        else if(bpm < (BPM_LOW+BPM_HIGH)/2.0 && gsr >GSR_HIGH){
            status = "Normal";
        }
        else if(bpm < (BPM_LOW+BPM_HIGH)/2.0 && gsr<(GSR_LOW+GSR_HIGH)/2.0){
            status="Calm";
        }
        else if(bpm < (BPM_LOW+BPM_HIGH)/2.0 && gsr<(GSR_LOW+GSR_HIGH)/2.0&& accelMagnitude <ACCEL_HIGH &&gyroMagnitude <GYRO_HIGH)
        {
            status="Calm";
        }
        else if(bpm>BPM_HIGH && gsr >GSR_HIGH && accelMagnitude> ACCEL_HIGH&& gyroMagnitude>GYRO_HIGH)
        {
            status="Stressed";
        }





//


        // Check if status changed to STRESSED
        if (status.equals("Stressed") ) {
            showStressNotification();
        }

        stressStatusTextView.setText(status);
        if(status.equals("Stressed")){
            color = Color.RED;
        }
        else if(status.equals("Normal")){
            color=Color.YELLOW;
        }
        else {
            color=Color.GREEN;
        }
        stressStatusTextView.setTextColor(color);
    }

    private void showStressNotification() {
        // Create notification channel (required for Android 8.0+)
        createNotificationChannel();

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "STRESS_CHANNEL")
                .setSmallIcon(R.drawable.ic_warning) // Use your own icon
                .setContentTitle("Stress Alert")
                .setContentText("You appear to be stressed. Consider taking a break.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Create an explicit intent for when notification is tapped
        Intent intent = new Intent(this, MainActivity.class); // Replace with your activity
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);

        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Stress Alerts";
            String description = "Notifications for stress detection";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("STRESS_CHANNEL", name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
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
    private void updateStressLevel() {
        determineStressLevel(bpm, gsr, accelMagnitude, gyroMagnitude);
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
//                            bpmValueTextView.setText(String.format(Locale.getDefault(), "%.0f BPM", bpm));
//                            gsrValueTextView.setText(String.format(Locale.getDefault(), "%.2f μS", gsr));
                            updateStressLevel();
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

//                updateChart(bpmChart, bpmEntries, "BPM", Color.RED);
//                updateChart(gsrChart, gsrEntries, "GSR", Color.BLUE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                stressStatusTextView.setText("Error fetching hardware data");
            }
        });
    }


}