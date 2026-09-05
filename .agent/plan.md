# Project Plan

Transform the existing Digital Level app into a modern, professional bubble level tool based on the reference UI. Key UI components: 1) Main Screen (Dashboard): Title, linear level preview graphic, 'START LEVEL' button, 'HISTORY' & 'CALIBRATE' buttons, Last Measurement card, Settings switches (Dark Mode, Keep Screen Awake, Level Feedback). 2) Level Screen: Horizontal/Vertical mode toggle. A complex level display consisting of a top horizontal tubular level, a left vertical tubular level, and a central circular bull's eye level. Glossy/3D visual styling with neon green fluid. Bottom section includes X/Y/Overall angles, a prominent status badge, HOLD/LOCK/CALIBRATE buttons, and a SAVE MEASUREMENT button. Preserve all previous technical requirements: XML Views, CustomView extending View, Room DB, 4 Activities, Accelerometer, Light sensor. The CustomView must be updated to draw the tubular and bull's eye levels with glossy realistic graphics.

## Project Brief

# Project Brief: Digital Level

## Features
1. **Interactive Dashboard:** A main landing screen providing quick access to the level tool, calibration, and app settings (Dark Mode, Keep Screen Awake, Level Feedback), alongside a summary of the most recent measurement.
2. **Professional Bubble Level Tool:** A high-precision measuring interface displaying a custom-drawn, glossy 2D/3D visual representation of horizontal, vertical, and central bull's eye levels with neon green fluid. 
3. **Hardware Sensor & Calibration Engine:** Real-time angle calculations (X, Y, and Overall) powered by the device's Accelerometer and Light Sensor, featuring manual zero-calibration, screen lock, and measurement hold capabilities.
4. **Measurement History:** The ability to save active measurement readings to a local database and browse past recordings in a dedicated history list and details view.

## High-Level Tech Stack
* **Language:** Kotlin
* **UI Framework:** Classic Android View System (XML Layouts, ViewBinding, and Material 3 XML Components) — specifically chosen to satisfy the strict university project requirements (No Jetpack Compose).
* **Architecture & Navigation:** Standard Multi-Activity Architecture utilizing Android Intents to navigate between the required 4 screens (`MainActivity`, `LevelActivity`, `HistoryActivity`, `MeasurementDetailsActivity`).
* **Custom Graphics:** Android `Canvas` API (extending standard `View`) for rendering the complex 3D/glossy graphical mechanics of the tubular and circular bubble levels.
* **Hardware Integration:** Android `SensorManager` API for interacting with the hardware Accelerometer and Light Sensor.
* **Persistence & Concurrency:** Room Database for persisting historical measurement data and retrieving the last measurement, supported by Kotlin Coroutines for asynchronous database operations.

## Implementation Steps
**Total Duration:** 15m 38s

### Task_11_Update_Colors_And_Themes: Update colors.xml and themes.xml to use a dark theme with neon green accents (#A2FF00) to match the reference design.
- **Status:** COMPLETED
- **Updates:** Updated colors.xml and themes.xml (DayNight) with neon green (#A2FF00) and dark backgrounds. Updated Material dependency. Project builds successfully.
- **Acceptance Criteria:**
  - Primary color is neon green
  - Dark theme background is deep gray/black
  - Light theme supported as a variant
- **Duration:** 2m 52s

### Task_12_Update_MainActivity_UI: Update activity_main.xml to match the reference dashboard: add a small horizontal level preview graphic (can be a smaller instance of DigitalLevelView or a custom drawable), make START LEVEL button neon green, update HISTORY and CALIBRATE to outlined/tonal buttons, polish the Last Measurement and Settings sections to match the dark/neon style.
- **Status:** COMPLETED
- **Updates:** activity_main.xml completely redesigned to match the reference dashboard (flat dark theme, neon green START LEVEL button, level preview vector graphic, outlined action buttons, updated settings switches). Project compiles successfully.
- **Acceptance Criteria:**
  - Dashboard looks like reference image
  - START LEVEL is green and prominent
  - Horizontal level graphic included above READY text
- **Duration:** 3m 22s

### Task_13_Redesign_DigitalLevelView_Graphics: Completely rewrite DigitalLevelView onDraw() to render three components: Top horizontal tubular level, Left vertical tubular level, and Center circular bull's eye level. Implement 3D/glossy shading, neon green fluid, and accurate bubble positioning based on X and Y angles.
- **Status:** COMPLETED
- **Updates:** DigitalLevelView redesigned with Top Horizontal Tube, Left Vertical Tube, and Center Bull's Eye circular level. 3D glossy shaders, neon green liquid, target lines, and dynamic scaling implemented. assembleDebug and 41 unit tests passed.
- **Acceptance Criteria:**
  - Draws top horizontal tube with bubble
  - Draws left vertical tube with bubble
  - Draws center bull's eye with 2D bubble
  - Glossy/3D aesthetics applied
  - Bubbles move based on X/Y tilt
- **Duration:** 4m 28s

### Task_14_Update_LevelActivity_UI: Update activity_level.xml layout. Adjust the Horizontal (Flat) / Vertical (Edge) toggle to look like the reference segmented button. Update the X/Y/Overall angle text grid, the large Status badge, the HOLD/LOCK/CALIBRATE button row, and the SAVE MEASUREMENT bottom button. Use the new DigitalLevelView layout.
- **Status:** COMPLETED
- **Updates:** activity_level.xml and LevelActivity.kt updated to match reference level screen (segmented Horizontal/Vertical toggle, 3-box X/Y/Overall angle grid, status badge card, HOLD/LOCK/CALIBRATE button row, neon green SAVE MEASUREMENT primary button). assembleDebug and 41 unit tests passed.
- **Acceptance Criteria:**
  - Layout matches reference Level screen
  - Segmented toggle for Flat/Edge
  - Values grid for angles
  - Status badge with icon and text
  - Bottom button row styling updated
- **Duration:** 4m 7s

### Task_15_Build_And_Test_Refinements: Build the project, run unit tests to ensure no regressions, and verify UI rendering on devices.
- **Status:** COMPLETED
- **Updates:** All unit tests passed with 0 failures. Gradle assembleDebug build succeeded with 0 errors. All components verified.
- **Acceptance Criteria:**
  - assembleDebug succeeds
  - testDebugUnitTest succeeds
  - App runs without crashing
- **Duration:** 49s

