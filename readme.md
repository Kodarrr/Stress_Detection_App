# Stress Level Detection Using Wearable Sensors with Smartphone Integration

A real-time stress monitoring system that combines wearable physiological sensors with smartphone motion sensors and fuzzy logic to classify user stress levels as calm, normal, or stressed.[file:1]

---

## Project Overview

This project implements a hybrid stress detection system using a PPG sensor, GSR sensor, and smartphone sensors (accelerometer, gyroscope) connected to an Android application over Bluetooth.[file:1] The system processes continuous data streams with a fuzzy logic engine to provide real-time stress level feedback without requiring pre-collected datasets or complex machine learning models.[file:1]

---

## Key Features

- Real-time stress level monitoring (calm, normal, stressed) from multimodal sensor data.[file:1]  
- Integration of wearable PPG and GSR sensors with smartphone accelerometer and gyroscope.[file:1]  
- Bluetooth-based data transmission from Arduino-based wearable to Android app.[file:1]  
- Fuzzy logic–based stress classification using predefined sensor thresholds.[file:1]  
- Data visualization with real-time graphs (e.g., heart rate, GSR, motion signals) using MPAndroidChart.[file:1]  
- Firebase-backed real-time data storage and synchronization.[file:1]  
- User authentication (login/sign-up) and simple, mobile-friendly UI.[file:1]

---

## System Architecture

The system consists of three main components:[file:1]

1. **Wearable Hardware (Arduino-based)**  
   - PPG sensor: heart rate and RMSSD-based variability.[file:1]  
   - GSR sensor: skin conductance via resistance in kΩ.[file:1]  
   - Bluetooth module: sends processed readings to smartphone.[file:1]

2. **Smartphone Sensors (Android Device)**  
   - Accelerometer: motion/activity intensity in m/s².[file:1]  
   - Gyroscope: rotational movement in rad/s.[file:1]

3. **Android Application**  
   - Receives sensor data streams (wearable + built-in).[file:1]  
   - Applies fuzzy logic rules to classify stress level.[file:1]  
   - Displays current stress status, sensor trends, and notifications.[file:1]  
   - Stores and syncs data through Firebase backend.[file:1]

A three-stage pipeline (acquisition → fusion via fuzzy logic → visualization/feedback) ensures continuous monitoring with modular hardware–software separation.[file:1]

---

## Stress Classification Logic

Sensor value ranges are mapped into low, medium, and high stress levels, which are combined by fuzzy rules:[file:1]

| Sensor              | Low Stress       | Medium Stress      | High Stress        |
|---------------------|------------------|--------------------|--------------------|
| PPG (RMSSD, ms)     | `< 60`           | `60 – 90`          | `> 90`             |
| GSR (kΩ)            | `< 30`           | `30 – 50`          | `> 50`             |
| Accelerometer (m/s²)| `0.1 – 0.3`      | `0.3 – 2.4`        | `> 2.4`            |
| Gyroscope (rad/s)   | `< 2.62`         | `2.62 – 4.19`      | `> 4.19`           |

Consistently high readings across one or more channels result in a high stress classification; mixed or moderate readings map to normal, and low activity/physiology map to calm.[file:1]

---

## Tech Stack

- **Hardware**: Arduino Uno, PPG sensor, GSR sensor, Bluetooth module, custom wrist/wearable setup.[file:1]  
- **Mobile Platform**: Android (Java, Android Studio).[file:1]  
- **Backend / Cloud**: Firebase (real-time database/storage, synchronization).[file:1]  
- **Visualization**: MPAndroidChart for real-time graphs and plots.[file:1]  
- **Logic**: Fuzzy logic engine for rule-based stress classification.[file:1]

---

## Installation & Setup

1. **Hardware Setup**  
   - Assemble PPG and GSR sensors with Arduino Uno following the project’s wiring scheme.[file:1]  
   - Configure the Bluetooth module to broadcast sensor readings to a paired Android phone.[file:1]  
   - Ensure sensors are calibrated and securely worn to maintain stable contact.[file:1]

2. **Android App Setup**  
   - Open the Android project in Android Studio.[file:1]  
   - Configure Firebase credentials (google-services configuration) for real-time database usage.[file:1]  
   - Build and install the app on an Android device with accelerometer and gyroscope support.[file:1]  
   - Grant required permissions (Bluetooth, sensors, network, etc.).[file:1]

3. **First Run**  
   - Pair the wearable device with the smartphone via Bluetooth.[file:1]  
   - Register or log in to the app using the built-in authentication.[file:1]  
   - Start the monitoring session to begin real-time stress detection and visualization.[file:1]

*(Adjust package names, Firebase config files, and Arduino sketch paths as needed for your repository structure.)*

---

## How It Works (Runtime Flow)

1. Wearable sensors continuously capture PPG and GSR data and send them via Bluetooth to the smartphone.[file:1]  
2. The Android app simultaneously reads accelerometer and gyroscope data from the device.[file:1]  
3. All four input streams are normalized and fed into a fuzzy logic inference system using the defined thresholds.[file:1]  
4. The system outputs a stress level label (calm/normal/stressed), which is displayed in real time along with graphs of raw signals.[file:1]  
5. Data is optionally logged to Firebase for historical viewing and analysis.[file:1]

---

## Use Cases

- Continuous self-monitoring of stress during study, work, or commuting.[file:1]  
- Non-clinical mental health awareness tools for students and professionals.[file:1]  
- Research prototypes for stress detection, human–computer interaction, or affective computing.[file:1]

---

## Limitations

- Requires wearing the hardware device, which may affect long-term comfort and compliance.[file:1]  
- Depends on a smartphone for full functionality and connectivity.[file:1]  
- Bluetooth link may occasionally drop, requiring reconnection.[file:1]  
- Fuzzy thresholds are literature-based and not individually personalized.[file:1]

---

## Future Improvements

- Miniaturized, watch-like wearable form factor with integrated PCB.[file:1]  
- Personalized stress thresholds based on user-specific baselines.[file:1]  
- Integration of additional mobile context signals (notifications, audio environment, etc.).[file:1]  
- Optional machine learning / deep learning models on top of current fuzzy features.[file:1]  
- Enhanced Android UI/UX with historical analytics and configurable alerts.[file:1]

---

## Ethical and Legal Notes

- Only essential physiological and motion data are collected, with explicit user consent and no personal identifiers stored in the system design.[file:1]  
- Data processing is intended to occur locally on the device, with Firebase usage structured to respect anonymity and basic data protection principles.[file:1]  
- The system is a research prototype and does not constitute a certified medical device; it must not be used for diagnosis or treatment decisions.[file:1]

---

## Authors

- **Shah Md Khalil Ullah** (Roll: 2007090)  
- **Shayka Islam Shipra** (Roll: 2007110)[file:1]

Department of Computer Science and Engineering,  
Khulna University of Engineering & Technology (KUET), Bangladesh.[file:1]
