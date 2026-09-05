package com.example.digitallevel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
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
        const val EXTRA_DELETED = Constants.EXTRA_DELETED
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

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnBackAction.setOnClickListener {
            finish()
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
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_measurement_dialog_title))
                .setMessage(getString(R.string.delete_measurement_dialog_message))
                .setPositiveButton("Delete") { _, _ ->
                    viewModel.deleteCurrentMeasurement {
                        val resultIntent = Intent().apply {
                            putExtra(EXTRA_DELETED, true)
                            putExtra(EXTRA_MEASUREMENT_ID, measurementId)
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun displayMeasurementDetails(measurement: MeasurementEntity) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

        val formattedStatus = when (measurement.status.uppercase(Locale.getDefault())) {
            "LEVEL", "✓ LEVEL" -> "✓ LEVEL"
            "SLIGHTLY TILTED" -> "SLIGHTLY TILTED"
            "TILTED" -> "TILTED"
            else -> measurement.status.uppercase(Locale.getDefault())
        }

        binding.tvDetStatus.text = formattedStatus
        binding.tvDetOverallTilt.text = String.format(Locale.getDefault(), "%.2f°", measurement.overallTilt)
        binding.tvDetXAngle.text = String.format(Locale.getDefault(), "X Axis: %.2f°", measurement.angleX)
        binding.tvDetYAngle.text = String.format(Locale.getDefault(), "Y Axis: %.2f°", measurement.angleY)

        val modeLabel = if (measurement.mode.equals("EDGE", ignoreCase = true)) {
            getString(R.string.mode_edge)
        } else {
            getString(R.string.mode_flat)
        }
        binding.tvDetMode.text = getString(R.string.mode_display, modeLabel)
        binding.tvDetLight.text = String.format(Locale.getDefault(), "Ambient Light: %.0f lux", measurement.lightLevel)
        binding.tvDetDateTime.text = dateFormat.format(Date(measurement.timestamp))
    }
}
