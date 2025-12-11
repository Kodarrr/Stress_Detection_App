# Stress Level Detection Using Wearable Sensors with Smartphone Integration

A real-time stress monitoring system that combines wearable physiological sensors with smartphone motion sensors and fuzzy logic to classify user stress levels as calm, normal, or stressed.

---

## Project Overview

This project implements a hybrid stress detection system using a PPG sensor, GSR sensor, and smartphone sensors (accelerometer, gyroscope) connected to an Android application over Bluetooth. The system processes continuous data streams with a fuzzy logic engine to provide real-time stress level feedback without requiring pre-collected datasets or complex machine learning models.

---

## Key Features

- Real-time stress level monitoring (calm, normal, stressed) from multimodal sensor data.  
- Integration of wearable PPG and GSR sensors with smartphone accelerometer and gyroscope.  
- Bluetooth-based data transmission from Arduino-based wearable to Android app.  
- Fuzzy logic–based stress classification using predefined sensor thresholds.  
- Data visualization with real-time graphs (e.g., heart rate, GSR, motion signals) using MPAndroidChart.  
- Firebase-backed real-time data storage and synchronization.  
- User authentication (login/sign-up) and simple, mobile-friendly UI.

---

## System Architecture

The system consists of three main components:

1. **Wearable Hardware (Arduino-based)**  
   - PPG sensor: heart rate and RMSSD-based variability.  
   - GSR sensor: skin conductance via resistance in kΩ.  
   - Bluetooth module: sends processed readings to smartphone.

2. **Smartphone Sensors (Android Device)**  
   - Accelerometer: motion/activity intensity in m/s².  
   - Gyroscope: rotational movement in rad/s.

3. **Android Application**  
   - Receives sensor data streams (wearable + built-in).  
   - Applies fuzzy logic rules to classify stress level.  
   - Displays current stress status, sensor trends, and notifications.  
   - Stores and syncs data through Firebase backend.

A three-stage pipeline (acquisition → fusion via fuzzy logic → visualization/feedback) ensures continuous monitoring with modular hardware–software separation.

---

## Stress Classification Logic

Sensor value ranges are mapped into low, medium, and high stress levels, which are combined by fuzzy rules:

| Sensor              | Low Stress       | Medium Stress      | High Stress        |
|---------------------|------------------|--------------------|--------------------|
| PPG (RMSSD, ms)     | `< 60`           | `60 – 90`          | `> 90`             |
| GSR (kΩ)            | `< 30`           | `30 – 50`          | `> 50`             |
| Accelerometer (m/s²)| `0.1 – 0.3`      | `0.3 – 2.4`        | `> 2.4`            |
| Gyroscope (rad/s)   | `< 2.62`         | `2.62 – 4.19`      | `> 4.19`           |

Consistently high readings across one or more channels result in a high stress classification; mixed or moderate readings map to normal, and low activity/physiology map to calm.

---

## Tech Stack

- **Hardware**: Arduino Uno, PPG sensor, GSR sensor, Bluetooth module, custom wrist/wearable setup.  
- **Mobile Platform**: Android (Java, Android Studio).  
- **Backend / Cloud**: Firebase (real-time database/storage, synchronization).  
- **Visualization**: MPAndroidChart for real-time graphs and plots.  
- **Logic**: Fuzzy logic engine for rule-based stress classification.

---

## Installation & Setup

1. **Hardware Setup**  
   - Assemble PPG and GSR sensors with Arduino Uno following the project’s wiring scheme.  
   - Configure the Bluetooth module to broadcast sensor readings to a paired Android phone.  
   - Ensure sensors are calibrated and securely worn to maintain stable contact.

2. **Android App Setup**  
   - Open the Android project in Android Studio.  
   - Configure Firebase credentials (google-services configuration) for real-time database usage.  
   - Build and install the app on an Android device with accelerometer and gyroscope support.  
   - Grant required permissions (Bluetooth, sensors, network, etc.).

3. **First Run**  
   - Pair the wearable device with the smartphone via Bluetooth.  
   - Register or log in to the app using the built-in authentication.  
   - Start the monitoring session to begin real-time stress detection and visualization.

*(Adjust package names, Firebase config files, and Arduino sketch paths as needed for your repository structure.)*

---

## How It Works (Runtime Flow)

1. Wearable sensors continuously capture PPG and GSR data and send them via Bluetooth to the smartphone.  
2. The Android app simultaneously reads accelerometer and gyroscope data from the device.  
3. All four input streams are normalized and fed into a fuzzy logic inference system using the defined thresholds.  
4. The system outputs a stress level label (calm/normal/stressed), which is displayed in real time along with graphs of raw signals.  
5. Data is optionally logged to Firebase for historical viewing and analysis.

---

## Use Cases

- Continuous self-monitoring of stress during study, work, or commuting.  
- Non-clinical mental health awareness tools for students and professionals.  
- Research prototypes for stress detection, human–computer interaction, or affective computing.

---

## Limitations

- Requires wearing the hardware device, which may affect long-term comfort and compliance.  
- Depends on a smartphone for full functionality and connectivity.  
- Bluetooth link may occasionally drop, requiring reconnection.  
- Fuzzy thresholds are literature-based and not individually personalized.

---

## Future Improvements

- Miniaturized, watch-like wearable form factor with integrated PCB.  
- Personalized stress thresholds based on user-specific baselines.  
- Integration of additional mobile context signals (notifications, audio environment, etc.).  
- Optional machine learning / deep learning models on top of current fuzzy features.  
- Enhanced Android UI/UX with historical analytics and configurable alerts.

---

## Ethical and Legal Notes

- Only essential physiological and motion data are collected, with explicit user consent and no personal identifiers stored in the system design.  
- Data processing is intended to occur locally on the device, with Firebase usage structured to respect anonymity and basic data protection principles.  
- The system is a research prototype and does not constitute a certified medical device; it must not be used for diagnosis or treatment decisions.

---

## Authors

- **Shah Md Khalil Ullah** (Roll: 2007090)  
- **Shayka Islam Shipra** (Roll: 2007110)

Department of Computer Science and Engineering,  
Khulna University of Engineering & Technology (KUET), Bangladesh.
