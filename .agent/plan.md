# Project Plan

A smartphone-based digital spirit level Android application. Uses accelerometer for tilt detection and ambient light sensor for brightness. Features 4 Activities (MainActivity, LevelActivity, HistoryActivity, MeasurementDetailsActivity) communicating via Intents and Activity Result API. Includes a CustomView for the bubble level, RecyclerView for history, and Room database for persistence. Kotlin, API 34-36, Classic View System (XML + CustomView extends View + RecyclerView).

## Project Brief

# Project Brief: Digital Spirit Level App

## Features
1. **Real-Time Tilt Detection:** Accurately measure and calculate device orientation/tilt across X and Y axes using the device's hardware accelerometer.
2. **Visual Bubble Level (Custom View):** Display an intuitive, real-time visual bubble interface mapped to the accelerometer data to assist users in leveling surfaces.
3. **Measurement History & Details:** Allow users to capture and save specific tilt measurements, view them in a historical list, and inspect individual measurement details.
4. **Ambient Light Adaptation:** Utilize the ambient light sensor to dynamically adjust screen brightness or contrast, ensuring the app remains readable in varying environments (e.g., bright outdoor construction sites).

## High-Level Technical Stack
* **Language & SDK:** Kotlin, targeting Android API 34-36.
* **UI Framework:** Classic View System (XML Layouts). 
* **Custom UI Component:** A `CustomView` extending `android.view.View` (overriding the `onDraw` method) to render the 2D bubble level graphics natively.
* **Navigation & Communication:** Multiple standard Activities (`MainActivity`, `LevelActivity`, `HistoryActivity`, `MeasurementDetailsActivity`) using explicit Android `Intent`s and the Activity Result API for seamless communication.
* **List Rendering:** `RecyclerView` for efficiently displaying the history of saved measurements.
* **Hardware Integration:** Android `SensorManager` API to interface with the Accelerometer and Ambient Light sensors.
* **Data Persistence:** Room Database to persist saved historical measurement records, as explicitly requested.
* **Concurrency:** Kotlin Coroutines for safely handling asynchronous Room database operations off the main thread.

## Implementation Steps
**Total Duration:** 1h 53m 12s

### Task_1_RoomDB_Setup: Set up Room database, DAO, and Measurement data entities for saving tilt records.
- **Status:** COMPLETED
- **Updates:** Room database, DAO, entity, and repository implemented. Dependencies added and gradle synced. Test build passed.
- **Acceptance Criteria:**
  - Room database initialized
  - Data entities created
  - DAO methods for insert/query defined
- **Duration:** 26m 11s

### Task_2_Hardware_CustomView: Implement SensorManager for Accelerometer and Ambient Light sensors, and build the custom Bubble Level View.
- **Status:** COMPLETED
- **Updates:** DigitalLevelView, TiltCalculator, and SensorManagerHelper created and compiled successfully.
- **Acceptance Criteria:**
  - Accelerometer and Ambient Light sensors configured
  - CustomView for bubble level created overriding onDraw
  - Dynamic brightness adaptation implemented
- **Duration:** 10m 1s

### Task_3_UI_Activities: Create XML layouts and Kotlin classes for MainActivity, LevelActivity, HistoryActivity, and MeasurementDetailsActivity.
- **Status:** COMPLETED
- **Updates:** Activities, MeasurementAdapter, ViewBinding, XML layouts, and AndroidManifest created. Build passed successfully.
- **Acceptance Criteria:**
  - All 4 activities created
  - XML layouts implemented with RecyclerView for history
  - Navigation between activities structured
- **Duration:** 1h 6m 36s

### Task_4_Integration_Navigation: Integrate sensor data with UI, implement Intent/Activity Result API communication, and wire up Room DB with Coroutines.
- **Status:** COMPLETED
- **Updates:** ViewModels, PreferencesManager, Activity Result API communication, Sensor lifecycle management, and calibration persistence implemented and verified with build & tests.
- **Acceptance Criteria:**
  - Sensors update the custom Bubble View in real-time
  - Measurements can be saved and displayed in History
  - Activity Result API handles communication
  - App functions seamlessly together
- **Duration:** 5m 39s

### Task_5_Run_Verify: Run and Verify the application. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** COMPLETED
- **Updates:** All 34 unit tests passed successfully. Gradle build assembleDebug succeeded with 0 errors. All acceptance criteria satisfied.
- **Acceptance Criteria:**
  - Make sure all existing tests pass
  - Build pass
  - App does not crash
- **Duration:** 4m 45s

