package com.example.digitallevel.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("digital_level_prefs", Context.MODE_PRIVATE)
    )

    companion object {
        private const val KEY_DARK_MODE = "key_dark_mode"
        private const val KEY_OFFSET_X = "key_offset_x"
        private const val KEY_OFFSET_Y = "key_offset_y"
        private const val KEY_IS_CALIBRATED = "key_is_calibrated"
    }

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) {
            prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
        }

    var offsetX: Float
        get() = prefs.getFloat(KEY_OFFSET_X, 0f)
        set(value) {
            prefs.edit().putFloat(KEY_OFFSET_X, value).apply()
        }

    var offsetY: Float
        get() = prefs.getFloat(KEY_OFFSET_Y, 0f)
        set(value) {
            prefs.edit().putFloat(KEY_OFFSET_Y, value).apply()
        }

    var isCalibrated: Boolean
        get() = prefs.getBoolean(KEY_IS_CALIBRATED, false)
        set(value) {
            prefs.edit().putBoolean(KEY_IS_CALIBRATED, value).apply()
        }

    fun saveCalibration(x: Float, y: Float) {
        prefs.edit()
            .putFloat(KEY_OFFSET_X, x)
            .putFloat(KEY_OFFSET_Y, y)
            .putBoolean(KEY_IS_CALIBRATED, true)
            .apply()
    }

    fun resetCalibration() {
        prefs.edit()
            .putFloat(KEY_OFFSET_X, 0f)
            .putFloat(KEY_OFFSET_Y, 0f)
            .putBoolean(KEY_IS_CALIBRATED, false)
            .apply()
    }
}
