package com.example.digitallevel

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import com.example.digitallevel.data.PreferencesManager
import com.example.digitallevel.databinding.ActivityMainBinding
import com.example.digitallevel.util.Constants
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager

    private val levelActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data!!
            val tilt = data.getFloatExtra(Constants.EXTRA_TILT, 0f)
            val status = data.getStringExtra(Constants.EXTRA_STATUS) ?: "N/A"
            binding.tvLastTilt.text = String.format(Locale.getDefault(), "Tilt: %.1f°", tilt)
            binding.tvLastStatus.text = getString(R.string.level_status, status)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as DigitalLevelApplication
        preferencesManager = app.preferencesManager

        binding.switchDarkMode.isChecked = preferencesManager.isDarkMode

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.isDarkMode = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.btnOpenLevel.setOnClickListener {
            levelActivityLauncher.launch(Intent(this, LevelActivity::class.java))
        }

        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        binding.btnAbout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("About Digital Level")
                .setMessage("Digital Level App v1.0\n\nMeasures tilt angles and ambient light levels using on-device sensors.")
                .setPositiveButton("OK", null)
                .show()
        }

        lifecycleScope.launch {
            app.repository.getAllMeasurements().collect { measurements ->
                if (measurements.isNotEmpty()) {
                    val last = measurements.first()
                    binding.tvLastTilt.text = String.format(Locale.getDefault(), "Tilt: %.1f°", last.overallTilt)
                    binding.tvLastStatus.text = getString(R.string.level_status, last.status)
                } else {
                    binding.tvLastTilt.text = getString(R.string.na_tilt)
                    binding.tvLastStatus.text = getString(R.string.na_status)
                }
            }
        }
    }
}
