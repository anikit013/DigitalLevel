package com.example.digitallevel

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.digitallevel.databinding.ActivityLevelBinding
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
    private val tiltCalculator = TiltCalculator()

    private val viewModel: LevelViewModel by viewModels {
        val app = application as DigitalLevelApplication
        ViewModelFactory(app.repository, app.preferencesManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sensorHelper = SensorManagerHelper(this)
        viewModel.updateSensorAvailability(
            accelAvailable = sensorHelper.isAccelerometerAvailable(),
            lightAvailable = sensorHelper.isLightSensorAvailable()
        )

        if (intent.hasExtra(EXTRA_REF_X) && intent.hasExtra(EXTRA_REF_Y)) {
            val refX = intent.getFloatExtra(EXTRA_REF_X, 0f)
            val refY = intent.getFloatExtra(EXTRA_REF_Y, 0f)
            viewModel.setCalibrationOffset(refX, refY)
            Toast.makeText(this, "Reference loaded for calibration", Toast.LENGTH_SHORT).show()
        }

        binding.btnCalibrate.setOnClickListener {
            viewModel.calibrateCurrentPosition()
            Toast.makeText(this, "Calibrated to current position", Toast.LENGTH_SHORT).show()
        }

        binding.btnResetCalibration.setOnClickListener {
            viewModel.resetCalibration()
            Toast.makeText(this, "Calibration reset", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveMeasurement.setOnClickListener {
            viewModel.saveMeasurement { id ->
                val state = viewModel.uiState.value
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_MEASUREMENT_ID, id.toInt())
                    putExtra(EXTRA_TILT, state.overallTilt)
                    putExtra(EXTRA_STATUS, state.status)
                }
                setResult(RESULT_OK, resultIntent)
                Toast.makeText(this, "Measurement saved!", Toast.LENGTH_SHORT).show()
            }
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
    }

    override fun onPause() {
        super.onPause()
        sensorHelper.unregisterListeners(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val tiltData = tiltCalculator.process(event.values)
                viewModel.processTilt(tiltData.xTilt, tiltData.yTilt)
            }
            Sensor.TYPE_LIGHT -> {
                viewModel.processLight(event.values[0])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateUI(state: LevelUiState) {
        if (!state.isAccelerometerAvailable) {
            binding.tvOverallTilt.text = getString(R.string.overall_tilt, "Sensor unavailable")
            binding.tvXAngle.text = getString(R.string.x_angle, "Sensor unavailable")
            binding.tvYAngle.text = getString(R.string.y_angle, "Sensor unavailable")
            binding.tvLevelStatus.text = getString(R.string.level_status, "Sensor unavailable")
        } else {
            binding.levelView.updateTilt(state.calibratedX, state.calibratedY)
            binding.tvOverallTilt.text = getString(
                R.string.overall_tilt,
                String.format(Locale.getDefault(), "%.1f°", state.overallTilt)
            )
            binding.tvXAngle.text = getString(
                R.string.x_angle,
                String.format(Locale.getDefault(), "%.1f°", state.calibratedX)
            )
            binding.tvYAngle.text = getString(
                R.string.y_angle,
                String.format(Locale.getDefault(), "%.1f°", state.calibratedY)
            )
            binding.tvLevelStatus.text = getString(R.string.level_status, state.status)
        }

        if (!state.isLightSensorAvailable) {
            binding.tvAmbientLight.text = getString(R.string.ambient_light, "0", "Sensor unavailable")
        } else {
            val lightCategory = when {
                state.lightLevel < 10f -> "Dark"
                state.lightLevel < 1000f -> "Normal"
                else -> "Bright"
            }
            binding.tvAmbientLight.text = getString(
                R.string.ambient_light,
                String.format(Locale.getDefault(), "%.1f", state.lightLevel),
                lightCategory
            )
        }

        val statusStr = if (state.isCalibrated) "Active" else "None"
        binding.tvCalibrationStatus.text = getString(R.string.calibration_status, statusStr)
    }
}
