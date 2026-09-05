package com.example.digitallevel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.digitallevel.data.MeasurementEntity
import com.example.digitallevel.data.PreferencesManager
import com.example.digitallevel.databinding.ActivityMainBinding
import com.example.digitallevel.util.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager

    private val levelActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if ((result.resultCode == RESULT_OK) && (result.data != null)) {
            val data = result.data!!
            val tilt = data.getFloatExtra(Constants.EXTRA_TILT, 0f)
            val status = data.getStringExtra(Constants.EXTRA_STATUS) ?: "N/A"

            // Update last measurement view immediately
            binding.layoutLastMeasurementData.visibility = View.VISIBLE
            binding.tvLastEmptyState.visibility = View.GONE
            binding.tvLastTilt.text = String.format(Locale.getDefault(), "Overall Tilt: %.1f°", tilt)
            binding.tvLastStatus.text = getString(R.string.level_status, status)
            val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
            binding.tvLastDateTime.text = getString(R.string.date_time, dateStr)
        }
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
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Apply screen awake flag based on preference
        updateKeepScreenAwakeFlag(preferencesManager.keepScreenAwake)

        // Wire Settings gear button
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        // Action Buttons
        binding.btnOpenLevel.setOnClickListener {
            levelActivityLauncher.launch(Intent(this, LevelActivity::class.java))
        }

        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnCalibrate.setOnClickListener {
            val intent = Intent(this, LevelActivity::class.java).apply {
                putExtra(Constants.EXTRA_QUICK_CALIBRATE, true)
            }
            levelActivityLauncher.launch(intent)
        }

        val showAboutDialog = View.OnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.about_dialog_title))
                .setMessage(getString(R.string.about_dialog_message))
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }
        binding.btnAbout.setOnClickListener(showAboutDialog)

        // Observe latest measurement from Room DB
        lifecycleScope.launch {
            app.repository.getAllMeasurements().collect { measurements ->
                if (measurements.isNotEmpty()) {
                    val last = measurements.first()
                    updateLastMeasurementCard(last)
                } else {
                    showEmptyLastMeasurementCard()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateKeepScreenAwakeFlag(preferencesManager.keepScreenAwake)

        // Re-sync dark mode setting if returned from SettingsActivity
        val targetMode = if (preferencesManager.isDarkMode) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode)
        }
    }

    private fun updateKeepScreenAwakeFlag(keepAwake: Boolean) {
        if (keepAwake) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    private fun updateLastMeasurementCard(last: MeasurementEntity) {
        binding.layoutLastMeasurementData.visibility = View.VISIBLE
        binding.tvLastEmptyState.visibility = View.GONE
        binding.tvLastTilt.text = String.format(Locale.getDefault(), "Overall Tilt: %.1f°", last.overallTilt)
        binding.tvLastXAxis.text = String.format(Locale.getDefault(), "X Axis: %.1f°", last.angleX)
        binding.tvLastYAxis.text = String.format(Locale.getDefault(), "Y Axis: %.1f°", last.angleY)
        binding.tvLastStatus.text = getString(R.string.level_status, last.status)

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        binding.tvLastDateTime.text = getString(R.string.date_time, dateFormat.format(Date(last.timestamp)))
    }

    private fun showEmptyLastMeasurementCard() {
        binding.layoutLastMeasurementData.visibility = View.GONE
        binding.tvLastEmptyState.visibility = View.VISIBLE
        binding.tvLastEmptyState.text = getString(R.string.no_measurements_yet)
        binding.tvLastTilt.text = getString(R.string.na_tilt)
        binding.tvLastStatus.text = getString(R.string.na_status)
    }
}
