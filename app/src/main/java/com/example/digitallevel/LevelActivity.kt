package com.example.digitallevel

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.digitallevel.data.PreferencesManager
import com.example.digitallevel.databinding.ActivityLevelBinding
import com.example.digitallevel.sensor.MeasurementMode
import com.example.digitallevel.sensor.SensorManagerHelper
import com.example.digitallevel.sensor.TiltCalculator
import com.example.digitallevel.ui.LevelUiState
import com.example.digitallevel.ui.LevelViewModel
import com.example.digitallevel.ui.ViewModelFactory
import com.example.digitallevel.util.Constants
import kotlinx.coroutines.launch
import java.util.Locale

class LevelActivity : AppCompatActivity(), SensorEventListener {

    companion object {
        const val EXTRA_REF_X = Constants.EXTRA_REF_X
        const val EXTRA_REF_Y = Constants.EXTRA_REF_Y
        const val EXTRA_MEASUREMENT_ID = Constants.EXTRA_MEASUREMENT_ID
        const val EXTRA_TILT = Constants.EXTRA_TILT
        const val EXTRA_STATUS = Constants.EXTRA_STATUS
    }

    private lateinit var binding: ActivityLevelBinding
    private lateinit var sensorHelper: SensorManagerHelper
    private lateinit var preferencesManager: PreferencesManager
    private val tiltCalculator = TiltCalculator()

    private var currentMode = MeasurementMode.FLAT
    private var isHoldActive = false
    private var isLockActive = false
    private var lockX = 0f
    private var lockY = 0f
    private var lastRawX = 0f
    private var lastRawY = 0f
    private var previousStatus: String? = null

    private val viewModel: LevelViewModel by viewModels {
        val app = application as DigitalLevelApplication
        ViewModelFactory(app.repository, app.preferencesManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val app = application as DigitalLevelApplication
        preferencesManager = app.preferencesManager

        val targetMode = if (preferencesManager.isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorHelper = SensorManagerHelper(this)
        viewModel.updateSensorAvailability(
            accelAvailable = sensorHelper.isAccelerometerAvailable(),
            lightAvailable = sensorHelper.isLightSensorAvailable(),
        )

        updateKeepScreenAwakeFlag()

        // Header Back Button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Segmented Mode Selector
        binding.toggleModeGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val newMode = when (checkedId) {
                    R.id.btnModeEdge -> MeasurementMode.EDGE
                    else -> MeasurementMode.FLAT
                }
                if (currentMode != newMode) {
                    currentMode = newMode
                    viewModel.setMeasurementMode(newMode)
                    val modeName = if (newMode == MeasurementMode.FLAT) "Horizontal (Flat)" else "Vertical (Edge)"
                    Toast.makeText(this, "$modeName Mode", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Controls Row: HOLD
        binding.btnHold.setOnClickListener {
            isHoldActive = !isHoldActive
            if (isHoldActive) {
                binding.btnHold.text = getString(R.string.btn_release)
            } else {
                binding.btnHold.text = getString(R.string.btn_hold)
            }
            binding.levelView.setHoldState(isHoldActive)
        }

        // Controls Row: LOCK
        binding.btnLock.setOnClickListener {
            isLockActive = !isLockActive
            if (isLockActive) {
                lockX = lastRawX
                lockY = lastRawY
                binding.btnLock.text = getString(R.string.btn_unlock)
            } else {
                lockX = 0f
                lockY = 0f
                binding.btnLock.text = getString(R.string.btn_lock)
            }
            binding.levelView.setLockState(isLockActive)
        }

        // Controls Row: CALIBRATE
        binding.btnCalibrate.setOnClickListener {
            viewModel.calibrateCurrentPosition()
            Toast.makeText(this, "Calibrated to current position", Toast.LENGTH_SHORT).show()
        }

        // Tools Toggles
        binding.switchLightMeter.setOnCheckedChangeListener { _, isChecked ->
            binding.cardLightMeterPanel.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        binding.switchDigitalMeter.setOnCheckedChangeListener { _, isChecked ->
            binding.cardDigitalMeterPanel.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        // Primary Action: SAVE MEASUREMENT
        binding.btnSaveMeasurement.setOnClickListener {
            viewModel.saveMeasurement { id ->
                val state = viewModel.uiState.value
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_MEASUREMENT_ID, id.toInt())
                    putExtra(EXTRA_TILT, state.overallTilt)
                    putExtra(EXTRA_STATUS, state.status)
                }
                setResult(RESULT_OK, resultIntent)
                Toast.makeText(this, R.string.measurement_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Initial intents handling
        if (intent.hasExtra(EXTRA_REF_X) && intent.hasExtra(EXTRA_REF_Y)) {
            val refX = intent.getFloatExtra(EXTRA_REF_X, 0f)
            val refY = intent.getFloatExtra(EXTRA_REF_Y, 0f)
            viewModel.setCalibrationOffset(refX, refY)
            Toast.makeText(this, "Reference loaded for calibration", Toast.LENGTH_SHORT).show()
        } else if (intent.getBooleanExtra(Constants.EXTRA_QUICK_CALIBRATE, false)) {
            viewModel.calibrateCurrentPosition()
            Toast.makeText(this, "Calibrated to current position", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        sensorHelper.registerListeners(this)
        updateKeepScreenAwakeFlag()
    }

    override fun onPause() {
        super.onPause()
        sensorHelper.unregisterListeners(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                if (isHoldActive) return
                val rawTilt = tiltCalculator.process(event.values, currentMode)
                lastRawX = rawTilt.xTilt
                lastRawY = rawTilt.yTilt

                if (isLockActive) {
                    val lockedData = tiltCalculator.applyLockOffset(rawTilt, lockX, lockY)
                    viewModel.processTilt(lockedData.xTilt, lockedData.yTilt)
                } else {
                    viewModel.processTilt(rawTilt.xTilt, rawTilt.yTilt)
                }
            }
            Sensor.TYPE_LIGHT -> {
                viewModel.processLight(event.values[0])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateKeepScreenAwakeFlag() {
        if (::preferencesManager.isInitialized && preferencesManager.keepScreenAwake) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun updateUI(state: LevelUiState) {
        if (!state.isAccelerometerAvailable) {
            binding.tvOverall.text = getString(R.string.not_available_na)
            binding.tvXAngle.text = getString(R.string.not_available_na)
            binding.tvYAngle.text = getString(R.string.not_available_na)
            binding.tvLevelStatus.text = getString(R.string.sensor_unavailable)
            binding.tvLevelSubtitle.text = getString(R.string.ambient_light_unavailable)

            binding.tvDigitalAngle.text = getString(R.string.not_available_na)
            binding.tvDigitalStatus.text = getString(R.string.sensor_unavailable)
            binding.tvDigitalMode.text = state.currentMode.name
        } else {
            binding.levelView.updateTilt(state.calibratedX, state.calibratedY, isHoldActive, isLockActive)

            // Angle Values Grid
            binding.tvXAngle.text = String.format(Locale.getDefault(), "%.1f°", state.calibratedX)
            binding.tvYAngle.text = String.format(Locale.getDefault(), "%.1f°", state.calibratedY)
            binding.tvOverall.text = String.format(Locale.getDefault(), "%.1f°", state.overallTilt)

            // Status Badge
            when (state.status) {
                "LEVEL" -> {
                    binding.tvLevelStatus.text = getString(R.string.status_level_check)
                    binding.tvLevelSubtitle.text = getString(R.string.subtitle_level)
                }
                "SLIGHTLY TILTED" -> {
                    binding.tvLevelStatus.text = state.status
                    binding.tvLevelSubtitle.text = getString(R.string.subtitle_almost_level)
                }
                else -> {
                    binding.tvLevelStatus.text = state.status
                    binding.tvLevelSubtitle.text = getString(R.string.subtitle_tilted)
                }
            }

            // Digital Meter Panel
            binding.tvDigitalAngle.text = String.format(Locale.getDefault(), "%.2f°", state.overallTilt)
            binding.tvDigitalStatus.text = state.status
            binding.tvDigitalMode.text = state.currentMode.name

            // Trigger haptic feedback on transition to LEVEL
            val currentStatus = state.status
            if ((previousStatus != null) && (previousStatus != "LEVEL") && (currentStatus == "LEVEL")) {
                if (preferencesManager.levelFeedbackEnabled) {
                    triggerHapticFeedback()
                }
            }
            previousStatus = currentStatus
        }

        // Light Meter Panel
        if (!state.isLightSensorAvailable) {
            binding.layoutLightData.visibility = View.GONE
            binding.progressLightIntensity.visibility = View.GONE
            binding.tvLightSensorUnavailable.visibility = View.VISIBLE
            binding.tvLightSensorUnavailable.text = getString(R.string.light_sensor_unavailable)
        } else {
            binding.layoutLightData.visibility = View.VISIBLE
            binding.progressLightIntensity.visibility = View.VISIBLE
            binding.tvLightSensorUnavailable.visibility = View.GONE

            binding.tvLuxReadout.text = String.format(Locale.getDefault(), "%.0f lx", state.lightLevel)
            binding.tvLightCategory.text = viewModel.getLightCategory(state.lightLevel)
            binding.progressLightIntensity.progress = state.lightLevel.toInt().coerceIn(0, 1000)
        }
    }

    @Suppress("DEPRECATION")
    private fun triggerHapticFeedback() {
        try {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            if ((vibrator != null) && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            }
        } catch (_: Exception) {
            // Ignore if device has no vibrator or permission denied
        }
    }
}
