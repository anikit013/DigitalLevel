package com.example.digitallevel

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.digitallevel.data.PreferencesManager
import com.example.digitallevel.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var preferencesManager: PreferencesManager

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
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up top app bar back navigation button
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Initialize switch states from PreferencesManager
        binding.switchDarkMode.isChecked = preferencesManager.isDarkMode
        binding.switchKeepScreenAwake.isChecked = preferencesManager.keepScreenAwake
        binding.switchLevelFeedback.isChecked = preferencesManager.levelFeedbackEnabled

        updateKeepScreenAwakeFlag(preferencesManager.keepScreenAwake)

        // Row clicks to toggle switches easily
        binding.rowDarkMode.setOnClickListener {
            binding.switchDarkMode.isChecked = !binding.switchDarkMode.isChecked
        }

        binding.rowKeepScreenAwake.setOnClickListener {
            binding.switchKeepScreenAwake.isChecked = !binding.switchKeepScreenAwake.isChecked
        }

        binding.rowLevelFeedback.setOnClickListener {
            binding.switchLevelFeedback.isChecked = !binding.switchLevelFeedback.isChecked
        }

        // Switch listeners
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.isDarkMode = isChecked
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.switchKeepScreenAwake.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.keepScreenAwake = isChecked
            updateKeepScreenAwakeFlag(isChecked)
        }

        binding.switchLevelFeedback.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.levelFeedbackEnabled = isChecked
        }
    }

    private fun updateKeepScreenAwakeFlag(keepAwake: Boolean) {
        if (keepAwake) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}
