package com.example.digitallevel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.digitallevel.data.MeasurementEntity
import com.example.digitallevel.databinding.ActivityMeasurementDetailsBinding
import com.example.digitallevel.ui.MeasurementDetailsViewModel
import com.example.digitallevel.ui.ViewModelFactory
import com.example.digitallevel.util.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MeasurementDetailsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MEASUREMENT_ID = Constants.EXTRA_MEASUREMENT_ID
    }

    private lateinit var binding: ActivityMeasurementDetailsBinding

    private val viewModel: MeasurementDetailsViewModel by viewModels {
        val app = application as DigitalLevelApplication
        ViewModelFactory(app.repository, app.preferencesManager)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMeasurementDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val measurementId = intent.getIntExtra(EXTRA_MEASUREMENT_ID, -1)
        if (measurementId == -1) {
            Toast.makeText(this, "Invalid measurement ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.loadMeasurement(measurementId)

        lifecycleScope.launch {
            viewModel.measurement.collect { measurement ->
                if (measurement != null) {
                    displayMeasurementDetails(measurement)
                }
            }
        }

        binding.btnUseReference.setOnClickListener {
            val m = viewModel.measurement.value
            if (m != null) {
                viewModel.setAsReference(m)
                val intent = Intent(this, LevelActivity::class.java).apply {
                    putExtra(Constants.EXTRA_REF_X, m.angleX)
                    putExtra(Constants.EXTRA_REF_Y, m.angleY)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                startActivity(intent)
                finish()
            }
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteCurrentMeasurement {
                val resultIntent = Intent().apply {
                    putExtra(EXTRA_MEASUREMENT_ID, measurementId)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }

    private fun displayMeasurementDetails(measurement: MeasurementEntity) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        binding.tvDetOverallTilt.text = getString(
            R.string.overall_tilt,
            String.format(Locale.getDefault(), "%.1f°", measurement.overallTilt)
        )
        binding.tvDetXAngle.text = getString(
            R.string.x_angle,
            String.format(Locale.getDefault(), "%.1f°", measurement.angleX)
        )
        binding.tvDetYAngle.text = getString(
            R.string.y_angle,
            String.format(Locale.getDefault(), "%.1f°", measurement.angleY)
        )
        binding.tvDetLight.text = String.format(Locale.getDefault(), "Light: %.1f lux", measurement.lightLevel)
        binding.tvDetStatus.text = getString(R.string.level_status, measurement.status)
        binding.tvDetDateTime.text = getString(R.string.date_time, dateFormat.format(Date(measurement.timestamp)))
    }
}
