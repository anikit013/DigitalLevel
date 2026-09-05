# Digital Level 📐

A modern Android **Digital Level / Smartphone Inclinometer** built with
Kotlin. The application turns a smartphone into a practical spirit-level
tool using the device's accelerometer for inclination measurement and an
ambient light sensor for a live light-meter feature.

Developed as a **Mobile Systems / Android university project**, with a
focus on real sensor integration, custom UI rendering, multiple
Activities, Activity communication, persistent measurement history,
calibration, and a clean responsive interface.

------------------------------------------------------------------------

## ✨ Features

### 📐 Digital Level

-   Real-time inclination measurement.
-   **Horizontal (Flat)** and **Vertical (Edge)** modes.
-   X-axis, Y-axis, and overall angle display.
-   Horizontal vial, vertical vial, and bull's-eye visualization.
-   Measurement status such as `LEVEL`, `SLIGHTLY TILTED`, and `TILTED`.

### 🎯 Calibration

Calibrate the level to establish a preferred reference orientation.
Calibration is integrated with the existing level measurement workflow.

### ⏸️ Hold

Temporarily freeze the current level measurement while inspecting or
recording a result.

### 🔒 Lock / Reference

Lock the current position as a reference for relative measurements.

### 💡 Ambient Light Meter

Optional live ambient-light measurement using Android's real
`Sensor.TYPE_LIGHT` sensor.

The feature displays: - Current illuminance in **lux (lx)**. -
Light-level category. - Visual intensity indication. - Graceful handling
when a device has no light sensor.

Data flow:

``` text
Sensor.TYPE_LIGHT
       ↓
event.values[0]
       ↓
Ambient illuminance (lux)
       ↓
Light Meter UI
```

### 🔢 Digital Meter

Optional numerical angle display using the application's existing
accelerometer/TiltCalculator measurement pipeline.

Example:

``` text
0.42°
LEVEL
```

No separate or fake angle-calculation system is used.

### 📳 Haptic Feedback

Optional feedback when the measured surface reaches the `LEVEL` state.

### 📱 Keep Screen Awake

Optional setting to keep the display active while using the level.

### 🕘 Measurement History

Save measurements locally and review them later through a
RecyclerView-backed history screen.

### 📋 Measurement Details

Open individual saved measurements to inspect recorded measurement
information.

### 🌓 Light and Dark Themes

Light and Dark appearance modes with theme-aware UI colors and readable
content.

### 🎨 Custom View

A functional Android Custom View renders the level visualization using
Canvas drawing.

------------------------------------------------------------------------

## 🏗️ Application Structure

``` text
MainActivity
    │
    ├── Start Level
    │       ↓
    │   LevelActivity
    │       ├── Flat / Edge
    │       ├── Accelerometer
    │       ├── Light Sensor
    │       ├── Digital Meter
    │       ├── Light Meter
    │       ├── Hold
    │       ├── Lock
    │       ├── Calibration
    │       └── Save Measurement
    │
    ├── History
    │       ↓
    │   HistoryActivity
    │       ↓
    │   MeasurementDetailsActivity
    │
    └── Settings
            ↓
        SettingsActivity
```

The application uses multiple Activities and Android Intent/Activity
Result communication where appropriate.

------------------------------------------------------------------------

## 🧩 Technology Stack

  Technology             Purpose
  ---------------------- --------------------------------------
  Kotlin                 Primary language
  Android SDK            Application platform
  Android Activities     Screen/navigation architecture
  Android Sensor APIs    Sensor access
  Accelerometer          Inclination measurement
  Ambient Light Sensor   Illuminance measurement
  Custom View / Canvas   Level visualization
  RecyclerView           Measurement history
  Room                   Local persistence
  ViewModel              UI/application state
  Preferences            Settings and calibration persistence
  Gradle                 Build system

------------------------------------------------------------------------

## 📡 Sensors

The application demonstrates two real Android sensors.

### Accelerometer

``` text
Accelerometer
      ↓
Sensor values
      ↓
TiltCalculator
      ↓
X / Y / Overall angle
      ↓
Level status
      ↓
Custom View + Digital Meter
```

### Ambient Light Sensor

``` text
Light Sensor
      ↓
Sensor.TYPE_LIGHT
      ↓
event.values[0]
      ↓
Lux
      ↓
Light Meter
```

The light sensor is independent of inclination calculations.

------------------------------------------------------------------------

## 📊 Measurement Modes

### Horizontal / Flat

Used for measuring surfaces placed approximately horizontally.

### Vertical / Edge

Used for measuring vertical edges or surfaces.

The selected mode controls the existing measurement and visualization
behavior.

------------------------------------------------------------------------

## 🗄️ Data Persistence

Saved measurements are persisted locally using **Room**.

High-level data flow:

``` text
MeasurementEntity
       ↓
MeasurementDao
       ↓
Room Database
       ↓
Repository
       ↓
ViewModel
       ↓
UI
```

Measurements are saved explicitly by the user rather than continuously
writing sensor events to the database.

------------------------------------------------------------------------

## 🔄 Activity Communication

The application demonstrates real Activity-to-Activity data transfer.

Example:

``` text
MainActivity
     ↓
LevelActivity
     ↓
Save Measurement
     ↓
Activity Result / Intent data
     ↓
MainActivity
```

The history/details workflow also uses Activity communication.

------------------------------------------------------------------------

## ⚙️ Settings

The dedicated Settings screen contains:

### Dark Mode

Switch between Light and Dark appearance.

### Keep Screen Awake

Keep the screen active while using the level.

### Level Feedback

Enable or disable haptic feedback when the level reaches the `LEVEL`
state.

Settings use the application's existing persistence mechanism.

------------------------------------------------------------------------

## 🎨 User Interface

The application follows a professional measuring-instrument style.

### Main Screen

Provides access to: - Start Level - History - Calibration - Last
Measurement - Settings

### Level Screen

Provides: - Flat / Edge selection - Visual level - X/Y/overall angle -
Measurement status - Hold - Lock - Calibration - Optional Light Meter -
Optional Digital Meter - Save Measurement

### Settings Screen

Provides the application's user preferences without cluttering the
measurement dashboard.

------------------------------------------------------------------------

## 📱 Requirements

Recommended development environment: - Android Studio - Kotlin - Android
SDK - Android device or compatible emulator

A **physical Android device is recommended** for complete sensor
testing, especially for: - Accelerometer inclination. - Ambient light
sensor. - Live lux changes. - Haptic feedback.

Emulator sensor availability depends on its configuration.

------------------------------------------------------------------------

## 🚀 Getting Started

### Clone

``` bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd <PROJECT_DIRECTORY>
```

Open the project in Android Studio and allow Gradle synchronization to
complete.

### Build

``` bash
./gradlew assembleDebug
```

### Run tests

``` bash
./gradlew test
```

Then connect an Android device or start an emulator and run the
application from Android Studio.

------------------------------------------------------------------------

## 🧪 Testing

### Automated tests

``` bash
./gradlew test
```

### Build verification

``` bash
./gradlew assembleDebug
```

### Manual accelerometer test

1.  Open Digital Level.
2.  Select Flat or Edge.
3.  Tilt the device.
4.  Verify X/Y/overall measurements change.
5.  Verify the visual level responds.

### Manual light-sensor test

1.  Open LevelActivity.
2.  Enable **Light Meter**.
3.  Observe the current lux value.
4.  Cover the device's ambient light sensor.
5.  Verify the lux value decreases.
6.  Uncover the sensor.
7.  Verify the lux value increases.

Sensor values must come from the actual device sensor and must never be
hardcoded or randomly generated.

------------------------------------------------------------------------

## 🧭 Typical Usage

``` text
Open Digital Level
       ↓
Start Level
       ↓
Choose Flat or Edge
       ↓
Measure surface
       ↓
Read visual + numerical level
       ↓
Optional:
  ├── Digital Meter
  ├── Light Meter
  ├── Hold
  └── Lock
       ↓
Calibrate if required
       ↓
Save Measurement
       ↓
View in History
```

------------------------------------------------------------------------

## 📸 Screenshots

Recommended repository structure:

``` text
docs/
├── main-light.png
├── main-dark.png
├── level-flat.png
├── level-edge.png
├── light-meter.png
├── digital-meter.png
├── history.png
└── settings.png
```

Add screenshots with standard Markdown, for example:

``` markdown
![Main Screen](docs/main-light.png)
```

------------------------------------------------------------------------

## 🎓 Academic Project

This application was developed as a **Mobile Systems / Android
university project**.

The project demonstrates: - Kotlin Android development. - Multiple
Activities. - Intent-based communication. - Activity Results. - Real
Android sensors. - Custom View and Canvas rendering. - RecyclerView. -
Room database. - ViewModel/state handling. - Persistent settings. -
Calibration. - Sensor lifecycle management. - Light/Dark themes. -
Responsive UI design.

------------------------------------------------------------------------

## 🔐 Privacy

The core application does not require an online account or cloud
service.

Sensor data is used locally for measurement functionality. Saved
measurements are stored locally through the application's Room database.

------------------------------------------------------------------------

## 📁 High-Level Project Structure

``` text
app/
├── src/
│   └── main/
│       ├── java/.../
│       │   ├── MainActivity
│       │   ├── LevelActivity
│       │   ├── HistoryActivity
│       │   ├── MeasurementDetailsActivity
│       │   ├── SettingsActivity
│       │   ├── sensor/
│       │   ├── database/
│       │   ├── repository/
│       │   ├── viewmodel/
│       │   └── ui/
│       │
│       └── res/
│           ├── drawable/
│           ├── mipmap/
│           ├── values/
│           └── values-night/
│
├── build.gradle / build.gradle.kts
└── settings.gradle / settings.gradle.kts
```

The exact package structure may vary with the current project version.

------------------------------------------------------------------------

## 🛠️ Development Notes

### Sensor lifecycle

Sensors should be registered while required and unregistered
appropriately with the Activity lifecycle to avoid unnecessary resource
usage.

### Light Meter

The Light Meter is optional and handles devices without
`Sensor.TYPE_LIGHT` gracefully.

### Theme

Theme-dependent UI colors should use the active Light/Dark theme rather
than hardcoded colors, keeping text, surfaces, controls, and indicators
readable.

### Measurement storage

Measurements are saved when explicitly requested by the user. Sensor
events are not continuously written to Room.

------------------------------------------------------------------------

## 🔮 Future Improvements

Potential extensions include: - Measurement export/share. - Advanced
measurement statistics. - Additional calibration profiles. - More
visualization modes. - Measurement unit preferences. - Accessibility
improvements. - Additional sensor-based tools. - Kotlin Multiplatform
Android/iOS version.

------------------------------------------------------------------------

## 👨‍💻 Author

**Anikit Chaudhary**

Developed as an Android / Mobile Systems project.

------------------------------------------------------------------------

## 📄 License

This repository was created primarily as a university/educational
project.

If the project is intended for public reuse, add an appropriate
open-source license such as MIT or Apache-2.0.
